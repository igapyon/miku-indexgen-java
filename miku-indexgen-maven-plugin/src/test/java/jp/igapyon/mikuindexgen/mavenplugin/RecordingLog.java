package jp.igapyon.mikuindexgen.mavenplugin;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.logging.Log;

final class RecordingLog implements Log {
    final List<String> infoLines = new ArrayList<String>();

    @Override
    public boolean isDebugEnabled() {
        return true;
    }

    @Override
    public void debug(CharSequence content) {
    }

    @Override
    public void debug(CharSequence content, Throwable error) {
    }

    @Override
    public void debug(Throwable error) {
    }

    @Override
    public boolean isInfoEnabled() {
        return true;
    }

    @Override
    public void info(CharSequence content) {
        infoLines.add(content == null ? null : content.toString());
    }

    @Override
    public void info(CharSequence content, Throwable error) {
        info(content);
    }

    @Override
    public void info(Throwable error) {
    }

    @Override
    public boolean isWarnEnabled() {
        return true;
    }

    @Override
    public void warn(CharSequence content) {
    }

    @Override
    public void warn(CharSequence content, Throwable error) {
    }

    @Override
    public void warn(Throwable error) {
    }

    @Override
    public boolean isErrorEnabled() {
        return true;
    }

    @Override
    public void error(CharSequence content) {
    }

    @Override
    public void error(CharSequence content, Throwable error) {
    }

    @Override
    public void error(Throwable error) {
    }

    int countInfoLine(String expected) {
        int count = 0;
        for (String line : infoLines) {
            if (expected.equals(line)) {
                count++;
            }
        }
        return count;
    }
}
