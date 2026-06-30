package net.tslat.smartbrainlib.api.core.behaviour.custom.path;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;
import net.tslat.smartbrainlib.library.interfaces.ToFloatBiFunction;
import net.tslat.smartbrainlib.library.object.MemoryTest;
import net.tslat.smartbrainlib.library.object.SquareRadius;
import net.tslat.smartbrainlib.util.BrainUtil;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.function.*;

/// Abstract base class for movement target-related behaviours
///
/// This is the base class for most 'move to X position' type behaviours
///
/// @param <BO> The brain owner entity
public abstract class SetRandomMoveTarget<BO extends PathfinderMob> extends ExtendedBehaviour<BO> {
	protected static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(1).noMemory(MemoryModuleType.WALK_TARGET);
	
	protected ToFloatBiFunction<BO, Vec3> speedModifier = (_, _) -> 1f;
	protected Function<BO, SquareRadius> radius = _ -> new SquareRadius(10, 7);
	protected BiPredicate<BO, Vec3> validPositionPredicate = (_, _) -> true;
	protected ToIntBiFunction<BO, Vec3> closeEnoughDist = (_, _) -> 0;
	
	/// Set the radius (in blocks) to look for flight positions
	@ApiStatus.NonExtendable
	public SetRandomMoveTarget<BO> setRadius(double radius) {
		return setRadius(radius, radius);
	}
	
	/// Set the radius (in blocks) to look for flight positions
	@ApiStatus.NonExtendable
	public SetRandomMoveTarget<BO> setRadius(double xz, double y) {
		return setRadius(_ -> new SquareRadius(xz, y));
	}
	
	/// Set the function to determine the radius (in blocks) to look for flight positions
	@ApiStatus.NonExtendable
	public SetRandomMoveTarget<BO> setRadius(Function<BO, SquareRadius> function) {
		this.radius = function;
		
		return this;
	}
	
	/// Set the movement speed modifier for the path when chosen
	@ApiStatus.NonExtendable
	public SetRandomMoveTarget<BO> speedModifier(float modifier) {
		return speedModifier((_, _) -> modifier);
	}
	
	/// Set the function to determine the movement speed modifier for the path when chosen
	@ApiStatus.NonExtendable
	public SetRandomMoveTarget<BO> speedModifier(ToFloatBiFunction<BO, Vec3> function) {
		this.speedModifier = function;
		
		return this;
	}
	
	/// Sets a predicate to check whether a target movement position is valid or not
	@ApiStatus.NonExtendable
	public SetRandomMoveTarget<BO> isValidPositionIf(BiPredicate<BO, Vec3> predicate) {
		this.validPositionPredicate = predicate;
		
		return this;
	}
	
	/// Set the distance (in blocks) that the entity should be considered 'close enough' to the target position to have arrived
	@ApiStatus.NonExtendable
	public SetRandomMoveTarget<BO> closeEnoughDist(int distance) {
		return closeEnoughDist((_, _) -> distance);
	}
	
	/// Set the function to determine the distance (in blocks) that the entity should be considered 'close enough' to the target position to have arrived
	@ApiStatus.NonExtendable
	public SetRandomMoveTarget<BO> closeEnoughDist(ToIntBiFunction<BO, Vec3> function) {
		this.closeEnoughDist = function;
		
		return this;
	}
	
	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// Set a callback for when the behaviour successfully begins
	///
	/// This is called immediately prior to [ExtendedBehaviour#start(LivingEntity)]
	@ApiStatus.NonExtendable
	@Override
	public SetRandomMoveTarget<BO> whenStarting(Consumer<BO> callback) {
		return (SetRandomMoveTarget<BO>)super.whenStarting(callback);
	}
	
	/// Set a callback for when the behaviour stops
	///
	/// This is called immediately prior to [ExtendedBehaviour#stop(LivingEntity)]
	///
	/// Note that the behaviour stopping does not necessarily mean it was successful
	@ApiStatus.NonExtendable
	@Override
	public SetRandomMoveTarget<BO> whenStopping(Consumer<BO> callback) {
		return (SetRandomMoveTarget<BO>)super.whenStopping(callback);
	}
	
