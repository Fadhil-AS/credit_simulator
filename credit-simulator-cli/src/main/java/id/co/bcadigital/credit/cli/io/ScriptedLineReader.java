package id.co.bcadigital.credit.cli.io;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class ScriptedLineReader implements LineReader {

    private final Deque<String> lines = new ArrayDeque<>();
    private final PrintStream out;

    public ScriptedLineReader(List<String> lines, PrintStream out) {
        this.out = out;
        for (String line : lines) {
            if (!line.isBlank() && !line.trim().startsWith("#")) {
                this.lines.add(line.trim());
            }
        }
    }

    public static ScriptedLineReader fromFile(Path path, PrintStream out) {
        try {
            return new ScriptedLineReader(Files.readAllLines(path, StandardCharsets.UTF_8), out);
        } catch (IOException e) {
            throw new UncheckedIOException("Tidak dapat membaca file input: " + path, e);
        }
    }

    @Override
    public String readLine(String prompt) {
        if (lines.isEmpty()) {
            return null;
        }
        String line = lines.poll();
        out.println(prompt + line);
        return line;
    }

    @Override
    public boolean hasNext() {
        return !lines.isEmpty();
    }

    @Override
    public boolean interactive() {
        return false;
    }
}
