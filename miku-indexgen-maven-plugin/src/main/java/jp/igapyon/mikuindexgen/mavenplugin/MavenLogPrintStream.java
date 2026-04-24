package jp.igapyon.mikuindexgen.mavenplugin;

import java.io.OutputStream;
import java.io.PrintStream;

import org.apache.maven.plugin.logging.Log;

final class MavenLogPrintStream extends PrintStream {
    private static final OutputStream DISCARDING_OUTPUT_STREAM = new OutputStream() {
        @Override
        public void write(int value) {
            // Discard writes not routed through println(String).
        }
    };

    private final Log log;

    MavenLogPrintStream(Log log) {
        super(DISCARDING_OUTPUT_STREAM, true);
        this.log = log;
    }

    @Override
    public void println(String line) {
        log.info(line);
    }
}
