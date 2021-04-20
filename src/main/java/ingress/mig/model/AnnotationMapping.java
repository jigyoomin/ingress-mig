package ingress.mig.model;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class AnnotationMapping {

    private TYPE type;
    
    private GROUP group;
    
    private Map<String, String> source = Maps.newHashMap();
    private Map<String, String> target = Maps.newHashMap();
    
    public boolean isChange() {
        return TYPE.CHANGE.equals(type);
    }
    
    public boolean isDelete() {
        return TYPE.DELETE.equals(type);
    }
    
    public static enum TYPE {
        CHANGE, // 자동 변환 가능
        IGNORE, // 아무 영향 없으므로 무시
        CANYOU, // 분석 필요
        DELETE  // 삭제 해야 하는 것들
    }
    
    public static enum GROUP {
        DEFAULT;
//        BUFFER("ingress.bluemix.net/proxy-buffer-size",
//                "ingress.bluemix.net/proxy-buffers");
        
        Set<String> annotationKeys = Sets.newHashSet();
        GROUP(String... annotationKeys) {
            this.annotationKeys.addAll(Arrays.asList(annotationKeys));
        }
        
        public boolean contains(String annotationKey) {
            return annotationKeys.contains(annotationKey);
        }
        
        public static GROUP getGroup(String annotationKey) {
            GROUP[] values = GROUP.values();
            return Arrays.stream(values)
                .filter(g -> g.contains(annotationKey))
                .findAny()
                .orElse(null);
        }
    }
    
    public void putSource(String key, String value) {
        this.source.put(key, value);
    }
    
    public void putTarget(String key, String value) {
        this.target.put(key, value);
    }
    
    public boolean hasSourceKey(String key) {
        return source.containsKey(key);
    }
    
    public String logString(TYPE... types) {
        StringBuffer sb = new StringBuffer();
        if (inType(types)) {
            Iterator<Entry<String, String>> iterator = source.entrySet().iterator();
            for (int i = 0 ; i < source.size() ; i++) {
                Entry<String, String> entry = iterator.next();
                if (i == 0) {
                    sb.append(String.format("[%s] %s: %s", getType().name(), entry.getKey(), StringUtils.abbreviate(entry.getValue(), Integer.MAX_VALUE)))
                        .append("\n");
                       
                } else {
                    sb.append(String.format("         %s: %s", entry.getKey(), StringUtils.abbreviate(entry.getValue(), Integer.MAX_VALUE)))
                        .append("\n");
                }
            }
            if (isChange()) {
                Iterator<Entry<String, String>> tgtIterator = target.entrySet().iterator();
                for (int i = 0 ; i < target.size() ; i++) {
                    Entry<String, String> entry = tgtIterator.next();
                    if (i == 0) {
                        sb.append(String.format("         └> %s: %s", entry.getKey(), entry.getValue()))
                            .append("\n");
                    } else {
                        sb.append(String.format("             %s: %s", entry.getKey(), entry.getValue()))
                            .append("\n");
                    }
                }
            }
        }
        return sb.toString();
    }
    
    public void log(TYPE... types) {
        if (inType(types)) {
            Iterator<Entry<String, String>> iterator = source.entrySet().iterator();
            for (int i = 0 ; i < source.size() ; i++) {
                Entry<String, String> entry = iterator.next();
                if (i == 0) {
                    log.info("[{}] {}: {}", getType(), entry.getKey(), StringUtils.abbreviate(entry.getValue(), Integer.MAX_VALUE));
                } else {
                    log.info("         {}: {}", entry.getKey(), StringUtils.abbreviate(entry.getValue(), Integer.MAX_VALUE));
                }
            }
            if (isChange()) {
                Iterator<Entry<String, String>> tgtIterator = target.entrySet().iterator();
                for (int i = 0 ; i < target.size() ; i++) {
                    Entry<String, String> entry = tgtIterator.next();
                    if (i == 0) {
                        log.info("         └> {}: {}", entry.getKey(), entry.getValue());
                    } else {
                        log.info("            {}: {}", entry.getKey(), entry.getValue());
                    }
                }
            }
        }
    }
    
    public boolean existLog(TYPE... types) {
        return inType(types);
    }
    
    public boolean inType(TYPE... types) {
        if (types == null || types.length == 0) {
            return true;
        }
        
        for (TYPE t : types) {
            if (t.equals(this.type)) {
                return true;
            }
        }
        
        return false;
    }
}
