package net.tslat.smartbrainlib.library.object;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterators;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

/// A pseudo-[List] implementation that allows for randomly shuffled iteration, accounting for individual weights on entries
///
/// This allows for weight-biased randomised entries in a lightweight iterable wrapper
public class SBLShufflingList<T> implements Iterable<T> {
	protected final List<WeightedEntry<T>> entries;
	protected final RandomSource random = RandomSource.createThreadLocalInstance();

	public SBLShufflingList() {
		this.entries = new ObjectArrayList<>();
	}

	public SBLShufflingList(int size) {
		this.entries = new ObjectArrayList<>(size);
	}

	@SafeVarargs
	public SBLShufflingList(Pair<T, Integer>... entries) {
		this.entries = new ObjectArrayList<>(entries.length);

		for (Pair<T, Integer> entry : entries) {
			this.entries.add(new WeightedEntry<>(entry.getFirst(), entry.getSecond()));
		}
	}

	public SBLShufflingList(Collection<Pair<T, Integer>> entries) {
		this.entries = new ObjectArrayList<>(entries.size());

		for (Pair<T, Integer> entry : entries) {
			this.entries.add(new WeightedEntry<>(entry.getFirst(), entry.getSecond()));
		}
	}

	/// Shuffle the current entries by their weight
	@SuppressWarnings("UnusedReturnValue")
    public SBLShufflingList<T> shuffle() {
		this.entries.forEach(entry -> entry.setShuffledWeight(this.random.nextFloat()));
		this.entries.sort(Comparator.comparingDouble(WeightedEntry::getShuffledWeight));

		return this;
	}

	/// Add a weighted entry to the list
	///
	/// @param entry The value to add to the list
	/// @param weight The comparative weight for this value
	public boolean add(T entry, int weight) {
		return this.entries.add(new WeightedEntry<>(entry, weight));
	}

	public @Nullable T get(int index) {
		return this.entries.get(index).value();
	}

	public int size() {
		return this.entries.size();
	}

	@Override
	public Iterator<T> iterator() {
		return new ObjectIterators.AbstractIndexBasedIterator<>(0, 0) {
			@Override
			protected T get(int location) {
				return SBLShufflingList.this.entries.get(location).value();
			}

			@Override
			protected void remove(int location) {
				SBLShufflingList.this.entries.remove(location);
			}

			@Override
			protected int getMaxPos() {
				return SBLShufflingList.this.entries.size();
			}
		};
	}

	@Override
	public void forEach(Consumer<? super T> action) {
		this.entries.forEach(entry -> action.accept(entry.value()));
	}

	public Stream<T> stream() {
		return this.entries.stream().map(WeightedEntry::value);
	}

	public static class WeightedEntry<T> {
		private final T object;
		private final int weight;
		private double shuffledWeight;

		WeightedEntry(T object, int weight) {
			this.object = object;
			this.weight = weight;
		}

		double getShuffledWeight() {
			return this.shuffledWeight;
		}

		T value() {
			return this.object;
		}

		int weight() {
			return this.weight;
		}

		void setShuffledWeight(float mod) {
			this.shuffledWeight = -Math.pow(mod, 1f / this.weight);
		}

		@Override
		public String toString() {
			return this.object + ":" + this.weight;
		}
	}
}
