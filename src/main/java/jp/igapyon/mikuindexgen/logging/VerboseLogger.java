package jp.igapyon.mikuindexgen.logging;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class VerboseLogger {
    private final boolean enabled;
    private final PrintStream stream;
    private final List<String> logs = new ArrayList<String>();

    public VerboseLogger(boolean enabled) {
        this(enabled, null);
    }

    public VerboseLogger(boolean enabled, PrintStream stream) {
        this.enabled = enabled;
        this.stream = stream;
    }

    public void log(String message) {
        if (enabled) {
            String line = "verbose: " + message;
            logs.add(line);
            if (stream != null) {
                stream.println(line);
            }
        }
    }

    public List<String> getLogs() {
        return new ArrayList<String>(logs);
    }
}
