package jp.igapyon.mikuindexgen.cli;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import jp.igapyon.mikuindexgen.coreapi.ExcludeGlob;
import jp.igapyon.mikuindexgen.coreapi.Indexgen;
import jp.igapyon.mikuindexgen.coreapi.IndexgenBatchException;
import jp.igapyon.mikuindexgen.coreapi.IndexgenOptions;
import jp.igapyon.mikuindexgen.coreapi.IndexgenResult;
import jp.igapyon.mikuindexgen.encoding.Encoding;
import jp.igapyon.mikuindexgen.jsonsummary.JsonSummary;
import jp.igapyon.mikuindexgen.model.CliOptions;
import jp.igapyon.mikuindexgen.version.Version;

public class MikuIndexgenCli {
    private static final String[] DEFAULT_INCLUDE_EXTENSIONS = new String[] { "md", "json" };
    private static final String DEFAULT_TEXT_ENCODING = "utf8";

    public static void main(String[] args) {
        int exitCode = new MikuIndexgenCli().run(args, System.out, System.err);
        System.exit(exitCode);
    }

    public int run(String[] args, PrintStream out, PrintStream err) {
        CliOptions cliOptions = null;
        try {
            cliOptions = parseArgs(args);
            IndexgenOptions options = IndexgenOptions.fromCliOptions(cliOptions);
            options.verboseStream = cliOptions.verbose ? err : null;
            IndexgenResult result = new Indexgen().createIndexes(options);
            return printResult(cliOptions, result, out, err);
        } catch (IndexgenBatchException ex) {
            return printResult(cliOptions, ex.getResult(), out, err);
        } catch (HelpRequestedException ex) {
            printHelp(out);
            return 0;
        } catch (VersionRequestedException ex) {
            out.println("miku-indexgen " + Version.VERSION);
            return 0;
        } catch (Exception ex) {
            err.println("error: " + ex.getMessage());
            printHelp(err);
            return 1;
        }
    }

    private int printResult(CliOptions cliOptions, IndexgenResult result, PrintStream out, PrintStream err) {
        for (String log : result.logs) {
            if (isVerboseLog(log)) {
                if (!cliOptions.verbose) {
                    err.println(log);
                }
            } else {
                out.println(log);
            }
        }
        if (result.skipped()) {
            out.println("skip: " + result.skippedOutputPath);
        } else if (!result.outputMessages.isEmpty()) {
            for (String outputMessage : result.outputMessages) {
                out.println(outputMessage);
            }
        } else {
            for (java.nio.file.Path generatedPath : result.generatedPaths) {
                out.println("generated: " + generatedPath);
            }
        }
        if (hasValue(cliOptions.inputParentDirectory)) {
            for (String failure : result.childFailureMessages) {
                err.println("failed: " + failure);
            }
            if (result.failed()) {
                out.println("completed: " + result.childDirectoriesProcessed + " child directories processed, "
                        + result.childDirectoriesFailed + " failed");
                return 1;
            }
            out.println("completed: " + result.childDirectoriesProcessed + " child directories processed");
        } else {
            out.println("completed: " + result.subdirectories + " subdirectories processed");
        }
        return 0;
    }

    private static boolean isVerboseLog(String log) {
        return log != null && log.startsWith("verbose: ");
    }

    public static List<String> parseIncludeExtensions(String value) {
        String[] rawItems = value.split(",");
        LinkedHashSet<String> extensions = new LinkedHashSet<String>();

        for (String rawItem : rawItems) {
            String item = rawItem.trim().toLowerCase();
            if (item.startsWith(".")) {
                item = item.substring(1);
            }
            if (item.length() > 0) {
                extensions.add(item);
            }
        }

        if (extensions.isEmpty()) {
            throw new IllegalArgumentException("Please specify at least one extension for --include-ext.");
        }

        return new ArrayList<String>(extensions);
    }

