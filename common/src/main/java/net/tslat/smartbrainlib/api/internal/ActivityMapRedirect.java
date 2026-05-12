package net.tslat.smartbrainlib.api.internal;

import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.schedule.Activity;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

/// A wrapper class around the vanilla [Brain]'s [Brain#availableBehaviorsByPriority], to allow for [SmartBrain] to use a more efficient
/// implementation whilst remaining fully compatible with the vanilla `Brain` system
///
/// SmartBrainLib itself should never actually interact with this implementation, it only exists for third-party compatibility
///
/// Other than that, this entire concept should basically never be used, ever.
class ActivityMapRedirect<BO extends LivingEntity & SmartBrainOwner<BO>> implements Map<Integer, Map<Activity, Set<BehaviorControl<? super BO>>>> {
	private final SmartBrain<BO>.Behaviours behaviours;

	private ActivityMapRedirect(SmartBrain<BO>.Behaviours behaviours) {
		this.behaviours = behaviours;
	}

	/// Create a new [SmartBrain#behaviours] wrapper
	static <BO extends LivingEntity & SmartBrainOwner<BO>> ActivityMapRedirect<BO> wrap(SmartBrain<BO>.Behaviours behaviours) {
		return new ActivityMapRedirect<>(behaviours);
	}

	/// Sort the wrapped list in natural order of [SmartBrain.Behaviours.ByPriority#priority()] if it has been modified and needs sorting
	private void checkSort() {
		this.behaviours.sort();
	}

	/// Mark the [SmartBrain.Behaviours] collection as needing to be sorted after a modification of the [SmartBrain.Behaviours.ByPriority] list
	private void markNeedsSorting() {
		this.behaviours.needsSorting = true;
	}

	@Override
	public int size() {
		return this.behaviours.behaviours.size();
	}

	@Override
	public boolean isEmpty() {
		return this.behaviours.behaviours.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return findByPriority((Integer)key, false) != null;
	}

	@Override
	public boolean containsValue(Object value) {
		final var map = (Map<?, ?>)value;

		for (var byPriority : this.behaviours.behaviours) {
			if (byPriority.behaviours.size() != map.size()) {
				for (var entry : byPriority.behaviours.entrySet()) {
					final var activityBehaviours = map.get(entry.getKey());

					//noinspection SuspiciousMethodCalls
					if (!(activityBehaviours instanceof Set<?> set) || set.size() != entry.getValue().size() || !set.containsAll(entry.getValue()))
						return false;
				}

				return true;
			}
		}

		return false;
	}

	@Override
	public @Nullable Map<Activity, Set<BehaviorControl<? super BO>>> get(Object key) {
		final SmartBrain<BO>.Behaviours.ByPriority behaviours = findByPriority((Integer)key, false);

		return behaviours == null ? null : ByPriorityView.of(behaviours);
	}

	@Override
	public @Nullable Map<Activity, Set<BehaviorControl<? super BO>>> put(Integer key, Map<Activity, Set<BehaviorControl<? super BO>>> value) {
		final var existing = get(key);

		if (existing != null)
			remove(key);

        //noinspection WriteOnlyObject
        ByPriorityView.of(findByPriority(key, true)).putAll(value);
		markNeedsSorting();

		return existing;
	}

	@Override
	public @Nullable Map<Activity, Set<BehaviorControl<? super BO>>> remove(Object key) {
		final var integer = (Integer)key;
		final var existing = get(integer);

		this.behaviours.behaviours.removeIf(activity -> activity.priority() == integer);

		return existing;
	}

