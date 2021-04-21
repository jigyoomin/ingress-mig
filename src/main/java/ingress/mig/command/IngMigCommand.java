package ingress.mig.command;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "im", description = "ingress migration", subcommands = {
        ApplyCommand.class, PrintCommand.class, ExportCommand.class})
public class IngMigCommand implements Runnable {

    @Override
    public void run() {
        System.out.println("executed main");
        
    }
    
    public static void main(String... args) {
        int execute = new CommandLine(new IngMigCommand()).execute(args);
        System.exit(execute);
    }

}
