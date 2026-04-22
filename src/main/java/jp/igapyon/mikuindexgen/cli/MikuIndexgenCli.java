package jp.igapyon.mikuindexgen.cli;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

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
            parseArgs(args);
            err.println("error: index generation is not implemented yet.");
            return 1;
        } catch (HelpRequestedException ex) {
            printHelp(out);
            return 0;
        } catch (RuntimeException ex) {
            err.println("error: " + ex.getMessage());
            printHelp(err);
            return 1;
        }
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
        List<String> positional = new ArrayList<String>();
        CliOptions options = new CliOptions();
        options.outputFileName = "index.json";
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

            if ("--output".equals(arg) || "-o".equals(arg)) {
                options.outputFileName = readRequiredOptionValue(argv, i, "--output", "a file name");
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

            positional.add(arg);
        }

        if (positional.isEmpty()) {
            throw new IllegalArgumentException("Please specify a target directory.");
        }

        options.targetDir = positional.get(0);
        return options;
    }

    public static void printHelp(PrintStream out) {
        out.println("Usage:\n"
                + "  miku-indexgen <targetDir> [--output index.json] [--title \"Docs Index\"] [--markdown] [--no-generator] [--json-summary-path /title,/name] [--no-recursive] [--no-overwrite] [--include-ext md,json] [--input-encoding utf8] [--output-encoding utf8] [--verbose]\n"
                + "\n"
                + "Description:\n"
                + "  Generate a root JSON index that aggregates matching files found under\n"
                + "  the target directory. Markdown output is optional.\n"
                + "  Supported encodings: utf8, shift_jis\n");
    }

    private static String readRequiredOptionValue(String[] argv, int index, String optionName, String description) {
        if (index + 1 >= argv.length || argv[index + 1] == null || argv[index + 1].length() == 0) {
            throw new IllegalArgumentException("Please specify " + description + " for " + optionName + ".");
        }
        return argv[index + 1];
    }
}
