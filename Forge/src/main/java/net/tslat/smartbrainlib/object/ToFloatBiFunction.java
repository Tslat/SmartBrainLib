package net.tslat.smartbrainlib.object;

import java.util.function.BiFunction;

/**
 * Represents a function that accepts two arguments and produces a float-valued
 * result.  This is the {@code float}-producing primitive specialization for
 * {@link BiFunction}.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #applyAsFloat(Object, Object)}.
 *
 * @param <T> the type of the first argument to the function
 * @param <U> the type of the second argument to the function
 *
 * @see BiFunction
 */
@FunctionalInterface
public interface ToFloatBiFunction<T, U> {
    /**
     * Applies this function to the given arguments.
     *
     * @param t the first function argument
     * @param u the second function argument
     * @return the function result
     */
    float applyAsFloat(T t, U u);
}
