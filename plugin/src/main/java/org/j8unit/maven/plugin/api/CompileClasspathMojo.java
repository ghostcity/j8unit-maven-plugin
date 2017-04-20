package org.j8unit.maven.plugin.api;

import static java.lang.Thread.currentThread;
import static java.util.Collections.singleton;
import static java.util.Optional.ofNullable;
import static java.util.UUID.randomUUID;
import static java.util.function.UnaryOperator.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static javax.tools.JavaFileObject.Kind.CLASS;
import static javax.tools.StandardLocation.locationFor;
import static javax.tools.ToolProvider.getSystemJavaCompiler;
import static org.j8unit.maven.plugin.Messages.Keys.MISSING_COMPILER;
import static org.j8unit.maven.plugin.api.J8UnitMojoFailureException.onDemandMojoFailure;
import static org.j8unit.maven.plugin.util.Comparators.PACKAGE_COMPARATOR;
import static org.j8unit.maven.plugin.util.Functions.CheckedFunction.doIt;
import static org.j8unit.maven.plugin.util.Lists.map;
import static org.j8unit.maven.plugin.util.Predicates.NOT_PRIVATE;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.StreamSupport;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Extension of the {@link Mojo} interface providing default class-path features.
 */
public abstract interface CompileClasspathMojo
extends Mojo {

    /**
     * Returns a message (pattern) for the given message key.
     *
     * @param key
     *            the message's identifier
     * @return the according message (pattern)
     */
    public abstract String message(final CharSequence key);

    /**
     * Returns the class-path folders containing all compiled source artifacts of the current project.
     *
     * @return the class-path folders containing all compiled source artifacts of the current project
     */
    public abstract List<String> getCompileClasspathElements();

    /**
     * Returns a file manager that is prepares the given {@code location} to map the
     * {@linkplain #getCompileClasspathElements() current project's class-path folders}.
     *
     * @param location
     *            the location to use for mapping
     * @return a file manager that is prepares the given {@code location} to map the current project's class-path
     *         folders
     * @throws MojoFailureException
     *             if no Java compiler is provided at Mojo runtime
     * @throws IOException
     *             if {@code location} {@linkplain Location#isOutputLocation()is an output location} and
     *             {@linkplain #getCompileClasspathElements() current project's class-path folders} do not represent
     *             existing directories
     */
    public default StandardJavaFileManager getCompileClasspathJavaFileManager(final Location location)
    throws MojoFailureException, IOException {
        final JavaCompiler compiler = ofNullable(getSystemJavaCompiler()).orElseThrow(onDemandMojoFailure(this.message(MISSING_COMPILER)));
        final StandardJavaFileManager manager = compiler.getStandardFileManager(new MojoDiagnosticListener<>(this), null, null);
        manager.setLocation(location, map(this.getCompileClasspathElements(), File::new));
        return manager;
    }

    /**
     * Returns a {@linkplain URLClassLoader class loader} that includes the {@linkplain #getCompileClasspathElements()
     * current project's class-path folders} when searching for classes and resources.
     *
     * @return a class loader specific to the current project
     */
    public default URLClassLoader getCompileClasspathClassLoader() {
        final URL[] urls = this.getCompileClasspathElements().stream().map(File::new).map(File::toURI).map(doIt(URI::toURL)).toArray(URL[]::new);
        return new URLClassLoader(urls, currentThread().getContextClassLoader());
    }

    /**
     * Returns a map containing all compiled Java source artifacts (a.k.a. classes) of the current project. The classes
     * are grouped by its package.
     *
     * @return a map containing all compiled Java source artifacts (a.k.a. classes) of the current project
     * @throws MojoFailureException
     *             if no Java compiler is provided at Mojo runtime
     * @throws IOException
     *             if an I/O error occurs while accessing/loading class-path folders or the class files
     */
    public default Map<Package, List<Class<?>>> getCompileClasspathClasses()
    throws MojoFailureException, IOException {
        final Location location = locationFor(randomUUID().toString());
        final StandardJavaFileManager manager = this.getCompileClasspathJavaFileManager(location);
        final ClassLoader loader = this.getCompileClasspathClassLoader();
        final Iterable<JavaFileObject> javaFiles = manager.list(location, "", singleton(CLASS), true);
        return StreamSupport.stream(javaFiles.spliterator(), false) //
                            .map(j -> manager.inferBinaryName(location, j)) //
                            .map(doIt(loader::loadClass)) //
                            .filter(NOT_PRIVATE) //
                            .collect(groupingBy(Class::getPackage, () -> new TreeMap<>(PACKAGE_COMPARATOR), mapping(identity(), toList())));
    }

}
