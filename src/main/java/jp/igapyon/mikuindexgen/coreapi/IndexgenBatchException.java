package jp.igapyon.mikuindexgen.coreapi;

import java.io.IOException;

public class IndexgenBatchException extends IOException {
    private static final long serialVersionUID = 1L;

    private final IndexgenResult result;

    public IndexgenBatchException(IndexgenResult result) {
        super(result.childDirectoriesFailed + " child directories failed.");
        this.result = result;
    }

    public IndexgenResult getResult() {
        return result;
    }
}
