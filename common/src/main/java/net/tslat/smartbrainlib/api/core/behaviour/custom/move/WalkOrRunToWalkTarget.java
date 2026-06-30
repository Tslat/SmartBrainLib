package net.tslat.smartbrainlib.api.core.behaviour.custom.move;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.Path;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/// Extension of [MoveToWalkTarget], but auto-marking the sprinting flag depending on the [#speedModifier].
/// This can be useful for using sprint animations on the client.
///
/// @param <BO> The brain owner entity
public class WalkOrRunToWalkTarget<BO extends PathfinderMob> extends MoveToWalkTarget<BO> {
	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// Set a callback for when the behaviour successfully begins
	///
	/// This is called immediately prior to [ExtendedBehaviour#start(LivingEntity)]
	@ApiStatus.NonExtendable
	@Override
	public WalkOrRunToWalkTarget<BO> whenStarting(Consumer<BO> callback) {
		return (WalkOrRunToWalkTarget<BO>)super.whenStarting(callback);
	}
	
	/// Set a callback for when the behaviour stops
	///
	/// This is called immediately prior to [ExtendedBehaviour#stop(LivingEntity)]
	///
	/// Note that the behaviour stopping does not necessarily mean it was successful
	@ApiStatus.NonExtendable
	@Override
	public WalkOrRunToWalkTarget<BO> whenStopping(Consumer<BO> callback) {
		return (WalkOrRunToWalkTarget<BO>)super.whenStopping(callback);
	}
	
	/// Set the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public WalkOrRunToWalkTarget<BO> runFor(int ticks) {
		return (WalkOrRunToWalkTarget<BO>)super.runFor(ticks);
	}
	
	/// Set the range of ticks the behaviour should try to run for, once started<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is run
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public WalkOrRunToWalkTarget<BO> runFor(int minTicks, int maxTicks) {
		return (WalkOrRunToWalkTarget<BO>)super.runFor(minTicks, maxTicks);
	}
	
	/// Set a function to determine the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public WalkOrRunToWalkTarget<BO> runFor(ToIntFunction<BO> timeProvider) {
		return (WalkOrRunToWalkTarget<BO>)super.runFor(timeProvider);
	}
	
	/// Disable the tick-based timeout for this behaviour and instead rely exclusively on other conditions
	/// such as [#getMemoryRequirements()] failing or [#stopIf(Predicate)]
	@ApiStatus.NonExtendable
	@Override
	public WalkOrRunToWalkTarget<BO> noTimeout() {
		return (WalkOrRunToWalkTarget<BO>)super.noTimeout();
	}
	
	/// Set the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public WalkOrRunToWalkTarget<BO> cooldownFor(int ticks) {
		return (WalkOrRunToWalkTarget<BO>)super.cooldownFor(ticks);
	}
	
	/// Set the range of ticks that this behaviour should be prevented from starting again after it has finished<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is stopped
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public WalkOrRunToWalkTarget<BO> cooldownFor(int minTicks, int maxTicks) {
		return (WalkOrRunToWalkTarget<BO>)super.cooldownFor(minTicks, maxTicks);
	}
	
	/// Set a function to determine the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public WalkOrRunToWalkTarget<BO> cooldownFor(ToIntFunction<BO> timeProvider) {
		return (WalkOrRunToWalkTarget<BO>)super.cooldownFor(timeProvider);
	}
	
	/// Set an additional condition for the behaviour to be able to start
	///
	/// Prevents this behaviour starting unless this predicate returns true
	@ApiStatus.NonExtendable
	@Override
	public WalkOrRunToWalkTarget<BO> startCondition(Predicate<BO> predicate) {
		return (WalkOrRunToWalkTarget<BO>)super.startCondition(predicate);
	}
	
	/// Set a condition under which the behaviour should automatically stop<br/>
	/// Has no effect on one-shot behaviours that don't tick or have a runtime
	///
	/// Stops the behaviour immediately if the predicate returns true, ready to run again
	@ApiStatus.NonExtendable
	@Override
	public WalkOrRunToWalkTarget<BO> stopIf(Predicate<BO> predicate) {
		return (WalkOrRunToWalkTarget<BO>)super.stopIf(predicate);
	}
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="<Internal Handling>">
	/// Instruct the entity's [PathNavigation] to begin walking on the provided [Path]
	///
	/// @return Whether the entity's navigator successfully set the path
	@Override
	protected boolean startOnNewPath(BO entity, Path path) {
		if (super.startOnNewPath(entity, path)) {
			entity.setSharedFlag(3, this.speedModifier > 1);
			
			return true;
		}
		
		return false;
	}
	
	/// Called when this behaviour is instructed to stop<br/>
	/// This may be due to timing out, failing to meet a condition, or for any other reason
	///
	/// This method is non-negotiable, and it must be safe to assume that if this method is called, this behaviour is safe to discard
	@MustBeInvokedByOverriders
	@Override
	protected void stop(BO entity) {
		super.stop(entity);

		entity.setSharedFlag(3, false);
	}
	//</editor-fold>
}