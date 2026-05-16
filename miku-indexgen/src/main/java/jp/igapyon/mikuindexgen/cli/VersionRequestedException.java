package jp.igapyon.mikuindexgen.cli;

public class VersionRequestedException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public VersionRequestedException() {
        super("Version requested.");
    }
}
