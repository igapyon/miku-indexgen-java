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

@Mojo(name = "index-child-directories", threadSafe = true)
public class MikuIndexgenChildDirectoriesMojo extends AbstractMojo {
    @Parameter(property = "miku-indexgen.inputParentDirectory", required = true)
    private File inputParentDirectory;

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
            getLog().info("completed: " + result.subdirectories + " child directories processed");
        } catch (Exception ex) {
            throw new MojoExecutionException("Failed to run miku-indexgen child-directory batch mode.", ex);
        }
    }

    private static boolean isVerboseLog(String log) {
        return log != null && log.startsWith("verbose: ");
    }

    IndexgenOptions toOptions() {
        IndexgenOptions options = new IndexgenOptions();
        options.inputParentDirectory = inputParentDirectory.getPath();
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

    void setInputParentDirectory(File inputParentDirectory) {
        this.inputParentDirectory = inputParentDirectory;
    }

    void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    void setTitle(String title) {
        this.title = title;
    }

    void setMarkdown(boolean markdown) {
        this.markdown = markdown;
    }

    void setIncludeGeneratorMetadata(boolean includeGeneratorMetadata) {
        this.includeGeneratorMetadata = includeGeneratorMetadata;
    }

    void setJsonSummaryPaths(List<String> jsonSummaryPaths) {
        this.jsonSummaryPaths = copyList(jsonSummaryPaths);
    }

    void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    void setIncludeExtensions(List<String> includeExtensions) {
        this.includeExtensions = copyList(includeExtensions);
    }

    void setInputEncoding(String inputEncoding) {
        this.inputEncoding = inputEncoding;
    }

    void setOutputEncoding(String outputEncoding) {
        this.outputEncoding = outputEncoding;
    }

    void setSkip(boolean skip) {
        this.skip = skip;
    }

    private static List<String> copyList(List<String> values) {
        return values == null ? null : new ArrayList<String>(values);
    }
}
