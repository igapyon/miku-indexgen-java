package jp.igapyon.mikuindexgen.mavenplugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import jp.igapyon.mikuindexgen.coreapi.Indexgen;
import jp.igapyon.mikuindexgen.coreapi.IndexgenOptions;
import jp.igapyon.mikuindexgen.coreapi.IndexgenResult;

@Mojo(name = "index", threadSafe = true)
public class MikuIndexgenMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project.basedir}", property = "miku-indexgen.inputDirectory")
    private File inputDirectory;

    @Parameter(property = "miku-indexgen.outputDirectory")
    private File outputDirectory;

    @Parameter(property = "miku-indexgen.title")
    private String title;

    @Parameter(defaultValue = "false", property = "miku-indexgen.markdown")
    private boolean markdown;

    @Parameter(defaultValue = "true", property = "miku-indexgen.includeGeneratorMetadata")
    private boolean includeGeneratorMetadata = true;

    @Parameter
    private List<String> jsonSummaryPaths;

    @Parameter(defaultValue = "true", property = "miku-indexgen.recursive")
    private boolean recursive = true;

    @Parameter(defaultValue = "true", property = "miku-indexgen.overwrite")
    private boolean overwrite = true;

    @Parameter(defaultValue = "false", property = "miku-indexgen.verbose")
    private boolean verbose;

    @Parameter
    private List<String> includeExtensions;

    @Parameter(defaultValue = "utf8", property = "miku-indexgen.inputEncoding")
    private String inputEncoding = "utf8";

    @Parameter(defaultValue = "utf8", property = "miku-indexgen.outputEncoding")
    private String outputEncoding = "utf8";

    @Parameter(defaultValue = "false", property = "miku-indexgen.skip")
    private boolean skip;

    @Override
    public void execute() throws MojoExecutionException {
        if (skip) {
            getLog().info("miku-indexgen skipped.");
            return;
        }

        try {
            IndexgenOptions options = toOptions();
            options.verboseStream = verbose ? new MavenLogPrintStream(getLog()) : null;
            IndexgenResult result = new Indexgen().createIndexes(options);
            for (String log : result.logs) {
                if (options.verboseStream == null || !isVerboseLog(log)) {
                    getLog().info(log);
                }
            }
            if (result.skipped()) {
                getLog().info("skip: " + result.skippedOutputPath);
            } else {
                for (java.nio.file.Path generatedPath : result.generatedPaths) {
                    getLog().info("generated: " + generatedPath);
                }
            }
            getLog().info("completed: " + result.subdirectories + " subdirectories processed");
        } catch (Exception ex) {
            throw new MojoExecutionException("Failed to run miku-indexgen.", ex);
        }
    }

    private static boolean isVerboseLog(String log) {
        return log != null && log.startsWith("verbose: ");
    }

    IndexgenOptions toOptions() {
        IndexgenOptions options = new IndexgenOptions();
        options.inputDirectory = inputDirectory.getPath();
        options.outputDirectory = outputDirectory == null ? null : outputDirectory.getPath();
        options.title = title;
        options.markdownOutput = markdown;
        options.includeGeneratorMetadata = Boolean.valueOf(includeGeneratorMetadata);
        options.jsonSummaryPaths = copyList(jsonSummaryPaths);
        options.recursive = recursive;
        options.overwrite = overwrite;
        options.verbose = verbose;
        if (includeExtensions != null && !includeExtensions.isEmpty()) {
            options.includeExtensions = copyList(includeExtensions);
        }
        options.inputEncoding = inputEncoding;
        options.outputEncoding = outputEncoding;
        return options;
    }

    public void setInputDirectory(File inputDirectory) {
        this.inputDirectory = inputDirectory;
    }

    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMarkdown(boolean markdown) {
        this.markdown = markdown;
    }

    public void setIncludeGeneratorMetadata(boolean includeGeneratorMetadata) {
        this.includeGeneratorMetadata = includeGeneratorMetadata;
    }

    public void setJsonSummaryPaths(List<String> jsonSummaryPaths) {
        this.jsonSummaryPaths = copyList(jsonSummaryPaths);
    }

    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public void setIncludeExtensions(List<String> includeExtensions) {
        this.includeExtensions = copyList(includeExtensions);
    }

    public void setInputEncoding(String inputEncoding) {
        this.inputEncoding = inputEncoding;
    }

    public void setOutputEncoding(String outputEncoding) {
        this.outputEncoding = outputEncoding;
    }

    public void setSkip(boolean skip) {
        this.skip = skip;
    }

    private static List<String> copyList(List<String> values) {
        return values == null ? null : new ArrayList<String>(values);
    }
}
