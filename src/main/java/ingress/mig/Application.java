package ingress.mig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import ingress.mig.core.Converter;
import ingress.mig.model.AnnotationMapping;
import ingress.mig.model.AnnotationMapping.TYPE;
import ingress.mig.view.ExcelExporter;
import ingress.mig.model.IngressVO;

public class Application {

    public static void main(String[] args) {
        List<String> kubeconfigs = kubeconfigs();
        List<Converter> converters = new ArrayList<>();

        kubeconfigs.forEach(config -> {
//            converters.add(new Converter(config).convert());
        });

        
        converters.forEach(converter -> {
            System.out.println(converter.getName());
//            converter.printBeforeAndAfterIngressYaml(TYPE.CANYOU);
//            converter.log(TYPE.IGNORE);   
//            converter.log();
        });
        new ExcelExporter()
            .withFilepath("C:\\Users\\Administrator\\Documents\\ing-mig\\ing-mig.xls")
            .export(converters, TYPE.CANYOU);
//        printAnnotations(converters);
    }

    private static void printAnnotations(List<Converter> converters, TYPE... types) {
        TYPE[] values = TYPE.values();
        Map<TYPE, Set<String>> map = Maps.newHashMap();
        //        Set<String> canyouAnnotations = Sets.newHashSet();
        converters.stream()
        .map(Converter::getIngressList)
        .flatMap(Collection::stream)
        .map(IngressVO::getAnnotations)
        .flatMap(Collection::stream)
        .forEach(mapping -> {
            map.computeIfAbsent(mapping.getType(), type -> Sets.newHashSet()).addAll(mapping.getSource().keySet());
        });
        map.entrySet().forEach(entry -> {
            boolean print = false;
            if (ArrayUtils.isEmpty(types)) {
                print = true;
            } 
            for (TYPE type : types) {
                if (type.equals(entry.getKey())) {
                    print = true;
                }
            }
            if (print) {
                System.out.println(entry.getKey());
                entry.getValue().forEach(System.out::println);
                System.out.println();
            }
        });
    }

    private static List<String> kubeconfigs() {
        List<String> list = new ArrayList<>();

        list.add("C:\\Users\\Administrator\\.kube\\cloudzcp-coinery-mainnet\\kube-config");
        list.add("C:\\Users\\Administrator\\.kube\\cloudzcp-dep-dev\\kube-config");
        list.add("C:\\Users\\Administrator\\.kube\\cloudzcp-dep-prod\\kube-config");
        list.add("C:\\Users\\Administrator\\.kube\\cloudzcp-ens-prod\\kube-config");
        list.add("C:\\Users\\Administrator\\.kube\\cloudzcp-gdi-dev\\kube-config");
        list.add("C:\\Users\\Administrator\\.kube\\cloudzcp-gdi-prod\\kube-config");
        list.add("C:\\Users\\Administrator\\.kube\\cloudzcp-hioms-dev\\kube-config");
        list.add("C:\\Users\\Administrator\\.kube\\cloudzcp-hioms-prod\\kube-config");
        list.add("C:\\Users\\Administrator\\.kube\\cloudzcp-intrasys-dev\\kube-config");
        list.add("C:\\Users\\Administrator\\.kube\\cloudzcp-intrasys-prod\\kube-config");
        list.add("C:\\Users\\Administrator\\.kube\\cloudzcp-lawai-prod\\kube-config");
        list.add("C:\\Users\\Administrator\\.kube\\cloudzcp-sk-university-dev\\kube-config");
        list.add("C:\\Users\\Administrator\\.kube\\cloudzcp-sk-university-prod\\kube-config");
        list.add("C:\\Users\\Administrator\\.kube\\cloudzcp-skcc-hangarae-dev\\kube-config");
        list.add("C:\\Users\\Administrator\\.kube\\cloudzcp-skcc-hangarae-prod\\kube-config");
        list.add("C:\\Users\\Administrator\\.kube\\cloudzcp-ske-pos-dev\\kube-config");
        list.add("C:\\Users\\Administrator\\.kube\\cloudzcp-ske-pos-prod\\kube-config");
        list.add("C:\\Users\\Administrator\\.kube\\cloudzcp-ski-clx-dev\\kube-config");
        list.add("C:\\Users\\Administrator\\.kube\\cloudzcp-ski-clx-prod\\kube-config");
        list.add("C:\\Users\\Administrator\\.kube\\cloudzcp-ski-dev\\kube-config");
        list.add("C:\\Users\\Administrator\\.kube\\cloudzcp-ski-prd\\kube-config");
        list.add("C:\\Users\\Administrator\\.kube\\cloudzcp-ski-united-dev\\kube-config");
        list.add("C:\\Users\\Administrator\\.kube\\cloudzcp-ski-united-prod\\kube-config");
        list.add("C:\\Users\\Administrator\\.kube\\cloudzcp-svmgmt-dev\\kube-config");
        list.add("C:\\Users\\Administrator\\.kube\\cloudzcp-svmgmt-prod\\kube-config");

        
        // nginx-ingress
        // list.add("C:\\Users\\Administrator\\.kube\\cloudzcp-skcc-mis-dev\\kube-config");
        // list.add("C:\\Users\\Administrator\\.kube\\cloudzcp-ski-eterm-prod\\kube-config");
        return list;
    }

}
