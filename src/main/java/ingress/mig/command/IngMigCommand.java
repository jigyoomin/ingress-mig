package ingress.mig.command;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

@Command(name = "im", description = "ingress migration", subcommands = {
        ApplyCommand.class, PrintCommand.class, ExportCommand.class})
public class IngMigCommand implements Runnable {

    @Spec
    private CommandSpec spec;
    
    @Override
    public void run() {
        spec.commandLine().usage(System.err);
    }
    
    public static void main(String... args) {
        int execute = new CommandLine(new IngMigCommand()).execute(args);
        System.exit(execute);
    }

}
