package net.tslat.smartbrainlib.library.object.collection;

import it.unimi.dsi.fastutil.objects.ObjectIterators;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
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
		return new ObjectIterators.AbstractIndexBasedListIterator<>(0, startingIndex) {
			@Override
			protected T get(int index) {
				return transformToView(TransformingListView.this.collection.get(index));
			}

			@Override
			protected void add(int index, T element) {
				TransformingListView.this.collection.add(index, transformToBase(element));
			}

			@Override
			protected void set(int index, T element) {
				TransformingListView.this.collection.set(index, transformToBase(element));
			}

			@Override
			protected void remove(int index) {
				TransformingListView.this.collection.remove(index);
			}

			@Override
			protected int getMaxPos() {
				return TransformingListView.this.collection.size();
			}
		};
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		return TransformingListView.of(this.collection.subList(fromIndex, toIndex), this.toViewTransformer, this.toBaseTransformer);
	}
}