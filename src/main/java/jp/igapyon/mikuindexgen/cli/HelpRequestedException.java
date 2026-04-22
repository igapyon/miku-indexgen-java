package jp.igapyon.mikuindexgen.cli;

public class HelpRequestedException extends RuntimeException {
    public HelpRequestedException() {
        super("Help requested.");
    }
}
