package net.tslat.smartbrainlib.library.interfaces;

import java.util.Objects;

/// Triple-argument variant of [java.util.function.Predicate]
///
/// Takes 3 inputs and returns a `boolean`
@FunctionalInterface
public interface TriPredicate<A, B, C> {
	/// Test this predicate's conditions
	boolean test(A a, B b, C c);

	/// Create a new [TriPredicate] that is this predicate, with the `other` predicate added onto the end as an `AND` conditional
	///
	/// Returns true only if both this and the `other` predicate return `true`
	///
	/// @return The new predicate instance
	default TriPredicate<A, B, C> and(TriPredicate<? super A, ? super B, ? super C> other) {
		Objects.requireNonNull(other);

		return (A a, B b, C c) -> test(a, b, c) && other.test(a, b, c);
	}

	/// Create a new [TriPredicate] that is this predicate, negated to return an inverse value (true -> false, false -> true)
	///
	/// The resulting predicate returns true if this predicate normally returns false, and false when this predicate
	/// normally returns true
	///
	/// @return The new predicate instance
	default TriPredicate<A, B, C> negate() {
		return (A a, B b, C c) -> !test(a, b, c);
	}

	/// Create a new [TriPredicate] that is this predicate, with the `other` predicate added onto the end as an `OR` conditional
	///
	/// Returns true only if either this or the `other` predicate return `true`
	///
	/// @return The new predicate instance
	default TriPredicate<A, B, C> or(TriPredicate<? super A, ? super B, ? super C> other) {
		Objects.requireNonNull(other);

		return (A a, B b, C c) -> test(a, b, c) || other.test(a, b, c);
	}
}