package net.tslat.smartbrainlib.object.backport;
import java.util.function.BiFunction;

/**
 * BACKPORTED FROM APACHE COMMONS LANG3
 * <br>
 * A function that accepts two arguments and produces a boolean result. This is the {@code boolean}-producing primitive
 * specialization for {@link BiFunction}.
 *
 * @param <T> the type of the first argument to the function.
 * @param <U> the type of the second argument to the function.
 *
 * @see BiFunction
 */
@FunctionalInterface
public interface ToBooleanBiFunction<T, U> {

	/**
	 * Applies this function to the given arguments.
	 *
	 * @param t the first function argument.
	 * @param u the second function argument.
	 * @return the function result.
	 */
	boolean applyAsBoolean(T t, U u);
}