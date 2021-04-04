package ingress.mig.model;

import java.util.ArrayList;
import java.util.List;

import ingress.mig.model.AnnotationMapping.TYPE;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class IngressVO {

    private String apiVersion;
    private String namespace;
    private String name;
    
    private List<AnnotationMapping> annotations = new ArrayList<>();
    
    public void addAnnotation(AnnotationMapping annotation) {
        annotations.add(annotation);
    }
    
    public void log(TYPE... types) {
        boolean printLog = hasType(types);
        
        if (printLog == false) {
            return;
        }
        
        log.info("======================================================");
        log.info("  Api version: {}", getApiVersion());
        log.info("  Ingress Name : {}/{}", getNamespace(), getName());
        log.info("------------------------------------------------------");
        
        annotations.forEach(anno -> anno.log(types));
        log.info("------------------------------------------------------");
        log.info("");
    }
    
    public String logString(TYPE... types) {
        StringBuffer sb = new StringBuffer();
        annotations.forEach(anno -> sb.append(anno.logString(types)));
        return sb.toString();
    }
    
    public boolean hasType(TYPE... types) {
        for (AnnotationMapping anno : annotations) {
            if (anno.existLog(types)) {
                return true;
            }
        }
        return false;
    }
}
