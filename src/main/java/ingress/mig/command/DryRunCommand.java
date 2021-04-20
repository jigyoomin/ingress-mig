package ingress.mig.command;

import java.util.List;

import ingress.mig.core.Converter;
import picocli.CommandLine.Command;

@Command(name = "dryrun", description = "dry run ingress migration.. print before after ingress")
public class DryRunCommand extends BaseCommand {

    @Override
    protected void execute(List<Converter> converters) {
        System.out.println("dryrun");
    }

}
