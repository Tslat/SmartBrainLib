package net.tslat.smartbrainlib.api.core.schedule;

import it.unimi.dsi.fastutil.objects.Object2ReferenceArrayMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.Level;
import net.tslat.smartbrainlib.api.SmartBrainBuilder;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.internal.SmartBrain;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.function.Function;

/// SmartBrainLib implementation of a context [Activity] schedule for [SmartBrainOwner]s
///
/// This is an approximate equivalent of [Brain#schedule], but omitting the data-driven aspects of it as SBL is intentionally not data-driven,
/// and expanding it to support numerous other contexts.
///
/// Override [SmartBrainBuilder#getSchedule()] in your brain builder to give an entity's [SmartBrain] a schedule
public class SmartBrainSchedule<BO extends LivingEntity & SmartBrainOwner<BO>, N extends Number & Comparable<N>> {
	protected final Type<BO, N> type;
	protected final Object2ReferenceArrayMap<N, Activity> timeline = new Object2ReferenceArrayMap<>();

	protected SmartBrainSchedule(Type<BO, N> type) {
		this.type = type;
	}

	/// Create a new [SmartBrainSchedule] instance that runs on a 'daytime' cycle
	public static <BO extends LivingEntity & SmartBrainOwner<BO>> SmartBrainSchedule<BO, Long> byDaytime() {
		return createCustom(Type.<BO, Long>ascending(entity -> {
			final Level level = entity.level();
			final MinecraftServer server = entity.level().getServer();

			//noinspection DataFlowIssue
			return level.dimensionType().defaultClock().map(server.clockManager()::getTotalTicks).orElse(0L) % 24000L;
		}));
	}

	/// Create a new [SmartBrainSchedule] instance that runs based on the entity's age
	///
	/// **<u>NOTE:</u>** By default, entities do not save their age, so a custom provider is required
	///
	/// @param ageProvider The age provider for this schedule
	public static <BO extends LivingEntity & SmartBrainOwner<BO>> SmartBrainSchedule<BO, Long> byEntityAge(Function<BO, Long> ageProvider) {
		return createCustom(Type.ascending(ageProvider));
	}

	/// Create a new [SmartBrainSchedule] instance that runs based on the percentage of health the entity has remaining
	///
	/// This can be used for things like health-threshold phases
	public static <BO extends LivingEntity & SmartBrainOwner<BO>> SmartBrainSchedule<BO, Float> byHealthThreshold() {
		return createCustom(Type.<BO, Float>descending(entity -> entity.getHealth() / entity.getMaxHealth()));
	}

	/// Create a new [SmartBrainSchedule] instance using a custom [Type] for value management
	public static <BO extends LivingEntity & SmartBrainOwner<BO>, N extends Number & Comparable<N>> SmartBrainSchedule<BO, N> createCustom(Type<BO, N> type) {
		return new SmartBrainSchedule<>(type);
	}

	/// Set the active [Activity] for the brain at the given marker value
	///
	/// @param value The marker value that the activity should be valid from
	/// @param activity The activity to set as active past the given value
	@SuppressWarnings("UnusedReturnValue")
    public SmartBrainSchedule<BO, N> activityAt(N value, Activity activity) {
		this.timeline.put(value, activity);
		sortSchedule();

		return this;
	}

	//<editor-fold defaultstate="collapsed" desc="<Internal Handling>">
	/// Sort the schedule to its natural order
	protected void sortSchedule() {
		@SuppressWarnings("unchecked")
		final N[] keys = (N[])this.timeline.keySet().toArray(new Number[0]);
		final Object2ReferenceArrayMap<N, Activity> copy = new Object2ReferenceArrayMap<>(this.timeline);

		this.timeline.clear();
		Arrays.sort(keys);

		for (N key : keys) {
			this.timeline.put(key, copy.get(key));
		}
	}

	public @Nullable Activity tick(BO entity) {
		return getActivityAt(this.type.valueProvider.apply(entity));
	}

	/// Get the current [Activity] for the value provided
	public @Nullable Activity getActivityAt(N value) {
		Activity last = null;

		for (Map.Entry<N, Activity> entry : this.timeline.entrySet()) {
			if (this.type.comparator.compare(entry.getKey(), value) > 0)
				return last;

			last = entry.getValue();
		}

		return last;
	}

	/// Value type class for [SmartBrainSchedule]s
	///
	/// This class determines the type of timeline this schedule follows, as well as how to assess it.<br/>
	/// This allows for schedules to operate on more than just basic daytime operations
	public static class Type<T extends LivingEntity, N extends Number & Comparable<N>> {
		protected final Function<T, N> valueProvider;
		protected final Comparator<N> comparator;

		protected Type(Function<T, N> valueProvider, Comparator<N> comparator) {
			this.valueProvider = valueProvider;
			this.comparator = comparator;
		}

		/// Create a new [SmartBrainSchedule.Type] that considers larger values to come after smaller numbers (such as an increasing tick count)
		public static <T extends LivingEntity, N extends Number & Comparable<N>> Type<T, N> ascending(Function<T, N> valueProvider) {
			return custom(valueProvider, Comparator.naturalOrder());
		}

		/// Create a new [SmartBrainSchedule.Type] that considers smaller values to come after larger numbers (such as a health threshold)
		public static <T extends LivingEntity, N extends Number & Comparable<N>> Type<T, N> descending(Function<T, N> valueProvider) {
			return custom(valueProvider, Comparator.reverseOrder());
		}

		/// Create a new [SmartBrainSchedule.Type] with custom progression handling
		public static <T extends LivingEntity, N extends Number & Comparable<N>> Type<T, N> custom(Function<T, N> valueProvider, Comparator<N> comparator) {
			return new Type<>(valueProvider, comparator);
		}
	}
	//</editor-fold>
}