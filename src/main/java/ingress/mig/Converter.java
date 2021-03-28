package ingress.mig;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import ingress.mig.Annotation.TYPE;
import io.fabric8.kubernetes.api.model.networking.v1beta1.Ingress;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Converter {
    
    private List<IngressVO> ingressList = new ArrayList<>();
    
    private KubernetesClient client;
    
    public Converter convert(String kubeconfig) {
        setKubeconfig(kubeconfig);
        
        client = new DefaultKubernetesClient();
        
        List<Ingress> ingresses = client.network().ingress().inAnyNamespace().list().getItems();
        
        ingresses.forEach(ing -> {
            IngressVO ingVo = new IngressVO();
            ingVo.setApiVersion(ing.getApiVersion());
            ingVo.setName(ing.getMetadata().getName());
            ingVo.setNamespace(ing.getMetadata().getNamespace());
            
            
            Map<String, String> annotations = ing.getMetadata().getAnnotations();
            annotations.entrySet().forEach(entry -> {
                Annotation converted = convertAnnotation(entry.getKey(), StringUtils.abbreviate(entry.getValue(), 50));
                ingVo.addAnnotation(converted);
            });
            
            ingressList.add(ingVo);
        });
        
        return this;
    }
    
    public void log(TYPE... types) {
        log.info("##################################################");
        log.info("# {}", client.getConfiguration().getCurrentContext().getName());
        log.info("##################################################");
        ingressList.forEach(vo -> {
            vo.log(types);
            log.info("");
        });
    }

    private Annotation convertAnnotation(String srcKey, String srcValue) {
        Annotation anno = new Annotation();
        anno.setSrcKey(srcKey);
        anno.setSrcValue(srcValue);
        
        if (isIbmAnnotation(srcKey)) {
            toNginxAnnotation(anno);
        } else {
            anno.setType(TYPE.IGNORE);
        }
        
        return anno;
    }
    
    private void toNginxAnnotation(Annotation anno) {
        String srcKey = anno.getSrcKey();
        anno.setType(TYPE.CHANGE);
        switch (srcKey) {
            case "ingress.bluemix.net/ALB-ID":
                albId(anno);
                break;
            case "ingress.bluemix.net/redirect-to-https":
                redirect(anno);
                break;
            case "ingress.bluemix.net/client-max-body-size":
                maxBodySize(anno);
                break;
            case "ingress.bluemix.net/proxy-read-timeout":
                proxyReadTimeout(anno);
                break;
            default:
                anno.setType(TYPE.CANYOU);
                break;
        }
    }



    private void albId(Annotation anno) {
        anno.setTgtKey("kubernetes.io/ingress.class");
        
        String srcValue = anno.getSrcValue();
        
        String prefix = srcValue.startsWith("private") ? "private" : "public";
        String nginxClass = String.format("%s-nginx", prefix);
        if (srcValue.endsWith("alb2")) {
            nginxClass = nginxClass + "-1";
        }
        
        anno.setTgtValue(nginxClass);
    }

    private void redirect(Annotation anno) {
        anno.setTgtKey("nginx.ingress.kubernetes.io/ssl-redirect");
        String tgtValue = Boolean.valueOf(anno.getSrcValue()) ? "true" : "false";
        anno.setTgtValue(tgtValue);
    }
    
    private void maxBodySize(Annotation anno) {
        anno.setTgtKey("nginx.ingress.kubernetes.io/proxy-body-size");
        
        String size = getValueFromServiceSizeStyle(anno.getSrcValue(), (a, b) -> {
            Integer byteA = toByte(a);
            Integer byteB = toByte(b);
            return byteA - byteB;
        });
        anno.setTgtValue(size);
    }
    
    private void proxyReadTimeout(Annotation anno) {
        anno.setTgtKey("nginx.ingress.kubernetes.io/proxy-read-timeout");
        
        String timeValue = getValueFromServiceSizeStyle(anno.getSrcValue(), (a, b) -> {
            Integer intA = Integer.parseInt(toSecond(a));
            Integer intB = Integer.parseInt(toSecond(b));
            
            return intA - intB;
        });
        anno.setTgtValue(toSecond(timeValue));
    }
    
    private String getValueFromServiceSizeStyle(String srcValue, Comparator<String> comp) {
        String[] services = StringUtils.split(srcValue, ";");
        
        String size = null;
        for (String service: services) {
            String[] split = StringUtils.split(service.trim(), " ");
            String newSize = null;
            if (split.length == 2) {
                // serviceName=XXX size=10m 인 형태
                newSize = split[1].split("=")[1];
            } else {
                // size=10m 인 형태면 이걸로 한다.
                newSize = split[0].split("=")[1];
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

    public boolean isIbmAnnotation(String key) {
        return StringUtils.startsWith(key, "ingress.bluemix.net");
    }
    
    public String getNginxKey(String ibmKey) {
        return null;
    }
    
    private void setKubeconfig(String kubeconfig) {
        System.setProperty("kubeconfig", kubeconfig);
    }
}
