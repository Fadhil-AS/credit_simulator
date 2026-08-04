package id.co.bcadigital.credit.cli.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

public class InteractiveLineReader implements LineReader {

    private final BufferedReader reader;
    private final PrintStream out;
    private boolean exhausted;

    public InteractiveLineReader(InputStream in, PrintStream out) {
        this.reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        this.out = out;
    }

    @Override
    public String readLine(String prompt) {
        out.print(prompt);
        out.flush();
        try {
            String line = reader.readLine();
            if (line == null) {
                exhausted = true;
                out.println();
                return null;
            }
            return line;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public boolean hasNext() {
        return !exhausted;
    }

    @Override
    public boolean interactive() {
        return true;
    }
}
