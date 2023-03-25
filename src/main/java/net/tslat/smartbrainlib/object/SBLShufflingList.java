package net.tslat.smartbrainlib.object;

import com.mojang.datafixers.util.Pair;
import io.netty.util.internal.ThreadLocalRandom;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class SBLShufflingList<T> implements Iterable<T> {
	private final List<WeightedEntry<T>> entries;
	private final ThreadLocalRandom random = ThreadLocalRandom.current();

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

	@Nonnull
	@Override
	public Iterator<T> iterator() {
		final ArrayList<T> al = new ArrayList<>();
		this.entries.forEach(we -> al.add(we.get()));
		
		return new ObjectListIterator<T>() {
			int pointer = 0;
            int last = -1;

            @Override
            public boolean hasNext() {
                return this.pointer < SBLShufflingList.this.entries.size();
            }

            @Override
            public boolean hasPrevious() {
                return this.pointer > 0;
            }

            @Override
            public T next() {
                if (!hasNext())
                    throw new NoSuchElementException();

                return SBLShufflingList.this.entries.get(this.last = this.pointer++).object;
            }

            @Override
            public T previous() {
                if (!hasPrevious())
                    throw new NoSuchElementException();

                return SBLShufflingList.this.entries.get(this.last = --this.pointer).object;
            }

            @Override
            public int nextIndex() {
                return this.pointer;
            }

            @Override
            public int previousIndex() {
                return this.pointer - 1;
            }

            @Override
            public void remove() {
                if (this.last == -1)
                    throw new IllegalStateException();

                SBLShufflingList.this.entries.remove(this.last);

                if (this.last < this.pointer)
                    --this.pointer;

                this.last = -1;
            }

            @Override
            public void forEachRemaining(Consumer<? super T> consumer) {
                while(this.pointer < SBLShufflingList.this.entries.size()) {
                    consumer.accept(SBLShufflingList.this.entries.get(this.last = this.pointer++).object);
                }
            }

            @Override
            public int back(int positions) {
                if (positions < 0)
                    throw new IllegalArgumentException("Argument must not be negative: " + positions);

                int remaining = SBLShufflingList.this.entries.size() - this.pointer;

                if (positions < remaining) {
                    this.pointer -= positions;
                }
                else {
                    positions = remaining;
                    this.pointer = 0;
                }

                this.last = this.pointer;

                return positions;
            }

            @Override
            public int skip(int positions) {
                if (positions < 0)
                    throw new IllegalArgumentException("Argument must not be negative: " + positions);

                int remaining = SBLShufflingList.this.entries.size() - this.pointer;

                if (positions < remaining) {
                    this.pointer += positions;
                }
                else {
                    positions = remaining;
                    this.pointer = SBLShufflingList.this.entries.size();
                }

                this.last = this.pointer - 1;

                return positions;
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