	/// Set the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public SetRandomMoveTarget<BO> runFor(int ticks) {
		return (SetRandomMoveTarget<BO>)super.runFor(ticks);
	}
	
	/// Set the range of ticks the behaviour should try to run for, once started<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is run
	/// 
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public SetRandomMoveTarget<BO> runFor(int minTicks, int maxTicks) {
		return (SetRandomMoveTarget<BO>)super.runFor(minTicks, maxTicks);
	}
	
	/// Set a function to determine the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public SetRandomMoveTarget<BO> runFor(ToIntFunction<BO> timeProvider) {
		return (SetRandomMoveTarget<BO>)super.runFor(timeProvider);
	}
	
	/// Disable the tick-based timeout for this behaviour and instead rely exclusively on other conditions
	/// such as [#getMemoryRequirements()] failing or [#stopIf(Predicate)]
	@ApiStatus.NonExtendable
	@Override
	public SetRandomMoveTarget<BO> noTimeout() {
		return (SetRandomMoveTarget<BO>)super.noTimeout();
	}
	
	/// Set the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public SetRandomMoveTarget<BO> cooldownFor(int ticks) {
		return (SetRandomMoveTarget<BO>)super.cooldownFor(ticks);
	}
	
	/// Set the range of ticks that this behaviour should be prevented from starting again after it has finished<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is stopped
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public SetRandomMoveTarget<BO> cooldownFor(int minTicks, int maxTicks) {
		return (SetRandomMoveTarget<BO>)super.cooldownFor(minTicks, maxTicks);
	}
	
	/// Set a function to determine the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public SetRandomMoveTarget<BO> cooldownFor(ToIntFunction<BO> timeProvider) {
		return (SetRandomMoveTarget<BO>)super.cooldownFor(timeProvider);
	}
	
	/// Set an additional condition for the behaviour to be able to start
	///
	/// Prevents this behaviour starting unless this predicate returns true.
	///
	/// @param predicate The condition for starting
	@ApiStatus.NonExtendable
	@Override
	public SetRandomMoveTarget<BO> startCondition(Predicate<BO> predicate) {
		return (SetRandomMoveTarget<BO>)super.startCondition(predicate);
	}
	
	/// Set a condition under which the behaviour should automatically stop<br/>
	/// Has no effect on one-shot behaviours that don't tick or have a runtime
	/// 
	/// Stops the behaviour immediately if the predicate returns true, ready to run again
	@ApiStatus.NonExtendable
	@Override
	public SetRandomMoveTarget<BO> stopIf(Predicate<BO> predicate) {
		return (SetRandomMoveTarget<BO>)super.stopIf(predicate);
	}
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="<Custom Implementation Boilerplate>">
	/// Get a new nearby position to attempt to move to
	///
	/// This position does not need to be predicated with the [#validPositionPredicate]
	protected abstract @Nullable Vec3 getTargetPos(BO entity);
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="<Internal Handling>">
	/// The set of memory requirements this task has before starting<br/>
	/// This outlines the approximate state the brain should be in to allow this behaviour to run
	///
	/// Ideally, this would be a statically cached set
	///
	/// @return The [Set] of [Memories][MemoryModuleType] and their associated required [status][MemoryStatus]
	/// @see MemoryTest
	@Override
	public Set<MemoryCondition<?, ?>> getMemoryRequirements() {
		return MEMORY_REQUIREMENTS;
	}
	
	/// Run the core functionality this behaviour has when starting<br/>
	/// This method is called once per behaviour run
	/// 
	/// By this stage any memory requirements set in [#getMemoryRequirements()] are true, so any memories paired with [MemoryStatus#VALUE_PRESENT] are safe to retrieve
	///
	/// If you are not making a custom behaviour and are instead just using an existing behaviour; you would use [#whenStarting(Consumer)] instead of overriding this method  
	@MustBeInvokedByOverriders
	@Override
	protected void start(BO entity) {
		Vec3 targetPos = getTargetPos(entity);
		
		if (targetPos != null && !this.validPositionPredicate.test(entity, targetPos))
			targetPos = null;
		
		if (targetPos != null) {
			BrainUtil.setMemory(entity, MemoryModuleType.WALK_TARGET, new WalkTarget(targetPos, this.speedModifier.applyAsFloat(entity, targetPos), this.closeEnoughDist.applyAsInt(entity, targetPos)));
		}
		else {
			BrainUtil.clearMemory(entity, MemoryModuleType.WALK_TARGET);
		}
	}
	//</editor-fold>
}
