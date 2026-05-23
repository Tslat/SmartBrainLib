package net.tslat.smartbrainlib.library.object.collection;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Function;

/// Implementation of a view of another [Collection], transforming elements between the two formats
///
/// The base list must have a 1:1 relationship for elements
/// Changes to the base list are immediately present in the view, and vice versa
public class TransformingCollectionView<C extends Collection<R>, R, T> implements Collection<T> {
	protected final C collection;
	protected final Function<R, T> toViewTransformer;
	protected final @Nullable Function<T, R> toBaseTransformer;

	protected TransformingCollectionView(C baseCollection, Function<R, T> toViewTransformer, @Nullable Function<T, R> toBaseTransformer) {
		this.collection = baseCollection;
		this.toViewTransformer = toViewTransformer;
		this.toBaseTransformer = toBaseTransformer;
	}

	/// Create a new instance of
	@SuppressWarnings("SameParameterValue")
	public static <C extends Collection<R>, R, T> TransformingCollectionView<C, R, T> of(C baseCollection, Function<R, T> toViewTransformer, @Nullable Function<T, R> toBaseTransformer) {
		return new TransformingCollectionView<>(baseCollection, toViewTransformer, toBaseTransformer);
	}

	/// Transform an element of the base [Collection]'s type to the collection view's type
	@Contract("null->null;!null->!null")
	@Nullable T transformToView(@Nullable R baseElement) {
		return baseElement == null ? null : this.toViewTransformer.apply(baseElement);
	}

	/// Transform an element of the [Collection] view's type to the base collection's type<br/>
	/// **<u>NOTE:</u>** Must only be used for inserting elements into the base collection. Contains checks should [transform in reverse][#transformToView] instead
	///
	/// If the view is not meant to support adding elements, then this will throw a [UnsupportedOperationException]
	@Contract("null->null;!null->!null")
	@Nullable R transformToBase(@Nullable T viewElement) {
		if (this.toBaseTransformer == null)
			throw new UnsupportedOperationException("Attempting to add an element to a collection which does not support additive modification!");

		return viewElement == null ? null : this.toBaseTransformer.apply(viewElement);
	}

	protected C getBaseCollection() {
		return this.collection;
	}

	@Override
	public int size() {
		return this.collection.size();
	}

	@Override
	public boolean isEmpty() {
		return this.collection.isEmpty();
	}

	@Override
	public boolean contains(Object obj) {
		if (this.toBaseTransformer != null)
			//noinspection unchecked
			return this.collection.contains(transformToBase((T)obj));

		for (R element : this.collection) {
			if (obj.equals(transformToView(element)))
				return true;
		}

		return false;
	}

	@Override
	public Iterator<T> iterator() {
		return new Iterator<>() {
			final Iterator<R> baseIterator = TransformingCollectionView.this.collection.iterator();

			@Override
			public boolean hasNext() {
				return this.baseIterator.hasNext();
			}

			@Override
			public T next() {
				return transformToView(this.baseIterator.next());
			}

			@Override
			public void remove() {
				this.baseIterator.remove();
			}
		};
	}

	@Override
	public Object[] toArray() {
		return this.collection.toArray();
	}

	@Override
	public <T1> T1[] toArray(T1[] a) {
		return this.collection.toArray(a);
	}

	@Override
	public boolean add(T obj) {
		return this.collection.add(transformToBase(obj));
	}

	@Override
	public boolean remove(Object obj) {
		if (this.toBaseTransformer != null)
			//noinspection unchecked
			return this.collection.remove(transformToBase((T)obj));

		for (Iterator<R> iterator = this.collection.iterator(); iterator.hasNext();) {
			if (obj.equals(transformToView(iterator.next()))) {
				iterator.remove();

				return true;
			}
		}

		return false;
	}

	@Override
	public boolean containsAll(Collection<?> collection) {
		final var viewSet = new ObjectOpenHashSet<>(this.collection.size());

		for (R element : this.collection) {
			viewSet.add(transformToView(element));
		}

		return viewSet.containsAll(collection);
	}

	@Override
	public boolean addAll(Collection<? extends T> collection) {
		boolean result = false;

		for (T element : collection) {
			result |= add(element);
		}

		return result;
	}

	@Override
	public boolean removeAll(Collection<?> collection) {
		boolean removed = false;

		for (Object element : collection) {
			removed |= remove(element);
		}

		return removed;
	}

	@Override
	public boolean retainAll(Collection<?> collection) {
		boolean result = false;

		for (Iterator<R> iterator = this.collection.iterator(); iterator.hasNext();) {
			final var element = transformToView(iterator.next());

			if (!collection.contains(element)) {
				iterator.remove();
				result = true;
			}
		}

		return result;
	}

	@Override
	public void clear() {
		this.collection.clear();
	}
}