package ingress.mig;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class Annotation {

    private TYPE type;
    
    private String srcKey;
    private String srcValue;
    
    private String tgtKey;
    private String tgtValue;
    
    public boolean change() {
        return TYPE.CHANGE.equals(type);
    }
    
    public static enum TYPE {
        CHANGE, IGNORE, CANYOU
    }
    
    public void log(TYPE... types) {
        if (inType(types)) {
            log.info("[{}] {}: {}", getType(), getSrcKey(), getSrcValue());
            if (change()) {
                log.info("         └> {}: {}", getTgtKey(), getTgtValue());
            }
        }
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
