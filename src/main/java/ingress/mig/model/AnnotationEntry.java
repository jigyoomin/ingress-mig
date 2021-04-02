package ingress.mig.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
public class AnnotationEntry {

    private String key;
    
    private String value;
}
