package net.tslat.smartbrainlib.library.object.collection;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import net.tslat.smartbrainlib.util.RandomUtil;
import org.apache.commons.lang3.mutable.MutableDouble;

import java.util.*;

/// A [List] implementation that allows for randomly shuffled iteration, accounting for individual weights on entries
///
/// This allows for weight-biased randomised entries in a lightweight iterable wrapper
public class WeightedShuffleableList<T> extends TransformingListView<WeightedShuffleableList.WeightedEntry<T>, T> {
	public WeightedShuffleableList() {
		super(new ObjectArrayList<>(), WeightedEntry::value, WeightedEntry::new);
	}

	public WeightedShuffleableList(int size) {
		super(new ObjectArrayList<>(size), WeightedEntry::value, WeightedEntry::new);
	}

	/// Create a new `WeightedShuffleableList` instance from a variable number of unweighted input values
	///
	/// Each entry is assigned a weight value of `1`, but usually you wouldn't add other weighted elements to lists created using this method
	@SafeVarargs
	public static <T> WeightedShuffleableList<T> of(T... values) {
		final WeightedShuffleableList<T> list = new WeightedShuffleableList<>(values.length);

		for (T value : values) {
			list.add(value, 1);
		}

		return list;
	}

	/// Create a new `WeightedShuffleableList` instance from a variable number of weighted input values
	@SafeVarargs
    public static <T> WeightedShuffleableList<T> of(ObjectIntPair<T>... entries) {
		final WeightedShuffleableList<T> list = new WeightedShuffleableList<>(entries.length);

		for (ObjectIntPair<T> entry : entries) {
			list.add(entry.left(), entry.rightInt());
		}

		return list;
	}

	/// Create a new `WeightedShuffleableList` instance by duplicating an existing collection of entries
	@SuppressWarnings("unchecked")
    public static <T> WeightedShuffleableList<T> wrapWeighted(Collection<ObjectIntPair<T>> entries) {
		return of(entries.toArray(ObjectIntPair[]::new));
	}

	/// Create a new `WeightedShuffleableList` instance by duplicating an existing collection of entries
	@SuppressWarnings("unchecked")
    public static <T> WeightedShuffleableList<T> wrapUnweighted(Collection<T> entries) {
		return of((T[])entries.toArray());
	}

	/// Add a weighted element to this list
	public void add(T value, int weight) {
		getBaseCollection().add(new WeightedEntry<>(value, weight));
	}

	//<editor-fold defaultstate="collapsed" desc="<Internal Handling>">
	/// Randomly shuffle the contents of this list, then sort it in order of its randomly assigned order
	@SuppressWarnings("UnusedReturnValue")
    public WeightedShuffleableList<T> shuffle() {
		getBaseCollection().forEach(entry -> entry.setShuffledWeight(RandomUtil.floatValue()));
		getBaseCollection().sort(Comparator.comparingDouble(WeightedEntry::getShuffledWeight));

		return this;
	}

	/// Value-weight container for a single entry in this list
	protected record WeightedEntry<T>(T value, int weight, MutableDouble shuffledWeight) {
		protected WeightedEntry(T value) {
			this(value, 1);
		}

		protected WeightedEntry(T value, int weight) {
			this(value, weight, new MutableDouble(0));
		}

		/// Get the randomised sorting weight value for the current sort operation
		protected double getShuffledWeight() {
			return this.shuffledWeight.doubleValue();
		}

		/// Set the sorting weight value for the next list sort operation
		///
		/// @param mod A random value between 0 and 1, representing a random chance factor
		protected void setShuffledWeight(float mod) {
			this.shuffledWeight.setValue(-Math.pow(mod, 1f / this.weight));
		}

		@Override
		public String toString() {
			return this.value + ":" + this.weight;
		}
	}
	//</editor-fold>
}
