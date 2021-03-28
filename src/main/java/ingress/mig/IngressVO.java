package ingress.mig;

import java.util.ArrayList;
import java.util.List;

import ingress.mig.Annotation.TYPE;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class IngressVO {

    private String apiVersion;
    private String namespace;
    private String name;
    
    private List<Annotation> annotations = new ArrayList<>();;
    
    public void addAnnotation(Annotation annotation) {
        annotations.add(annotation);
    }
    
    public void log(TYPE... types) {
        log.info("======================================================");
        log.info("  Api version: {}", getApiVersion());
        log.info("  Ingress Name : {}/{}", getNamespace(), getName());
        log.info("------------------------------------------------------");
        
        annotations.forEach(anno -> anno.log(types));
        log.info("------------------------------------------------------");
    }
}
