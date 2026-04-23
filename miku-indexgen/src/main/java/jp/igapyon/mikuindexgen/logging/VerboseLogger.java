package jp.igapyon.mikuindexgen.logging;

import java.util.ArrayList;
import java.util.List;

public class VerboseLogger {
    private final boolean enabled;
    private final List<String> logs = new ArrayList<String>();

    public VerboseLogger(boolean enabled) {
        this.enabled = enabled;
    }

    public void log(String message) {
        if (enabled) {
            logs.add("verbose: " + message);
        }
    }

    public List<String> getLogs() {
        return new ArrayList<String>(logs);
    }
}
