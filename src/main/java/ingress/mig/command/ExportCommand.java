package ingress.mig.command;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;

import ingress.mig.core.Converter;
import ingress.mig.view.ExcelExporter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "export", description = "export excel")
public class ExportCommand extends BaseCommand {

    @Option(names = { "-f" }, description = "Excel file name", defaultValue = "ing-mig.xls")
    protected String filename;
    
    @Override
    protected void execute(List<Converter> converters) {
        Path path = FileSystems.getDefault().getPath(filename);
        
        new ExcelExporter()
            .withFilepath(path.toAbsolutePath().toString())
            .export(converters, types);
    }
}