	@Override
	public void putAll(Map<? extends Integer, ? extends Map<Activity, Set<BehaviorControl<? super BO>>>> map) {
		for (var entry : map.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public void clear() {
		this.behaviours.behaviours.clear();
	}

	@Override
	public Set<Integer> keySet() {
		checkSort();

		return SetView.of(TransformingListView.of(this.behaviours.behaviours,
												  SmartBrain.Behaviours.ByPriority::priority,
												  integer -> ActivityMapRedirect.this.behaviours.new ByPriority(integer)));
	}

	@Override
	public Collection<Map<Activity, Set<BehaviorControl<? super BO>>>> values() {
		checkSort();

		return TransformingListView.of(this.behaviours.behaviours, ByPriorityView::of, null);
	}

	@Override
	public Set<Entry<Integer, Map<Activity, Set<BehaviorControl<? super BO>>>>> entrySet() {
		checkSort();

		final Function<SmartBrain<BO>.Behaviours.ByPriority, Entry<Integer, Map<Activity, Set<BehaviorControl<? super BO>>>>> toViewTransformer = group -> {
			final var mapView = ByPriorityView.of(group);

			return entryOf(group, SmartBrain.Behaviours.ByPriority::priority, _ -> mapView, (_, value) -> {
				final var copy = new Reference2ObjectArrayMap<>(mapView);

				mapView.clear();
				mapView.putAll(value);

				return copy;
			});
		};

		return SetView.of(TransformingListView.of(this.behaviours.behaviours, toViewTransformer, null));
	}

	@SuppressWarnings("SameParameterValue")
	@Contract("_,true->!null")
    private SmartBrain<BO>.Behaviours.@Nullable ByPriority findByPriority(int priority, boolean computeNew) {
		for (var activityBehaviours : this.behaviours.behaviours) {
			if (activityBehaviours.priority() == priority)
				return activityBehaviours;
		}

		if (!computeNew)
			return null;

		final var behaviours = this.behaviours.new ByPriority(priority);

		this.behaviours.behaviours.add(behaviours);
		markNeedsSorting();

		return behaviours;
	}

	/// A [Map] view of the [SmartBrain.Behaviours.ByPriority#behaviours()] map, transmuting the [List] to a [Set] for compatibility
	/// Changes to the view are immediately present in the map, and vice versa
	static class ByPriorityView<BO extends LivingEntity & SmartBrainOwner<BO>> implements Map<Activity, Set<BehaviorControl<? super BO>>> {
		private final SmartBrain<BO>.Behaviours.ByPriority byPriority;

		private ByPriorityView(SmartBrain<BO>.Behaviours.ByPriority byPriority) {
			this.byPriority = byPriority;
		}

		/// Create a new [SetView] instance of the given [List]
		static <BO extends LivingEntity & SmartBrainOwner<BO>> ByPriorityView<BO> of(SmartBrain<BO>.Behaviours.ByPriority byPriority) {
			return new ByPriorityView<>(byPriority);
		}

		@Override
		public int size() {
			return this.byPriority.behaviours.size();
		}

		@Override
		public boolean isEmpty() {
			return this.byPriority.behaviours.isEmpty();
		}

		@Override
		public boolean containsKey(Object key) {
			return this.byPriority.behaviours.containsKey(key);
		}

		@Override
		public boolean containsValue(Object value) {
			final var set = (Set<?>)value;

			for (var behaviours : this.byPriority.behaviours.values()) {
				if (SetView.of(behaviours).equals(set))
					return true;
			}

			return false;
		}

		@Override
		public @Nullable Set<BehaviorControl<? super BO>> get(Object key) {
			final var behaviours = this.byPriority.behaviours.get(key);

            //noinspection ConstantValue
            return behaviours == null ? null : SetView.of(behaviours);
		}

		@Override
		public @Nullable Set<BehaviorControl<? super BO>> put(Activity key, Set<BehaviorControl<? super BO>> value) {
			final var existing = this.byPriority.behaviours.put(key, new ObjectArrayList<>(value));

			return existing == null ? null : SetView.of(existing);
		}

		@Override
		public @Nullable Set<BehaviorControl<? super BO>> remove(Object key) {
			final var existing = this.byPriority.behaviours.remove(key);

			//noinspection ConstantValue
			return existing == null ? null : SetView.of(existing);
		}

		@Override
		public void putAll(Map<? extends Activity, ? extends Set<BehaviorControl<? super BO>>> map) {
			for (var entry : map.entrySet()) {
				this.byPriority.behaviours.put(entry.getKey(), new ObjectArrayList<>(entry.getValue()));
			}
		}

		@Override
		public void clear() {
			this.byPriority.behaviours.clear();
		}

		@Override
		public Set<Activity> keySet() {
			return this.byPriority.behaviours.keySet();
		}

		@Override
		public Collection<Set<BehaviorControl<? super BO>>> values() {
			return TransformingCollectionView.of(this.byPriority.behaviours.values(), SetView::of, null);
		}

		@Override
		public Set<Entry<Activity, Set<BehaviorControl<? super BO>>>> entrySet() {
            return SetView.of(TransformingCollectionView.of(
					this.byPriority.behaviours.entrySet(),
					entry -> entryOf(entry,
									 Entry::getKey,
									 e -> SetView.of(e.getValue()),
									 (e, v) -> SetView.of(e.setValue(new ObjectArrayList<>(v)))), null));
		}

		@Override
		public boolean equals(Object o) {
			if (o == this)
				return true;

			if (!(o instanceof Map<?, ?> map))
				return false;

			if (map.size() != size())
				return false;

			if (map.isEmpty())
				return true;

			for (var entry : map.entrySet()) {
				if (!(entry.getKey() instanceof Activity activity) || !(entry.getValue() instanceof Set<?> set))
					return false;

				final var behaviours = get(activity);

				if (behaviours == null || !behaviours.equals(set))
					return false;
			}

			return true;
		}

		@Override
		public int hashCode() {
			return this.byPriority.hashCode();
		}
	}

	/// Implementation of a view of another [Collection], transforming elements between the two formats
	///
	/// The base list must have a 1:1 relationship for elements
	/// Changes to the base list are immediately present in the view, and vice versa
	static class TransformingCollectionView<C extends Collection<R>, R, T> implements Collection<T> {
		protected final C collection;
		protected final Function<R, T> toViewTransformer;
		protected final @Nullable Function<T, R> toBaseTransformer;

		private TransformingCollectionView(C baseCollection, Function<R, T> toViewTransformer, @Nullable Function<T, R> toBaseTransformer) {
			this.collection = baseCollection;
			this.toViewTransformer = toViewTransformer;
			this.toBaseTransformer = toBaseTransformer;
		}

		/// Create a new instance of
		@SuppressWarnings("SameParameterValue")
        static <C extends Collection<R>, R, T> TransformingCollectionView<C, R, T> of(C baseCollection, Function<R, T> toViewTransformer, @Nullable Function<T, R> toBaseTransformer) {
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

	/// Implementation of a view of another [List], transforming elements between the two formats
	///
	/// The base list must have a 1:1 relationship for elements
	/// Changes to the base list are immediately present in the view, and vice versa
	static class TransformingListView<R, T> extends TransformingCollectionView<List<R>, R, T> implements List<T> {
		private TransformingListView(List<R> baseList, Function<R, T> toViewTransformer, @Nullable Function<T, R> toBaseTransformer) {
			super(baseList, toViewTransformer, toBaseTransformer);
		}

		/// Create a new instance of
		static <R, T> TransformingListView<R, T> of(List<R> baseList, Function<R, T> toViewTransformer, @Nullable Function<T, R> toBaseTransformer) {
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

				@Override
				public boolean hasNext() {
					return this.index < TransformingListView.this.collection.size();
				}

				@Override
				public T next() {
					return transformToView(TransformingListView.this.collection.get(++this.index));
				}

				@Override
				public void remove() {
					TransformingListView.this.collection.remove(this.index);
				}

				@Override
				public boolean hasPrevious() {
					return this.index > 0;
				}

				@Override
				public T previous() {
					return transformToView(TransformingListView.this.collection.get(--this.index));
				}

				@Override
				public int nextIndex() {
					return this.index + 1;
				}

				@Override
				public int previousIndex() {
					return this.index - 1;
				}

				@Override
				public void set(T element) {
					TransformingListView.this.collection.set(this.index, transformToBase(element));
				}

				@Override
				public void add(T element) {
					TransformingListView.this.collection.add(this.index, transformToBase(element));
				}
			};
		}

		@Override
		public List<T> subList(int fromIndex, int toIndex) {
			return TransformingListView.of(this.collection.subList(fromIndex, toIndex), this.toViewTransformer, this.toBaseTransformer);
		}
	}

	/// A [Set] view of a [Collection]<br/>
	/// Changes to the collection are immediately present in the set, and vice versa
	static class SetView<T> implements Set<T> {
		private final Collection<T> collection;

		private SetView(Collection<T> behaviours) {
			this.collection = behaviours;
		}

		/// Create a new [SetView] instance of the given [Collection]
		static <T> SetView<T> of(Collection<T> behaviours) {
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

	/// Create a [Map.Entry] of a given object, using inline transformers to provide the values
	static <R, K, V> Map.Entry<K, V> entryOf(R element, Function<R, K> keyFunction, Function<R, V> valueFunction, BiFunction<R, V, V> setter) {
		return new Entry<>() {
			@Override
			public K getKey() {
				return keyFunction.apply(element);
			}

			@Override
			public V getValue() {
				return valueFunction.apply(element);
			}

			@Override
			public V setValue(V value) {
				return setter.apply(element, value);
			}
		};
	}
}