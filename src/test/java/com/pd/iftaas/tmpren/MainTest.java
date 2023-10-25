package com.pd.iftaas.tmpren;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import picocli.CommandLine;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {


    @Test
    void testHelp() {
        var out = System.out;
        var err = System.err;

        assertEquals(0, run("-h"));

        System.setOut(Mockito.mock(PrintStream.class));
        System.setErr(Mockito.mock(PrintStream.class));
        assertEquals(0, run("--help"));
        assertEquals(2, run("-h", "--help")); // duplicate option
        System.setOut(out);
        System.setErr(err);

    }

    @Test
    void testVersion() {
        assertEquals(0, new CommandLine(new Main()).execute("-V"));
        assertEquals(0, new CommandLine(new Main()).execute("--version"));
    }

    private static int run(String... args){
        return new CommandLine(new Main()).execute(args);
    }

    @Test
    void testWithDefaults() throws IOException {
        var dir = Files.createTempDirectory("tmpren");

        var funds = dir.resolve("funds");
        var dates = dir.resolve("dates");
        String fundName = "12300";
        var fund123 = funds.resolve(fundName);
        String date = "20230107";
        var date20230107 = dates.resolve(date);

        var file = Path.of("PD_IFTAAS_HI_%s_%s.xml".formatted(fundName, date));
        var srcFile = Files.createFile(dir.resolve(file));
        Files.writeString(srcFile, "Hello World", StandardOpenOption.APPEND);

        assertAll(
                () -> assertEquals(0, run("--path", dir.toString())),
                () -> assertTrue(Files.exists(funds)),
                () -> assertTrue(Files.exists(dates)),
                () -> assertTrue(Files.exists(fund123)),
                () -> assertTrue(Files.isDirectory(fund123)),
                () -> assertTrue(Files.exists(date20230107)),
                () -> assertTrue(Files.isDirectory(date20230107)),
                () -> assertTrue(Files.isRegularFile(fund123.resolve(file))),
                () -> assertTrue(Files.isRegularFile(date20230107.resolve(file)))
        );
        // tidy up...
        removeRecursively(dir);
    }

    private static void removeRecursively(Path dir) {
        try{
            Files.list(dir).filter(Files::isDirectory).forEach(MainTest::removeRecursively);
            Files.list(dir).filter(Files::isRegularFile).forEach(MainTest::rm);
            Files.delete(dir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void rm(Path file) {
        try {
            Files.delete(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
