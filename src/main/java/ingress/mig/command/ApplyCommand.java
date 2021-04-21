package ingress.mig.command;

import java.util.List;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;

import ingress.mig.core.Converter;
import picocli.CommandLine.Command;

@Command(name = "apply", description = "real ingress annotation migration")
public class ApplyCommand extends BaseCommand {

    @Override
    protected void execute(List<Converter> converters) {
        System.out.println("Trying to apply with - " + kubeconfigs);
        boolean letsgo = confirm("You want to apply?? (y/n) : ")
                && confirm("Have you checked excel?? (y/n) : ")
                && confirm("...Be careful... Really?? (y/n) : ")
                && confirm("Seriously?? (y/n) : ")
                ;
        
        if (letsgo) {
            System.out.println("ok.. pray...");
            for (Converter c : converters) {
//                c.apply();
            }
        } else {
            System.out.println("ok canceled.");
        }
    }

    private boolean confirm(String question) {
        System.out.print(question);
        Scanner sc = new Scanner(System.in);
        String yes = sc.next();
//        sc.close();
        return StringUtils.equalsAny(yes.toLowerCase(), "yes", "y");
    }

}
