package org.j8unit.maven.plugin.util;

import static java.lang.reflect.Modifier.isPrivate;
import java.util.function.Predicate;

public enum Predicates {
    ;

    public static final Predicate<Class<?>> NOT_PRIVATE = new Predicate<Class<?>>() {

        @Override
        public boolean test(final Class<?> clazz) {
            return (clazz == null) || (!isPrivate(clazz.getModifiers()) && this.test(clazz.getDeclaringClass()));
        }

    };

}
