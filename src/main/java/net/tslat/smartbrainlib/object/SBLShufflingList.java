package net.tslat.smartbrainlib.object;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.datafixers.util.Pair;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterators;
import net.minecraft.util.RandomSource;

public class SBLShufflingList<T> implements Iterable<T> {
	private final List<WeightedEntry<T>> entries;
	private final RandomSource random = RandomSource.createNewThreadLocalInstance();

	public SBLShufflingList() {
		this.entries = new ObjectArrayList<>();
	}

	public SBLShufflingList(int size) {
		this.entries = new ObjectArrayList<>(size);
	}

	public SBLShufflingList(Pair<T, Integer>... entries) {
		this.entries = new ObjectArrayList<>(entries.length);

		for (Pair<T, Integer> entry : entries) {
			this.entries.add(new WeightedEntry<>(entry.getFirst(), entry.getSecond()));
		}
	}

	public SBLShufflingList<T> shuffle() {
		this.entries.forEach(entry -> entry.setShuffledWeight(this.random.nextFloat()));
		this.entries.sort(Comparator.comparingDouble(WeightedEntry::getShuffledWeight));

		return this;
	}

	public boolean add(T entry, int weight) {
		return this.entries.add(new WeightedEntry<>(entry, weight));
	}

	@Nullable
	public T get(int index) {
		return this.entries.get(index).get();
	}

	@NotNull
	@Override
	public Iterator<T> iterator() {
		return new ObjectIterators.AbstractIndexBasedIterator<>(0, 0) {
			@Override
			protected T get(int location) {
				return SBLShufflingList.this.entries.get(location).get();
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
		this.entries.forEach(entry -> action.accept(entry.get()));
	}

	public Stream<T> stream() {
		return this.entries.stream().map(WeightedEntry::get);
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

		T get() {
			return this.object;
		}

		int getWeight() {
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
