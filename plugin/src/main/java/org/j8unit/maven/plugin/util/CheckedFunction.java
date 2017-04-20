package org.j8unit.maven.plugin.util;

import static java.util.Objects.requireNonNull;
import java.util.function.Function;

@FunctionalInterface
public interface CheckedFunction<T, R>
extends Function<T, R> {

    public abstract R checkedApply(final T t)
    throws Exception;

    @Override
    public default R apply(final T t) {
        try {
            return this.checkedApply(t);
        } catch (final RuntimeException runtime) {
            throw runtime;
        } catch (final Exception checked) {
            return hiddenThrow(checked);
        }
    }

    @SuppressWarnings("unchecked")
    public static <R, E extends Exception> R hiddenThrow(final Exception any)
    throws E {
        throw (E) any;
    }

    @Override
    default <V> CheckedFunction<V, R> compose(final Function<? super V, ? extends T> before) {
        requireNonNull(before);
        return (final V v) -> apply(before.apply(v));
    }

    default <V> CheckedFunction<V, R> compose(final CheckedFunction<? super V, ? extends T> before) {
        requireNonNull(before);
        return (final V v) -> apply(before.apply(v));
    }

    @Override
    default <V> CheckedFunction<T, V> andThen(final Function<? super R, ? extends V> after) {
        requireNonNull(after);
        return (final T t) -> after.apply(apply(t));
    }

    default <V> CheckedFunction<T, V> andThen(final CheckedFunction<? super R, ? extends V> after) {
        requireNonNull(after);
        return (final T t) -> after.apply(apply(t));
    }

    public static <T, R> CheckedFunction<T, R> doIt(final CheckedFunction<T, R> code) {
        return code;
    }

}
