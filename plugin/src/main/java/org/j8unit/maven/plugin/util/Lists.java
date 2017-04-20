package org.j8unit.maven.plugin.util;

import static java.util.stream.Collectors.toList;
import java.util.List;
import java.util.function.Function;

public enum Lists {
    ;

    public static <T, R> List<R> map(final List<T> from, final Function<? super T, ? extends R> mapper) {
        return from.stream().map(mapper).collect(toList());
    }

}
