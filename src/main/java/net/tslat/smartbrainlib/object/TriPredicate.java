package net.tslat.smartbrainlib.object;

import java.util.Objects;

/**
 * Triple-argument variant of {@link java.util.function.Predicate}
 */
@FunctionalInterface
public interface TriPredicate<A, B, C> {
	boolean test(A a, B b, C c);

	default TriPredicate<A, B, C> and(TriPredicate<? super A, ? super B, ? super C> other) {
		Objects.requireNonNull(other);

		return (A a, B b, C c) -> test(a, b, c) && other.test(a, b, c);
	}

	default TriPredicate<A, B, C> negate() {
		return (A a, B b, C c) -> !test(a, b, c);
	}

	default TriPredicate<A, B, C> or(TriPredicate<? super A, ? super B, ? super C> other) {
		Objects.requireNonNull(other);

		return (A a, B b, C c) -> test(a, b, c) || other.test(a, b, c);
	}
}
