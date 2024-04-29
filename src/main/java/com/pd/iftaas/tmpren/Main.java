package com.pd.iftaas.tmpren;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

@Command(name = "tmpren", mixinStandardHelpOptions = true, version = "1.0.2-SNAPSHOT")
public class Main implements Callable<Integer> {

    @Option(names = {"-s", "--src"}, description = """
            The source pattern; provide a regular expression with two capturing groups \
            one of which extracts the fund and another which extracts the date component \
            of the file.""")
    private String srcPattern = "PD_IFTAAS_\\w+_(?:\\w+_)?([0-9]{3}[0-9]?00)_(2[0-9]{7}).*\\.xml(?:\\.gz)?";
    @Option(names = {"-f", "--funds-target"}, description = """
            Regular expression for the funds target directory; use $n \
            to reference the n-th group in the srcPattern.""")
    private String fundsTargetPattern = "funds/$1";

    @Option(names = {"-d", "--dates-target"}, description = """
            Regular expression for the dates target directory; use $n \
            to reference the n-th group in the srcPattern.""")
    private String datesTargetPattern = "dates/$2";

    @Option(names = {"-p", "--path"}, description = """
            The path to the start directory in that the files \
            will be matched with the given source pattern.""")
    private Path dir = Path.of(System.getProperty("user.dir"));

    public Integer call() throws IOException {
        var src = Pattern.compile(srcPattern);
        try (var dirContents = Files.list(dir)) {
            dirContents.map(Path::getFileName).forEach(f -> {
                var m = src.matcher(f.toString());
                if (m.matches()) {
                    var fundsLink = dir.resolve(m.replaceAll(fundsTargetPattern));
                    var datesLink = dir.resolve(m.replaceAll(datesTargetPattern));
                    try {
                        createLink(fundsLink.resolve(f), dir.resolve(f));
                        createLink(datesLink.resolve(f), dir.resolve(f));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            return 0;
        }
    }

    private void createLink(Path link, Path src) throws IOException {
        Files.createDirectories(link.getParent());
        Files.createLink(link, src);
    }

    public static void main(String[] args) {
        System.exit(new CommandLine(new Main()).execute(args));
    }
}