package net.tslat.smartbrainlib.api.internal;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.schedule.Activity;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.library.object.collection.SetView;
import net.tslat.smartbrainlib.library.object.collection.TransformingCollectionView;
import net.tslat.smartbrainlib.library.object.collection.TransformingListView;
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