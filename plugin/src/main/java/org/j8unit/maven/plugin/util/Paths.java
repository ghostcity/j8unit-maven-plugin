package org.j8unit.maven.plugin.util;

import java.io.File;
import java.nio.file.Path;

public enum Paths {
    ;

    public static Path resolve(final File base, final Package pakkage) {
        return java.nio.file.Paths.get(base.getPath(), pakkage.getName().split("\\."));
    }

}
