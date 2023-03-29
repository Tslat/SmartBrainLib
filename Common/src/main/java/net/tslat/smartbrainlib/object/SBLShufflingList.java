package net.tslat.smartbrainlib.object;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.AbstractObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
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

	@NotNull
	@Override
	public Iterator<T> iterator() {
		return new AbstractIndexBasedIterator(0, 0) {
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

	/**
	 * BACKPORTED FROM FastUtil 8.5.9
	 * @param <K>
	 */
	public static abstract class AbstractIndexBasedIterator<K> extends AbstractObjectIterator<K> {
		/**
		 * The minimum pos can be, and is the logical start of the "range". Usually set to the initialPos
		 * unless it is a ListIterator, in which case it can vary.
		 *
		 * There isn't any way for a range to shift its beginning like the end can (through
		 * {@link #remove}), so this is final.
		 */
		protected final int minPos;
		/**
		 * The current position index, the index of the item to be returned after the next call to
		 * {@link #next()}.
		 *
		 * <p>
		 * This value will be between {@code minPos} and {@link #getMaxPos()} (exclusive) (on a best effort,
		 * so concurrent structural modifications outside this iterator may cause this to be violated, but
		 * that usually invalidates iterators anyways). Thus {@code pos} being {@code minPos + 2} would mean
		 * {@link #next()} was called twice and the next call will return the third element of this
		 * iterator.
		 */
		protected int pos;
		/**
		 * The last returned index by a call to {@link #next} or, if a list-iterator,
		 * {@link java.util.ListIterator#previous().
		 *
		 * It is &minus;1 if no such call has occurred or a mutation has occurred through this iterator and
		 * no advancement has been done.
		 */
		protected int lastReturned;

		protected AbstractIndexBasedIterator(int minPos, int initialPos) {
			this.minPos = minPos;
			this.pos = initialPos;
		}

		// When you implement these, you should probably declare them final to encourage the JVM to inline
		// them.
		/**
		 * Get the item corresponding to the given index location.
		 *
		 * <p>
		 * Do <em>not</em> advance {@link #pos} in this method; the default {@code next} method takes care
		 * of this.
		 *
		 * <p>
		 * The {@code location} given will be between {@code minPos} and {@link #getMaxPos()} (exclusive).
		 * Thus, a {@code location} of {@code minPos + 2} would mean {@link #next()} was called twice and
		 * this method should return what the next call to {@link #next()} should return.
		 */
		protected abstract K get(int location);

		/**
		 * Remove the item at the given index.
		 *
		 * <p>
		 * Do <em>not</em> modify {@link #pos} in this method; the default {@code #remove()} method takes
		 * care of this.
		 *
		 * <p>
		 * This method should also do what is needed to track the change to the {@link #getMaxPos}. Usually
		 * this is accomplished by having this method call the parent {@link Collection}'s appropriate
		 * remove method, and having {@link #getMaxPos} track the parent {@linkplain Collection#size()
		 * collection's size}.
		 */
		protected abstract void remove(int location);

		/**
		 * The maximum pos can be, and is the logical end (exclusive) of the "range".
		 *
		 * <p>
		 * If pos is equal to the return of this method, this means the last element has been returned and
		 * the next call to {@link #next()} will throw.
		 *
		 * <p>
		 * Usually set return the parent {@linkplain Collection#size() collection's size}, but does not have
		 * to be (for example, sublists and subranges).
		 */
		protected abstract int getMaxPos();

		@Override
		public boolean hasNext() {
			return pos < getMaxPos();
		}

		@Override
		public K next() {
			if (!hasNext()) throw new NoSuchElementException();
			return get(lastReturned = pos++);
		}

		@Override
		public void remove() {
			if (lastReturned == -1) throw new IllegalStateException();
			remove(lastReturned);
			/* If the last operation was a next(), we are removing an element *before* us, and we must decrease pos correspondingly. */
			if (lastReturned < pos) pos--;
			lastReturned = -1;
		}

		@Override
		public void forEachRemaining(final Consumer<? super K> action) {
			while (pos < getMaxPos()) {
				action.accept(get(lastReturned = pos++));
			}
		}

		@Override
		public int skip(int n) {
			if (n < 0) throw new IllegalArgumentException("Argument must be nonnegative: " + n);
			final int max = getMaxPos();
			final int remaining = max - pos;
			if (n < remaining) {
				pos += n;
			} else {
				n = remaining;
				pos = max;
			}
			lastReturned = pos - 1;
			return n;
		}
	}
}
