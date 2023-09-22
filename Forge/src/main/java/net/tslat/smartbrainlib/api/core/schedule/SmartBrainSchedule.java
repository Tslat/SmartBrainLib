package net.tslat.smartbrainlib.api.core.schedule;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.schedule.Schedule;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.ToIntBiFunction;
import java.util.function.ToIntFunction;

/**
 * SBL-implementation of the vanilla {@link net.minecraft.world.entity.schedule.Schedule Schedule}.<br>
 * It extends the vanilla {@link Schedule} purely for compatibility reasons, but does not utilise any of its functionality.<br>
 * <br>
 * This segment of the Brain system is used to timeline activities, allowing you to run activity groups and tasks on a tick-based schedule.<br>
 * <br>
 * Activities scheduled using this system will <b>override</b> the activity priorities from {@link SmartBrainOwner#getActivityPriorities()} at tick time
 */
public class SmartBrainSchedule extends Schedule {
	private final Type type;
	private final Int2ObjectArrayMap<Activity> timeline = new Int2ObjectArrayMap<>(0);
	private final ListMultimap<Integer, Consumer<LivingEntity>> callbacks = MultimapBuilder.hashKeys(0).arrayListValues().build();

	private boolean sortedTimeline = true;

	public SmartBrainSchedule() {
		this(Type.DAYTIME);
	}

	public SmartBrainSchedule(Type type) {
		this.type = type;
	}

	/**
	 * Set the active {@link Activity} for the brain at the given tick/time
	 * @param tick The tick/time to activate the activity
	 * @param activity The activity to set as active at the given time
	 * @return this
	 */
	public SmartBrainSchedule activityAt(int tick, Activity activity) {
		this.timeline.put(tick, activity);

		this.sortedTimeline = false;

		return this;
	}

	/**
	 * Add a callback to run at the given tick
	 * @param tick The tick/time to run the callback at
	 * @param callback The callback to run at the given time
	 * @return this
	 */
	public SmartBrainSchedule doAt(int tick, Consumer<LivingEntity> callback) {
		this.callbacks.put(tick, callback);

		return this;
	}

	/**
	 * Adds a dynamically-scheduled task for a given tick-time in the future
	 * @param brainOwner The owner of the brain
	 * @param delay The delay time (in ticks) before the task should be called
	 * @param task The task to run after the given delay
	 */
	public void scheduleTask(LivingEntity brainOwner, int delay, Consumer<LivingEntity> task) {
		this.callbacks.put(this.type.resolveDelay(brainOwner, delay), entity -> task.accept(brainOwner));
	}

	/**
	 * Remove all entries from the schedule, clearing it out
	 */
	public void clearSchedule() {
		this.callbacks.clear();
		this.timeline.clear();
	}

	/**
	 * Tick the schedule and return the activity to switch the entity to, if applicable
	 * @param brainOwner The owner of the brain that contains this schedule
	 * @return The activity to set as active based on the current tick, or null if none to set
	 */
	@Nullable
	public Activity tick(LivingEntity brainOwner) {
		int tick = this.type.resolve(brainOwner);

		if (!this.callbacks.isEmpty()) {
			this.callbacks.get(tick).forEach(consumer -> consumer.accept(brainOwner));

			if (this.type == Type.AGE)
				this.callbacks.removeAll(tick);
		}

		if (!this.timeline.isEmpty()) {
			if (!this.sortedTimeline)
				sortTimeline();

			int index = -1;
			Activity activity = null;

			for (Int2ObjectMap.Entry<Activity> entry : this.timeline.int2ObjectEntrySet()) {
				index++;

				if (entry.getIntKey() >= tick) {
					if (entry.getIntKey() == tick)
						activity = entry.getValue();

					break;
				}

				activity = entry.getValue();
			}

			if (this.type == Type.AGE && index + 1 >= this.timeline.size())
				this.timeline.clear();

			return activity;
		}

		return null;
	}

	private void sortTimeline() {
		Int2ObjectArrayMap<Activity> copy = new Int2ObjectArrayMap<>(this.timeline);
		int[] keys = copy.keySet().toArray(new int[0]);

		Arrays.sort(keys);
		this.timeline.clear();

		for (int key : keys) {
			this.timeline.put(key, copy.get(key));
		}

		this.sortedTimeline = true;
	}

	@Override
	public final Activity getActivityAt(int tick) {
		if (this.type == Type.AGE)
			return Activity.IDLE;

		Activity activity = Activity.IDLE;

		for (Int2ObjectMap.Entry<Activity> entry : this.timeline.int2ObjectEntrySet()) {
			if (entry.getIntKey() >= tick)
				return activity;

			activity = entry.getValue();
		}

		return activity;
	}

	/**
	 * The type of scheduling this scheduler is using (I.E. how it determines the input tick)
	 */
	public enum Type {
		/**
		 * Time of day (0-24000 ticks)
		 */
		DAYTIME(e -> (int)(e.level().getDayTime() % 24000L), (e, t) -> (int)((e.level().getDayTime() + t) % 24000L)),
		/**
		 * Age of the brain owner (0+).<br>
		 * This makes the schedule a 'run-once' per entity
		 */
		AGE(e -> e.tickCount, (e, t) -> e.tickCount + t);

		final ToIntFunction<LivingEntity> tickResolver;
		final ToIntBiFunction<LivingEntity, Integer> delayResolver;

		Type(ToIntFunction<LivingEntity> tickResolver, ToIntBiFunction<LivingEntity, Integer> delayResolver) {
			this.tickResolver = tickResolver;
			this.delayResolver = delayResolver;
		}

		public int resolve(LivingEntity entity) {
			return this.tickResolver.applyAsInt(entity);
		}

		public int resolveDelay(LivingEntity entity, int delay) {
			return this.delayResolver.applyAsInt(entity, delay);
		}
	}
}
