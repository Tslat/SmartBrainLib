package net.tslat.smartbrainlib.api;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.schedule.Activity;
import net.tslat.smartbrainlib.SBLPlatform;
import net.tslat.smartbrainlib.api.core.schedule.SmartBrainSchedule;
import net.tslat.smartbrainlib.api.internal.SmartBrain;
import org.jetbrains.annotations.ApiStatus;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/// The root class for all `SmartBrainLib` functionality.<br/>
/// Any entity that intends to use the [SmartBrain] system **<u>MUST</u>** implement this class
///
/// @param <BO> The brain owner entity class that is implementing this interface
/// @see SmartBrainBuilder
public interface SmartBrainOwner<BO extends LivingEntity & SmartBrainOwner<BO>> extends SmartBrainBuilder<BO> {
	/// Return the [SmartBrainBuilder] instance for this brain owner
	///
	/// Normally this is just the entity itself, however, if you want to move your AI declarations to an external class,
	/// you would return an instance of that class here
	///
	/// You should **<u>NOT</u>** be calling this method yourself.<br/>
	/// If you are intending to make a new brain instance manually, call [SBLPlatform#makeBrain] instead
	@ApiStatus.OverrideOnly
	default SmartBrainBuilder<BO> getBrainBuilder() {
		return this;
	}

	/// Return the array of activities that this brain should automatically use, in the order that they
	/// should be prioritised.<br/>
	/// Earlier activities have a higher priority than later ones
	///
	/// Activities can also be manually set at any time without being on this array, but you will need to
	/// add them to the [additional activities][SmartBrainBuilder#getAdditionalActivities] list
	default Activity[] getActivityActivationPriority() {
		return new Activity[] {Activity.FIGHT, Activity.IDLE};
	}

	/// Override this to return a set of activities that should be prioritised over scheduled activities.
	///
	/// Activities listed here will be selected even if a [schedule][SmartBrainSchedule] determines another activity is valid.
	///
	/// @return A [Set] of [Activity] categories
	default Set<Activity> getScheduleIgnoringActivities() {
		return ObjectArraySet.of(Activity.FIGHT);
	}

	/// Get the [Activity] that is currently relevant for this [SmartBrainOwner], if any
	///
	/// Can be overridden to return different activities depending on entity context
	///
	/// @param canRunActivity A test to confirm whether an [Activity] can be run at the current time
	default Optional<Activity> getCurrentRelevantActivity(Predicate<Activity> canRunActivity) {
		for (Activity activity : getActivityActivationPriority()) {
			if (canRunActivity.test(activity))
				return Optional.of(activity);
		}

		return Optional.empty();
	}
}