    public static CliOptions parseArgs(String[] argv) {
        CliOptions options = new CliOptions();
        options.markdownOutput = false;
        options.includeGeneratorMetadata = Boolean.TRUE;
        options.recursive = true;
        options.overwrite = true;
        options.verbose = false;
        options.includeExtensions = new ArrayList<String>();
        for (String extension : DEFAULT_INCLUDE_EXTENSIONS) {
            options.includeExtensions.add(extension);
        }
        options.excludeGlobs = new ArrayList<String>();
        options.inputEncoding = DEFAULT_TEXT_ENCODING;
        options.outputEncoding = DEFAULT_TEXT_ENCODING;

        for (int i = 0; i < argv.length; i++) {
            String arg = argv[i];

            if ("--output-directory".equals(arg)) {
                options.outputDirectory = readRequiredOptionValue(argv, i, "--output-directory", "an output directory");
                i++;
                continue;
            }

            if ("--input-directory".equals(arg)) {
                options.inputDirectory = readRequiredOptionValue(argv, i, "--input-directory", "an input directory");
                i++;
                continue;
            }

            if ("--input-parent-directory".equals(arg)) {
                options.inputParentDirectory = readRequiredOptionValue(argv, i, "--input-parent-directory", "an input parent directory");
                i++;
                continue;
            }

            if ("--refresh-index".equals(arg)) {
                options.refreshIndex = readRequiredOptionValue(argv, i, "--refresh-index", "an index.json path");
                i++;
                continue;
            }

            if ("--title".equals(arg)) {
                options.title = readRequiredOptionValue(argv, i, "--title", "a title");
                i++;
                continue;
            }

            if ("--no-recursive".equals(arg)) {
                options.recursive = false;
                continue;
            }

            if ("--markdown".equals(arg)) {
                options.markdownOutput = true;
                continue;
            }

            if ("--no-generator".equals(arg)) {
                options.includeGeneratorMetadata = Boolean.FALSE;
                continue;
            }

            if ("--json-summary-path".equals(arg)) {
                options.jsonSummaryPaths = JsonSummary.parseJsonSummaryPaths(
                        readRequiredOptionValue(argv, i, "--json-summary-path", "a comma-separated JSON Pointer list"));
                i++;
                continue;
            }

            if ("--no-overwrite".equals(arg)) {
                options.overwrite = false;
                continue;
            }

            if ("--include-ext".equals(arg)) {
                options.includeExtensions = parseIncludeExtensions(
                        readRequiredOptionValue(argv, i, "--include-ext", "a comma-separated extension list"));
                i++;
                continue;
            }

            if ("--exclude-glob".equals(arg)) {
                options.excludeGlobs.add(readRequiredOptionValue(argv, i, "--exclude-glob", "a glob pattern"));
                i++;
                continue;
            }

            if ("--input-encoding".equals(arg)) {
                options.inputEncoding = Encoding.parseEncodingOption(
                        readRequiredOptionValue(argv, i, "--input-encoding", "an encoding"));
                i++;
                continue;
            }

            if ("--output-encoding".equals(arg)) {
                options.outputEncoding = Encoding.parseEncodingOption(
                        readRequiredOptionValue(argv, i, "--output-encoding", "an encoding"));
                i++;
                continue;
            }

            if ("--verbose".equals(arg)) {
                options.verbose = true;
                continue;
            }

            if ("--help".equals(arg) || "-h".equals(arg)) {
                throw new HelpRequestedException();
            }

            if ("--version".equals(arg) || "-v".equals(arg)) {
                throw new VersionRequestedException();
            }

            throw new IllegalArgumentException("Unknown argument: " + arg);
        }

        int inputModes = 0;
        inputModes += hasValue(options.inputDirectory) ? 1 : 0;
        inputModes += hasValue(options.inputParentDirectory) ? 1 : 0;
        inputModes += hasValue(options.refreshIndex) ? 1 : 0;
        if (inputModes > 1) {
            throw new IllegalArgumentException("Specify only one of --input-directory, --input-parent-directory, or --refresh-index.");
        }
        if (inputModes == 0) {
            throw new IllegalArgumentException("Please specify --input-directory, --input-parent-directory, or --refresh-index.");
        }
        options.excludeGlobs = ExcludeGlob.normalizeExcludeGlobPatterns(options.excludeGlobs);
        return options;
    }

    public static void printHelp(PrintStream out) {
        out.println(HelpText.TEXT);
    }

    private static boolean hasValue(String value) {
        return value != null && value.length() > 0;
    }

    private static String readRequiredOptionValue(String[] argv, int index, String optionName, String description) {
        if (index + 1 >= argv.length || argv[index + 1] == null || argv[index + 1].length() == 0) {
            throw new IllegalArgumentException("Please specify " + description + " for " + optionName + ".");
        }
        return argv[index + 1];
    }
}
