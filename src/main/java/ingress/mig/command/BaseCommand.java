package ingress.mig.command;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import ingress.mig.core.Converter;
import ingress.mig.model.AnnotationMapping.TYPE;
import lombok.extern.slf4j.Slf4j;
import ingress.mig.model.KubeConfig;
import ingress.mig.model.KubeConfigYaml;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Slf4j
public abstract class BaseCommand implements Runnable {

    @Parameters(index = "0", description = "kubeconfig list yaml filename", defaultValue = "kubeconfigs.yaml")
    protected String kubeconfigs;
    
    @Option(names = { "-t" }, description = "CHANGE, IGNORE, CANYOU, DELETE", converter = TypeConverter.class)
    protected TYPE[] types;
    
    @Option(names = { "-d" }, description = "Remove changed original annotations. default is false")
    protected boolean deleteChange = false;
    
    protected KubeConfig[] configs;
    
    @Override
    public void run() {
        KubeConfig[] configs = parseKubeConfigs();
        List<Converter> converters = convert(configs);
        execute(converters);
    }
    
    protected List<Converter> convert(KubeConfig[] configs) {
        List<Converter> converters = new ArrayList<>();

        for (KubeConfig config : configs) {
            List<String> contexts = config.getContexts();
            if (contexts != null && contexts.size() > 0) {
                for (String context : config.getContexts()) {
                    converters.add(
                        new Converter(config.getConfigpath(), context)
                            .withDeleteChange(deleteChange)
                            .convert()
                    );
                }
            } else {
                converters.add(
                    new Converter(config.getConfigpath())
                        .withDeleteChange(deleteChange)
                        .convert()
                );
            }
        }
        
        return converters;
    }

    private KubeConfig[] parseKubeConfigs() {
        File kubeconfigsFile = new File(kubeconfigs);
        if (!kubeconfigsFile.exists()) {
            log.error("Can not find file {}", kubeconfigs);
            throw new RuntimeException("File not found " + kubeconfigs);
        }
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            KubeConfigYaml kubeConfigYaml = mapper.readValue(kubeconfigsFile, KubeConfigYaml.class);
            return kubeConfigYaml.getKubeconfigs();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    abstract protected void execute(List<Converter> converters);
    
    static class TypeConverter implements ITypeConverter<TYPE> {

        public TypeConverter() {
        }
        
        @Override
        public TYPE convert(String value) throws Exception {
            return TYPE.valueOf(value.toUpperCase());
        }
        
    }
}
