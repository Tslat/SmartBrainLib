package net.tslat.smartbrainlib.library.object.collection;

import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

/// Implementation of a view of another [List], transforming elements between the two formats
///
/// The base list must have a 1:1 relationship for elements
/// Changes to the base list are immediately present in the view, and vice versa
public class TransformingListView<R, T> extends TransformingCollectionView<List<R>, R, T> implements List<T> {
	protected TransformingListView(List<R> baseList, Function<R, T> toViewTransformer, @Nullable Function<T, R> toBaseTransformer) {
		super(baseList, toViewTransformer, toBaseTransformer);
	}

	/// Create a new instance of
	public static <R, T> TransformingListView<R, T> of(List<R> baseList, Function<R, T> toViewTransformer, @Nullable Function<T, R> toBaseTransformer) {
		return new TransformingListView<>(baseList, toViewTransformer, toBaseTransformer);
	}

	@Override
	public Iterator<T> iterator() {
		return listIterator();
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> collection) {
		for (T element : collection) {
			add(index++, element);
		}

		return !collection.isEmpty();
	}

	@Override
	public T get(int index) {
		return transformToView(this.collection.get(index));
	}

	@Override
	public T set(int index, T element) {
		return transformToView(this.collection.set(index, transformToBase(element)));
	}

	@Override
	public void add(int index, T element) {
		this.collection.add(index, transformToBase(element));
	}

	@Override
	public T remove(int index) {
		return transformToView(this.collection.remove(index));
	}

	@Override
	public int indexOf(Object object) {
		for (int i = 0; i < this.collection.size(); i++) {
			if (object.equals(transformToView(this.collection.get(i))))
				return i;
		}

		return -1;
	}

	@Override
	public int lastIndexOf(Object object) {
		for (int i = this.collection.size() - 1; i >= 0; i--) {
			if (object.equals(transformToView(this.collection.get(i))))
				return i;
		}

		return -1;
	}

	@Override
	public ListIterator<T> listIterator() {
		return listIterator(0);
	}

	@Override
	public ListIterator<T> listIterator(int startingIndex) {
		return new ListIterator<>() {
			int index = startingIndex;
			int last = -1;

			@Override
			public boolean hasNext() {
				return this.index < TransformingListView.this.collection.size();
			}

			@Override
			public boolean hasPrevious() {
				return this.index > 0;
			}

			@Override
			public T next() {
				if (!hasNext())
					throw new NoSuchElementException();

				return transformToView(TransformingListView.this.collection.get(this.last = this.index++));
			}

			@Override
			public void remove() {
				if (this.last == -1)
					throw new IllegalStateException();

				TransformingListView.this.collection.remove(this.last);
				this.last = -1;
			}

			@Override
			public T previous() {
				if (!hasPrevious())
					throw new NoSuchElementException();

				return transformToView(TransformingListView.this.collection.get(--this.index));
			}

			@Override
			public int nextIndex() {
				return this.index;
			}

			@Override
			public int previousIndex() {
				return this.index - 1;
			}

			@Override
			public void set(T element) {
				if (this.last == -1)
					throw new IllegalStateException();

				TransformingListView.this.collection.set(this.last, transformToBase(element));
			}

			@Override
			public void add(T element) {
				TransformingListView.this.collection.add(this.index++, transformToBase(element));
				this.last = -1;
			}
		};
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		return TransformingListView.of(this.collection.subList(fromIndex, toIndex), this.toViewTransformer, this.toBaseTransformer);
	}
}