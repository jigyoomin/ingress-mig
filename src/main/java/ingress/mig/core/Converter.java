package ingress.mig.core;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import ingress.mig.consts.NginxAnnotations;
import ingress.mig.consts.SourceAnnotations;
import ingress.mig.model.AnnotationMapping;
import ingress.mig.model.AnnotationMapping.GROUP;
import ingress.mig.model.AnnotationMapping.TYPE;
import ingress.mig.model.IngressVO;
import io.fabric8.kubernetes.api.model.networking.v1beta1.Ingress;
import io.fabric8.kubernetes.api.model.networking.v1beta1.IngressBuilder;
import io.fabric8.kubernetes.api.model.networking.v1beta1.IngressSpec;
import io.fabric8.kubernetes.api.model.networking.v1beta1.IngressSpecBuilder;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Converter {
    
    @Getter
    private List<IngressVO> ingressList = Lists.newArrayList();
    
    /**
     * as-is ingress
     */
    private Map<IngressVO, Ingress> ingressMap = Maps.newHashMap();
    
    /**
     * to-be ingress
     */
    private Map<IngressVO, Ingress> migIngressMap = Maps.newHashMap(); 
    
    private KubernetesClient client;
    
    private boolean deleteChange = false;
    private boolean multiAlb = false;
    
    private ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory()
            .disable(Feature.WRITE_DOC_START_MARKER)
            .enable(Feature.MINIMIZE_QUOTES));
    
    private Comparator<String> timeComparator =
        (a, b) -> {
            Integer intA = Integer.parseInt(toSecond(a));
            Integer intB = Integer.parseInt(toSecond(b));
            
            return intA - intB;
        };
        
    private Comparator<String> volumeSizeComparator =
            (a, b) -> {
                Integer byteA = toByte(a);
                Integer byteB = toByte(b);
                
                return byteA - byteB;
            };
            
    public Converter(String kubeconfigPath) {
        setKubeconfig(kubeconfigPath);
        
        this.client = new DefaultKubernetesClient();
    }
    
    public Converter(String kubeconfigPath, String context) {
        setKubeconfig(kubeconfigPath);
        
        Config config = Config.autoConfigure(context);
        this.client = new DefaultKubernetesClient(config);
    }
    
    public String getName() {
        String currentContext = client.getConfiguration().getCurrentContext().getName();
        return StringUtils.substringBefore(currentContext, "/");
    }
            
    public Converter convert() {
        log.info("Converting {} ...", getName());
        
        List<Ingress> ingresses = client.network().ingress().inAnyNamespace().list().getItems();
        
        ingresses.forEach(ing -> {
            try {
                IngressVO ingVo = new IngressVO();
                ingVo.setApiVersion(ing.getApiVersion());
                ingVo.setName(ing.getMetadata().getName());
                ingVo.setNamespace(ing.getMetadata().getNamespace());
                
                Map<String, String> annotations = ing.getMetadata().getAnnotations();
                Optional.ofNullable(annotations).orElse(Maps.newHashMap()).entrySet().forEach(entry -> {
                    AnnotationMapping converted = convertAnnotation(ingVo.getAnnotations(), entry.getKey(), entry.getValue());
                    ingVo.addAnnotation(converted);
                });
                
                ingressList.add(ingVo);
                
                ingressMap.put(ingVo, ing);
                migIngressMap.put(ingVo, toMigIngress(ingVo, ing));
            } catch (Exception e) {
                logClusterName();
                log.error("Error - ing {} / {}", ing.getMetadata().getNamespace(), ing.getMetadata().getName());
                log.error("print log", e);
                throw e;
            }
        });
        
        log.info("Convert complete {}", getName());
        return this;
    }
    
    private Ingress toMigIngress(IngressVO vo, Ingress ingress) {
        
//        Map<String, String> newAnnotations = Maps.newTreeMap(ingress.getMetadata().getAnnotations());
        Map<String, String> newAnnotations = Maps.newLinkedHashMap(ingress.getMetadata().getAnnotations());
        
        List<AnnotationMapping> annotations = vo.getAnnotations();
        // 삭제 먼저 수행
        annotations.stream()
            .filter(mapping -> mapping.isDelete())
            .forEach(anno -> {
                anno.getSource().keySet().forEach(key -> {
                    newAnnotations.remove(key);
                });
            });
        
        annotations.stream()
            .filter(mapping -> mapping.isChange())
            .forEach(mapping -> {
                if (mapping.isChange()) {
                    newAnnotations.putAll(mapping.getTarget());
                    
                    if (deleteChange) {
                        mapping.getSource().keySet().forEach(key -> {
                            newAnnotations.remove(key);
                        });
                    }
                }
            });
        
        String ingressClass = newAnnotations.get(NginxAnnotations.INGRESS_CLASS);
            
        IngressSpec spec = new IngressSpecBuilder(ingress.getSpec())
            .withIngressClassName(ingressClass)
            .build();
        
        return new IngressBuilder().withSpec(spec)
            .withNewMetadata()
            .withName(ingress.getMetadata().getName())
            .withNamespace(ingress.getMetadata().getNamespace())
            .withAnnotations(newAnnotations)
            .withLabels(ingress.getMetadata().getLabels())
            .endMetadata()
            .build();
    }
    
    public void logClusterName() {
        log.info("##################################################");
        log.info("# {}", client.getConfiguration().getCurrentContext().getName());
        log.info("##################################################");
    }
    
    public void log(TYPE... types) {
        logClusterName();
        ingressList.forEach(vo -> {
            vo.log(types);
        });
    }

    private AnnotationMapping convertAnnotation(List<AnnotationMapping> list, String srcKey, String srcValue) {
        AnnotationMapping mapping = getOrCreateAnnotationMapping(list, srcKey, srcValue);
        
        if (SourceAnnotations.DELETE.contains(srcKey)) {
            mapping.setType(TYPE.DELETE);
        } else if (isIbmAnnotation(srcKey)) {
            toNginxAnnotation(mapping);
        } else {
            mapping.setType(TYPE.IGNORE);
        }
        
        return mapping;
    }
    
    private AnnotationMapping getOrCreateAnnotationMapping(List<AnnotationMapping> list, String srcKey, String srcValue) {
        for (AnnotationMapping mapping : list) {
            if (mapping.getGroup() != null && mapping.getGroup().contains(srcKey)) {
                mapping.putSource(srcKey, srcValue);
                return mapping;
            }
        }
        
        AnnotationMapping newMapping = new AnnotationMapping();
        newMapping.setGroup(GROUP.getGroup(srcKey));
        newMapping.putSource(srcKey, srcValue);
        return newMapping;
    }
    
    private void toNginxAnnotation(AnnotationMapping anno) {
        anno.setType(TYPE.CHANGE);
        if (anno.hasSourceKey(SourceAnnotations.ALB_ID)) {
            albId(anno);
        } else if (anno.hasSourceKey(SourceAnnotations.REDIRECT_TO_HTTPS)) {
            redirect(anno);
        } else if (anno.hasSourceKey(SourceAnnotations.CLIENT_MAX_BODY_SIZE)) {
            maxBodySize(anno);
        } else if (anno.hasSourceKey(SourceAnnotations.PROXY_READ_TIMEOUT)) {
            proxyReadTimeout(anno);
        } else if (anno.hasSourceKey(SourceAnnotations.PROXY_CONNECT_TIMEOUT)) {
            proxyConnectTimeout(anno);
        } else if (anno.hasSourceKey(SourceAnnotations.SSL_SERVICES)) {
            sslService(anno);
        } else {
            anno.setType(TYPE.CANYOU);
        }
    }
    
    private void sslService(AnnotationMapping anno) {
        anno.putTarget(NginxAnnotations.BACKEND_PROTOCOL, "https");
    }
    
    private void albId(AnnotationMapping anno) {
        String srcValue = anno.getSource().get(SourceAnnotations.ALB_ID);
        
        String prefix = srcValue.startsWith("private") ? "private" : "public";
        String nginxClass = String.format("%s-nginx", prefix);
        
        if (multiAlb) {
            if (srcValue.endsWith("alb1")) {
                nginxClass = nginxClass + "-1";
            } else if (srcValue.endsWith("alb2")) {
                nginxClass = nginxClass + "-2";
            }
        }
        
        anno.putTarget(NginxAnnotations.INGRESS_CLASS, nginxClass);
    }

    private void redirect(AnnotationMapping anno) {
        String tgtValue = Boolean.valueOf(anno.getSource().get(SourceAnnotations.REDIRECT_TO_HTTPS)) ? "true" : "false";
        anno.putTarget(NginxAnnotations.SSL_REDIRECT, tgtValue);
    }
    
    private void maxBodySize(AnnotationMapping anno) {
        String size = getValueFromServiceSizeStyle(anno.getSource().get(SourceAnnotations.CLIENT_MAX_BODY_SIZE), volumeSizeComparator);
        anno.putTarget(NginxAnnotations.PROXY_BODY_SIZE, size);
    }
    
    private void proxyReadTimeout(AnnotationMapping anno) {
        String timeValue = getValueFromServiceSizeStyle(anno.getSource().get(SourceAnnotations.PROXY_READ_TIMEOUT), timeComparator);
        anno.putTarget(NginxAnnotations.PROXY_READ_TIMEOUT, toSecond(timeValue));
    }
    
    private void proxyConnectTimeout(AnnotationMapping anno) {
        String timeValue = getValueFromServiceSizeStyle(anno.getSource().get(SourceAnnotations.PROXY_CONNECT_TIMEOUT), timeComparator);
        anno.putTarget(NginxAnnotations.PROXY_CONNECT_TIMEOUT, toSecond(timeValue));
    }
    
    
    public void printBeforeAndAfterIngressYaml(TYPE... types) {
        ingressList.forEach(ingVo -> {
            if (ingVo.hasType(types)) {
                String originalIngStr = getOriginalYaml(ingVo);
                String migIngStr = getConvertedYaml(ingVo);
                ingVo.log(types);
                log.info("[BEFORE]");
                log.info(originalIngStr);
                log.info("---");
                log.info("[AFTER]");
                log.info(migIngStr);
            }
        });
    }

    public String getOriginalYaml(IngressVO ingVo) {
        try {
            return objectMapper.writeValueAsString(cleansing(ingressMap.get(ingVo)));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public String getConvertedYaml(IngressVO ingVo) {
        try {
            return objectMapper.writeValueAsString(migIngressMap.get(ingVo));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * 실제 필요한 값만 가진 모델로 바꾼다.<br/>
     * 아래 어노테이션들 삭제<p/>
     * <pre>
     * kubectl.kubernetes.io/last-applied-configuration
     * kubernetes.io/change-cause
     * </pre>
     * 
     * @param ingress
     * @return
     */
    private Ingress cleansing(Ingress ingress) {
        Map<String, String> annotations = Maps.newHashMap(ingress.getMetadata().getAnnotations());
        annotations.remove(SourceAnnotations.KUBECTL_KUBERNETES_IO_LAST_APPLIED_CONFIGURATION);
        annotations.remove(SourceAnnotations.KUBERNETES_IO_CHANGE_CAUSE);
        return new IngressBuilder().withNewMetadata()
                .withNamespace(ingress.getMetadata().getNamespace())
                .withName(ingress.getMetadata().getName())
                .withLabels(ingress.getMetadata().getLabels())
                .withAnnotations(annotations)
                .endMetadata()
                .withSpec(ingress.getSpec())
                .build();
    }
    
    
    
    /**
     * "key1=val1 key2=val2" 형태의 string 을 map 으로 치환한다.
     * 
     * @param strValue
     * @return
     */
    private Map<String, String> extractKeyValueString(String strValue) {
        Map<String, String> map = Maps.newHashMap();
        String[] keyValueArray = StringUtils.split(strValue);
        for (String keyValue : keyValueArray) {
            String[] split = StringUtils.split(keyValue, "=");
            map.put(split[0], split[1]);
        }
        
        return map;
    }
    
    private String getValueFromServiceSizeStyle(String srcValue, Comparator<String> comp) {
        String[] services = StringUtils.split(srcValue.trim(), ";");
        
        String size = null;
        for (String service: services) {
            String[] split = StringUtils.split(service.trim(), " ");
            String newSize = null;
            if (split.length == 2) {
                // serviceName=service size=10m 인 형태
                newSize = split[1].trim().split("=")[1];
            } else {
                // size=10m 인 형태면 이걸로 한다.
                String sizeStr = split[0].trim();
                String[] sizeSplit = sizeStr.split("=");
                if (sizeSplit.length == 2) {
                    newSize = sizeSplit[1];
                } else {
                    // size=10m 이 아닌 형태..
                    // 10m 처럼 단순 value 만 적용한 형태는 그냥 value 를 사용한다.
                    newSize = sizeSplit[0];
                }
            }
            
            if (size == null) {
                size = newSize;
            } else {
                size = comp.compare(size, newSize) > 0 ? size : newSize; 
            }
        }
        return size;
    }
    
    private String toSecond(String time) {
        if (time.endsWith("m") || time.endsWith("M")) {
            String substring = time.substring(0, time.length() - 1);
            return String.valueOf(Integer.parseInt(substring) * 60);
        } else if (time.endsWith("s") || time.endsWith("M")) {
            return time.substring(0, time.length() - 1);
        } 
        
        Integer.parseInt(time);
        return time;
    }
    
    private Integer toByte(String size) {
        if (size.endsWith("m") || size.endsWith("M")) {
            String substring = size.substring(0, size.length() - 1);
            return Integer.parseInt(substring) * 1024 * 1024;
        } else if (size.endsWith("k") || size.endsWith("K")) {
            String substring = size.substring(0, size.length() - 1);
            return Integer.parseInt(substring) * 1024;
        } else if (size.endsWith("g") || size.endsWith("G")) {
            String substring = size.substring(0, size.length() - 1);
            return Integer.parseInt(substring) * 1024 * 1024 * 1024;
        } else {
            return Integer.parseInt(size);
        }
    }

    private boolean isIbmAnnotation(String key) {
        return StringUtils.startsWith(key, "ingress.bluemix.net");
    }
    
    private void setKubeconfig(String kubeconfig) {
        System.setProperty("kubeconfig", kubeconfig);
    }
    
    public Converter withDeleteChange(boolean deleteChange) {
        this.deleteChange = deleteChange;
        return this;
    }
    
    public Converter withMultiAlb(boolean multiAlb) {
        this.multiAlb = multiAlb;
        return this;
    }
    
    public void apply() {
        Set<Entry<IngressVO,Ingress>> entrySet = migIngressMap.entrySet();
        entrySet.forEach(entry -> {
            IngressVO vo = entry.getKey();
            Ingress ingress = entry.getValue();
            try {
                client.network().ingress().inNamespace(vo.getNamespace()).createOrReplace(ingress);
                log.info("Ingress {} / {} configured.", vo.getNamespace(), vo.getName());
            } catch (Exception e) {
                log.error("Failed to apply ingress {} / {}", vo.getNamespace(), vo.getName());
                log.error(getConvertedYaml(vo));
                log.error("Exception trace", e);
            }
        });
    }
    
}
