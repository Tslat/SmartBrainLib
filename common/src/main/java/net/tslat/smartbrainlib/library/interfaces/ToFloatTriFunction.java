package net.tslat.smartbrainlib.library.interfaces;

import net.minecraft.util.ToFloatFunction;
import org.apache.commons.lang3.function.TriFunction;

/// Represents a function that accepts three arguments and produces a float-valued result
///
/// This is the `float`-producing primitive specialization for [TriFunction]
///
/// This is a <a href="package-summary.html">functional interface</a> whose functional method is [#applyAsFloat(Object, Object, Object)].
///
/// @param <T> the type of the first argument to the function
/// @param <U> the type of the second argument to the function
/// @param <V> the type of the third argument to the function
///
/// @see TriFunction
/// @see ToFloatFunction
@FunctionalInterface
public interface ToFloatTriFunction<T, U, V> {
    /// Applies this function to the given arguments.
    ///
    /// @param t The first function argument
    /// @param u The second function argument
    /// @param v The third function argument
    /// @return The function result
    float applyAsFloat(T t, U u, V v);
}
