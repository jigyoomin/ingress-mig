package ingress.mig.model;

import java.util.List;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class KubeConfig {

    private String configpath;
    
    private List<String> contexts;
}
