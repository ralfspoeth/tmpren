package com.pd.iftaas.tmpren;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Main {
    public static void main(String[] args) {
        try {
            var srcPattern = Pattern.compile(args[0]);
            var linkPattern = args[1];
            var dir = Path.of(args.length>2?args[2]:System.getProperty("user.dir"));
            try (var dirContents = Files.list(dir)) {
                dirContents.map(Path::getFileName).forEach(f -> {
                            var m = srcPattern.matcher(f.toString());
                            if (m.matches()) {
                                var linkedFile = dir.resolve(m.replaceAll(linkPattern));
                                try {
                                    Files.createDirectories(linkedFile.getParent());
                                    Files.createLink(linkedFile, dir.resolve(f));
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                );
            }
        } catch (IndexOutOfBoundsException | PatternSyntaxException e) {
            usage();
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void usage() {
        System.out.println("Usage: tmpren <srcpattern> <linkexpr> [<dir>]");
        System.out.println("Where:");
        System.out.println("\tsrcpattern = regex expression for the source file, potentially containing groups");
        System.out.println("\tlinkexpr = expression for the link file, potentially containing group refs");
        System.out.println("\tdir = optional target dir");

    }
}