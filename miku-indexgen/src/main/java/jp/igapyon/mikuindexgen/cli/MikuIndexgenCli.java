package jp.igapyon.mikuindexgen.cli;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import jp.igapyon.mikuindexgen.coreapi.Indexgen;
import jp.igapyon.mikuindexgen.coreapi.IndexgenOptions;
import jp.igapyon.mikuindexgen.coreapi.IndexgenResult;
import jp.igapyon.mikuindexgen.encoding.Encoding;
import jp.igapyon.mikuindexgen.jsonsummary.JsonSummary;
import jp.igapyon.mikuindexgen.model.CliOptions;

public class MikuIndexgenCli {
    private static final String[] DEFAULT_INCLUDE_EXTENSIONS = new String[] { "md", "json" };
    private static final String DEFAULT_TEXT_ENCODING = "utf8";

    public static void main(String[] args) {
        int exitCode = new MikuIndexgenCli().run(args, System.out, System.err);
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    public int run(String[] args, PrintStream out, PrintStream err) {
        try {
            CliOptions cliOptions = parseArgs(args);
            IndexgenResult result = new Indexgen().createIndexes(IndexgenOptions.fromCliOptions(cliOptions));
            for (String log : result.logs) {
                if (isVerboseLog(log)) {
                    err.println(log);
                } else {
                    out.println(log);
                }
            }
            if (result.skipped()) {
                out.println("skip: " + result.skippedOutputPath);
            } else {
                for (java.nio.file.Path generatedPath : result.generatedPaths) {
                    out.println("generated: " + generatedPath);
                }
            }
            out.println("completed: " + result.subdirectories + " subdirectories processed");
            return 0;
        } catch (HelpRequestedException ex) {
            printHelp(out);
            return 0;
        } catch (Exception ex) {
            err.println("error: " + ex.getMessage());
            printHelp(err);
            return 1;
        }
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
        }

        if (options.inputDirectory == null || options.inputDirectory.length() == 0) {
            throw new IllegalArgumentException("Please specify an input directory for --input-directory.");
        }
        return options;
    }

    public static void printHelp(PrintStream out) {
        out.println("Usage:\n"
                + "  miku-indexgen --input-directory <dir> [--output-directory <dir>] [--title \"Docs Index\"] [--markdown] [--no-generator] [--json-summary-path /title,/name] [--no-recursive] [--no-overwrite] [--include-ext md,json] [--input-encoding utf8] [--output-encoding utf8] [--verbose]\n"
                + "\n"
                + "Description:\n"
                + "  Generate a root JSON index that aggregates matching files found under\n"
                + "  the input directory. Output files are written to the input directory by default.\n"
                + "  Supported encodings: utf8, shift_jis\n");
    }

    private static String readRequiredOptionValue(String[] argv, int index, String optionName, String description) {
        if (index + 1 >= argv.length || argv[index + 1] == null || argv[index + 1].length() == 0) {
            throw new IllegalArgumentException("Please specify " + description + " for " + optionName + ".");
        }
        return argv[index + 1];
    }
}
