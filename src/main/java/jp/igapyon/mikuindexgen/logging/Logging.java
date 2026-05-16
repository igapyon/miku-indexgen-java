package jp.igapyon.mikuindexgen.logging;

import jp.igapyon.mikuindexgen.coreapi.IndexgenOptions;
import jp.igapyon.mikuindexgen.coreapi.IndexgenResult;
import jp.igapyon.mikuindexgen.coreapi.IndexgenTimings;

public final class Logging {
    private Logging() {
    }

    public static void logVerboseStart(IndexgenOptions options, String targetPath, String jsonPath, String markdownPath, VerboseLogger logger) {
        logger.log("target=" + targetPath);
        logger.log("output=" + jsonPath);
        if (options.title != null) {
            logger.log("title=" + options.title);
        }
        logger.log("include-ext=" + join(options.includeExtensions));
        logger.log("generator=" + (Boolean.FALSE.equals(options.includeGeneratorMetadata) ? "disabled" : "enabled"));
        if (options.jsonSummaryPaths != null && !options.jsonSummaryPaths.isEmpty()) {
            logger.log("json-summary-path=" + join(options.jsonSummaryPaths));
        }
        logger.log("input-encoding=" + options.inputEncoding);
        logger.log("output-encoding=" + options.outputEncoding);
    }

    public static void logVerboseTimings(IndexgenResult result, IndexgenOptions options, VerboseLogger logger) {
        IndexgenTimings timings = result.timings;
        logger.log("files=" + result.files.size());
        logger.log("timing.subdirs=" + formatDuration(timings.subdirsMs));
        logger.log("timing.collect=" + formatDuration(timings.collectMs));
        logger.log("timing.stat=" + formatDuration(timings.statMs));
        logger.log("timing.readFile=" + formatDuration(timings.readFileMs));
        logger.log("timing.summary=" + formatDuration(timings.summaryMs));
        logger.log("timing.json.stringify=" + formatDuration(timings.jsonStringifyMs));
        logger.log("timing.json.write=" + formatDuration(timings.jsonWriteMs));
        if (options.markdownOutput) {
            logger.log("timing.markdown=" + formatDuration(timings.markdownMs));
        }
        logger.log("timing.total=" + formatDuration(timings.totalMs));
    }

    private static String formatDuration(double durationMs) {
        return String.format(java.util.Locale.ROOT, "%.2fms", durationMs);
    }

    private static String join(java.util.List<String> values) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(values.get(i));
        }
        return builder.toString();
    }
}
