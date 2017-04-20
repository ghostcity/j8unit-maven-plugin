package org.j8unit.maven.plugin;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Files.write;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.util.ResourceBundle.getBundle;
import static java.util.stream.Collectors.joining;
import static org.apache.maven.plugins.annotations.LifecyclePhase.GENERATE_TEST_SOURCES;
import static org.j8unit.maven.plugin.Messages.Keys.ADD_SOURCE_ROOT;
import static org.j8unit.maven.plugin.Messages.Keys.GENERATE_SOURCE_CODE;
import static org.j8unit.maven.plugin.Messages.Keys.OMIT_SOURCE_ROOT;
import static org.j8unit.maven.plugin.Messages.Keys.SOURCE_ROOT;
import static org.j8unit.maven.plugin.util.Paths.resolve;
import static org.j8unit.maven.plugin.util.Resources.resolveResource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.j8unit.maven.plugin.api.ComfortMojo;
import org.j8unit.maven.plugin.api.CompileClasspathMojo;
import org.j8unit.maven.plugin.api.LoggingMojo;

@Mojo(name = "genesis", defaultPhase = GENERATE_TEST_SOURCES)
public class GenesisMojo
extends AbstractMojo
implements ComfortMojo, CompileClasspathMojo, LoggingMojo {

    private static final ResourceBundle MESSAGES = getBundle(Messages.class.getCanonicalName());

    @Override
    public String message(final CharSequence key) {
        return MESSAGES.getString(key.toString());
    }

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    @Parameter(defaultValue = "${mojo}", required = true, readonly = true)
    private MojoExecution mojo;

    @Override
    public MojoExecution getMojoExecution() {
        return this.mojo;
    }

    @Parameter(property = "j8unit.genesis.skip", defaultValue = "false", required = true)
    private boolean skip;

    @Override
    public boolean skip() {
        return this.skip;
    }

    @Parameter(defaultValue = "${project.compileClasspathElements}", required = true, readonly = true)
    private List<String> compileClasspathElements;

    @Override
    public List<String> getCompileClasspathElements() {
        return this.compileClasspathElements;
    }

    @Parameter(defaultValue = "${project.build.directory}/generated-test-sources/j8unit", required = true)
    private File sourceDirectory;

    @Parameter(defaultValue = "true", required = true)
    private boolean addSourceDirectory;

    @Parameter(defaultValue = "PACKAGE", required = true)
    private RenderStyles style;

    @Override
    public void executeMojo()
    throws MojoFailureException, IOException {
        this.info(this.message(SOURCE_ROOT), this.sourceDirectory);
        createDirectories(this.sourceDirectory.toPath());
        if (this.addSourceDirectory) {
            this.info(this.message(ADD_SOURCE_ROOT), this.sourceDirectory);
            this.project.addTestCompileSourceRoot(this.sourceDirectory.getAbsolutePath());
        } else {
            this.info(this.message(OMIT_SOURCE_ROOT), this.sourceDirectory);
        }
        for (final Entry<Package, List<Class<?>>> entry : this.getCompileClasspathClasses().entrySet()) {
            final Package pakkage = entry.getKey();
            final List<Class<?>> classes = entry.getValue();
            this.info(this.message(GENERATE_SOURCE_CODE), pakkage, classes);
            this.style.render(this.sourceDirectory, pakkage, classes);
        }
    }

    static enum RenderStyles {

        PACKAGE {

            @Override
            public void render(final File sourceDirectory, final Package pakkage, final List<Class<?>> classes)
            throws IOException {
                final Path templateFile = resolveResource(this, J8UNIT_TEMPLATE_NAME);
                final String templateContent = new String(readAllBytes(templateFile), UTF_8);
                final Path targetSourceFile = resolve(sourceDirectory, pakkage).resolve(J8UNIT_FILE_NAME);
                final String namespace = pakkage.getName();
                final String classesUnderTest = classes.stream().map(Class::getCanonicalName).map(n -> n + ".class").collect(joining(", "));
                final String targetContent = format(templateContent, namespace, classesUnderTest);
                createDirectories(targetSourceFile.getParent());
                write(targetSourceFile, targetContent.getBytes(UTF_8), CREATE);
            }

        };

        public abstract void render(File sourceDirectory, Package pakkage, List<Class<?>> classes)
        throws IOException;

        // Move inside enum constant (but currently, build fails by maven-plugin-plugin)
        // (similar to
        // http://stackoverflow.com/questions/38547239/maven-plugin-plugindescriptor-goal-fails-at-the-and-of-file)
        static final String J8UNIT_FILE_NAME = "PackageAPIJ8UnitConformanceTests.java";

        static final String J8UNIT_TEMPLATE_NAME = J8UNIT_FILE_NAME + ".template";

    }

}
