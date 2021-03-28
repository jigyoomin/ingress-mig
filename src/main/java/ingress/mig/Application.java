package ingress.mig;

import java.util.ArrayList;
import java.util.List;

import ingress.mig.Annotation.TYPE;

public class Application {

    public static void main(String[] args) {
        List<String> kubeconfigs = kubeconfigs();
        
        kubeconfigs.forEach(config -> {
            Converter converter = new Converter();
            converter.convert(config);
//            converter.log(TYPE.CANYOU);
        converter.log();
        });
    }
    
    private static List<String> kubeconfigs() {
        List<String> list = new ArrayList<>();
        
        list.add("C:\\Users\\earth\\.kube\\cloudzcp-gdi-prod\\kube-config");
        
        return list;
    }
    
}
