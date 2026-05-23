package net.tslat.smartbrainlib.library.object.collection;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/// A [Set] view of a [Collection]<br/>
/// Changes to the collection are immediately present in the set, and vice versa
public class SetView<T> implements Set<T> {
	private final Collection<T> collection;

	protected SetView(Collection<T> behaviours) {
		this.collection = behaviours;
	}

	/// Create a new [SetView] instance of the given [Collection]
	public static <T> SetView<T> of(Collection<T> behaviours) {
		return new SetView<>(behaviours);
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
		return this.collection.contains(obj);
	}

	@Override
	public Iterator<T> iterator() {
		return this.collection.iterator();
	}

	@Override
	public Object[] toArray() {
		return this.collection.toArray();
	}

	@Override
	public <A> A[] toArray(A[] array) {
		return this.collection.toArray(array);
	}

	@Override
	public boolean add(T behaviorControl) {
		if (!this.collection.contains(behaviorControl))
			return this.collection.add(behaviorControl);

		return false;
	}

	@Override
	public boolean remove(Object obj) {
		return this.collection.remove(obj);
	}

	@Override
	public boolean containsAll(Collection<?> collection) {
		return this.collection.containsAll(collection);
	}

	@Override
	public boolean addAll(Collection<? extends T> collection) {
		boolean modified = false;

		for (T element : collection) {
			modified |= add(element);
		}

		return modified;
	}

	@Override
	public boolean retainAll(Collection<?> collection) {
		return this.collection.retainAll(collection);
	}

	@Override
	public boolean removeAll(Collection<?> collection) {
		return this.collection.removeAll(collection);
	}

	@Override
	public void clear() {
		this.collection.clear();
	}
}