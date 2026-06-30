package net.tslat.smartbrainlib.library.interfaces;

import net.minecraft.util.ToFloatFunction;

import java.util.function.BiFunction;

/// Represents a function that accepts two arguments and produces a float-valued result
///
/// This is the `float`-producing primitive specialization for [BiFunction]
///
/// This is a <a href="package-summary.html">functional interface</a> whose functional method is [#applyAsFloat(Object, Object)].
///
/// @param <T> the type of the first argument to the function
/// @param <U> the type of the second argument to the function
///
/// @see BiFunction
/// @see ToFloatFunction
@FunctionalInterface
public interface ToFloatBiFunction<T, U> {
    /// Applies this function to the given arguments.
    ///
    /// @param t The first function argument
    /// @param u The second function argument
    /// @return The function result
    float applyAsFloat(T t, U u);
}
