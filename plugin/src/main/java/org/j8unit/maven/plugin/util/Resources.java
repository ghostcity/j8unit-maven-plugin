package org.j8unit.maven.plugin.util;

import static java.lang.String.format;
import static java.nio.file.FileSystems.newFileSystem;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyMap;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.MissingResourceException;

public enum Resources {
    ;

    public static abstract interface ResourcePair {

        public abstract Object[] asResourcePair();

    }

    public static <E extends Enum<E> & ResourcePair> Object[][] asResourcePairs(final Class<E> clazz) {
        return stream(clazz.getEnumConstants()).map(c -> c.asResourcePair()).toArray(Object[][]::new);
    }

    public static Path resolveResource(final Object reference, final String name)
    throws MissingResourceException {
        final Class<?> base = reference.getClass();
        final URI uri;
        try {
            uri = base.getResource(name).toURI();
        } catch (final NullPointerException missingResource) {
            final MissingResourceException miss = new MissingResourceException(format("Cannot find resource for %s via %s.", name, base), name, "");
            miss.initCause(missingResource);
            throw miss;
        } catch (final URISyntaxException malformedURL) {
            final MissingResourceException miss = new MissingResourceException(format("Cannot load resource for %s via %s.", name, base), name, "");
            miss.initCause(malformedURL);
            throw miss;
        }
        try {
            return Paths.get(uri);
        } catch (final FileSystemNotFoundException jarFileOrSimilar) {
            try {
                return newFileSystem(uri, emptyMap()).provider().getPath(uri);
            } catch (final IOException uncreatableFileSystem) {
                uncreatableFileSystem.addSuppressed(jarFileOrSimilar);
                final MissingResourceException miss = new MissingResourceException(format("Cannot load resource for %s via %s.", name, base), name, "");
                miss.initCause(uncreatableFileSystem);
                throw miss;
            }
        }
    }

}
