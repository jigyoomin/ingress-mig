package ingress.mig.command;

import java.util.List;

import ingress.mig.core.Converter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "print", description = "print annotations for analysis")
public class PrintCommand extends BaseCommand {

    @Option(names = { "-i" }, description = "Print before and after ingress yaml")
    protected boolean printIngress;
    
    @Override
    protected void execute(List<Converter> converters) {
        for (Converter converter : converters) {
            if (printIngress) {
                converter.printBeforeAndAfterIngressYaml(types);
            } else {
                converter.log(types);
            }
        }
    }

    
}
