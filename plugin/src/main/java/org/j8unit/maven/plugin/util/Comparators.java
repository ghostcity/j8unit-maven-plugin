package org.j8unit.maven.plugin.util;

import java.util.Comparator;

public enum Comparators {
    ;

    public static final Comparator<Package> PACKAGE_COMPARATOR = (x, y) -> x.getName().compareTo(y.getName());

}
