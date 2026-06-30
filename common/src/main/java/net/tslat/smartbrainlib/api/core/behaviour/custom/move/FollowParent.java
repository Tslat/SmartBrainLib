package net.tslat.smartbrainlib.api.core.behaviour.custom.move;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtil;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jspecify.annotations.Nullable;

import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/// A movement behaviour for automatically following the parent of an [AgeableMob][AgeableMob].
///
/// Note that because vanilla animals do not store a reference to their parent or child, by default this behaviour just grabs the nearest
/// animal of the same class and presumes it is the parent.
///
/// @param <BO> The brain owner entity
public class FollowParent<BO extends AgeableMob> extends FollowEntity<BO> {
	protected BiPredicate<BO, AgeableMob> parentPredicate = (entity, other) -> entity.getClass() == other.getClass() && other.getAge() >= 0;

	public FollowParent() {
		super((_, _) -> false);
		
		closeEnoughDist(2);
	}
	
	/// Set the predicate that determines whether a given entity is a suitable 'parent' to follow
	@ApiStatus.NonExtendable
	public FollowParent<BO> parentPredicate(BiPredicate<BO, AgeableMob> predicate) {
		this.parentPredicate = predicate;
		
		return this;
	}
	
	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// Set a callback for when the behaviour successfully begins
	///
	/// This is called immediately prior to [ExtendedBehaviour#start(LivingEntity)]
	@ApiStatus.NonExtendable
	@Override
	public FollowParent<BO> whenStarting(Consumer<BO> callback) {
		return (FollowParent<BO>)super.whenStarting(callback);
	}
	
	/// Set a callback for when the behaviour stops
	///
	/// This is called immediately prior to [ExtendedBehaviour#stop(LivingEntity)]
	///
	/// Note that the behaviour stopping does not necessarily mean it was successful
	@ApiStatus.NonExtendable
	@Override
	public FollowParent<BO> whenStopping(Consumer<BO> callback) {
		return (FollowParent<BO>)super.whenStopping(callback);
	}
	
	/// Set the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public FollowParent<BO> runFor(int ticks) {
		return (FollowParent<BO>)super.runFor(ticks);
	}
	
	/// Set the range of ticks the behaviour should try to run for, once started<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is run
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public FollowParent<BO> runFor(int minTicks, int maxTicks) {
		return (FollowParent<BO>)super.runFor(minTicks, maxTicks);
	}
	
	/// Set a function to determine the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public FollowParent<BO> runFor(ToIntFunction<BO> timeProvider) {
		return (FollowParent<BO>)super.runFor(timeProvider);
	}
	
	/// Disable the tick-based timeout for this behaviour and instead rely exclusively on other conditions
	/// such as [#getMemoryRequirements()] failing or [#stopIf(Predicate)]
	@ApiStatus.NonExtendable
	@Override
	public FollowParent<BO> noTimeout() {
		return (FollowParent<BO>)super.noTimeout();
	}
	
	/// Set the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public FollowParent<BO> cooldownFor(int ticks) {
		return (FollowParent<BO>)super.cooldownFor(ticks);
	}
	
	/// Set the range of ticks that this behaviour should be prevented from starting again after it has finished<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is stopped
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public FollowParent<BO> cooldownFor(int minTicks, int maxTicks) {
		return (FollowParent<BO>)super.cooldownFor(minTicks, maxTicks);
	}
	
	/// Set a function to determine the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public FollowParent<BO> cooldownFor(ToIntFunction<BO> timeProvider) {
		return (FollowParent<BO>)super.cooldownFor(timeProvider);
	}
	
	/// Set an additional condition for the behaviour to be able to start
	///
	/// Prevents this behaviour starting unless this predicate returns true
	@ApiStatus.NonExtendable
	@Override
	public FollowParent<BO> startCondition(Predicate<BO> predicate) {
		return (FollowParent<BO>)super.startCondition(predicate);
	}
	
	/// Set a condition under which the behaviour should automatically stop<br/>
	/// Has no effect on one-shot behaviours that don't tick or have a runtime
	///
	/// Stops the behaviour immediately if the predicate returns true, ready to run again
	@ApiStatus.NonExtendable
	@Override
	public FollowParent<BO> stopIf(Predicate<BO> predicate) {
		return (FollowParent<BO>)super.stopIf(predicate);
	}
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="<Internal Handling>">
	/// Check any extra conditions required for this behaviour to start
	///
	/// By this stage, memory conditions from [#getMemoryRequirements()] have already been checked
	///
	/// @return Whether the conditions have been met to start the behaviour
	@MustBeInvokedByOverriders
	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, BO entity) {
		return entity.getAge() < 0 && super.checkExtraStartConditions(level, entity);
	}
	
	/// Get the entity to follow, or null if a target entity is not available
	@Override
	protected @Nullable LivingEntity getFollowingEntity(BO entity) {
		return BrainUtil.memoryOrDefault(entity, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, NearestVisibleLivingEntities.empty())
		                .findClosest(other -> other instanceof AgeableMob ageableMob && this.parentPredicate.test(entity, ageableMob)).map(AgeableMob.class::cast).orElse(null);
	}
	//</editor-fold>
}
