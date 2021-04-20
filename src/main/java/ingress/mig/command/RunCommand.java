package ingress.mig.command;

import java.util.List;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;

import ingress.mig.core.Converter;
import picocli.CommandLine.Command;

@Command(name = "run", description = "real ingress annotation migration")
public class RunCommand extends BaseCommand {

    @Override
    protected void execute(List<Converter> converters) {
        System.out.println("realm run with - " + kubeconfigs);
        System.out.println("really?? you want?? real?? (y/n)");
        Scanner sc = new Scanner(System.in);
        String yes = sc.next();
        sc.close();
        
        if (StringUtils.equalsAny(yes.toLowerCase(), "yes", "y")) {
            System.out.println("pray...");
            for (Converter c : converters) {
//                c.apply();
            }
        } else {
            System.out.println("ok");
        }
    }

}
