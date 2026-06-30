package net.tslat.smartbrainlib.api.core.behaviour.custom.target;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.ToFloatFunction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;
import net.tslat.smartbrainlib.library.object.ExactPositionTracker;
import net.tslat.smartbrainlib.library.object.MemoryTest;
import net.tslat.smartbrainlib.util.BrainUtil;
import net.tslat.smartbrainlib.util.RandomUtil;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/// Set the look target to a random nearby position
///
/// @param <BO> The brain owner entity
public class SetRandomLookTarget<BO extends Mob> extends ExtendedBehaviour<BO> {
	protected static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(1).noMemory(MemoryModuleType.LOOK_TARGET);

	protected ToFloatFunction<BO> runChance = _ -> 0.02f;
	protected ToIntFunction<BO> lookTime = entity -> entity.getRandom().nextInt(20) + 20;

	/// Set the chance (`0->1`) of setting a new look target per tick this behaviour is checked
	public SetRandomLookTarget<BO> lookChance(float chance) {
		return lookChance(_ -> chance);
	}
	
	/// Set a value provider to determine the chance (`0->1`) of setting a new look target per tick this behaviour is checked
	public SetRandomLookTarget<BO> lookChance(ToFloatFunction<BO> chance) {
		this.runChance = chance;

		return this;
	}

	/// Set how many ticks the entity's look target should be set for
	public SetRandomLookTarget<BO> lookTime(int ticks) {
		return lookTime(_ -> ticks);
	}

	/// Set how many ticks the entity's look target should be set for
	public SetRandomLookTarget<BO> lookTime(int minTicks, int maxTicks) {
		return lookTime(entity -> entity.getRandom().nextInt(maxTicks - minTicks) + minTicks);
	}

	/// Set the value provider for how long the entity's look target should be set for
	public SetRandomLookTarget<BO> lookTime(ToIntFunction<BO> function) {
		this.lookTime = function;

		return this;
	}
	
	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// Set a callback for when the behaviour successfully begins
	///
	/// This is called immediately prior to [ExtendedBehaviour#start(LivingEntity)]
	@ApiStatus.NonExtendable
	@Override
	public SetRandomLookTarget<BO> whenStarting(Consumer<BO> callback) {
		return (SetRandomLookTarget<BO>)super.whenStarting(callback);
	}
	
	/// Set a callback for when the behaviour stops
	///
	/// This is called immediately prior to [ExtendedBehaviour#stop(LivingEntity)]
	///
	/// Note that the behaviour stopping does not necessarily mean it was successful
	@ApiStatus.NonExtendable
	@Override
	public SetRandomLookTarget<BO> whenStopping(Consumer<BO> callback) {
		return (SetRandomLookTarget<BO>)super.whenStopping(callback);
	}
	
	/// Set the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public SetRandomLookTarget<BO> runFor(int ticks) {
		return (SetRandomLookTarget<BO>)super.runFor(ticks);
	}
	
	/// Set the range of ticks the behaviour should try to run for, once started<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is run
	/// 
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public SetRandomLookTarget<BO> runFor(int minTicks, int maxTicks) {
		return (SetRandomLookTarget<BO>)super.runFor(minTicks, maxTicks);
	}
	
	/// Set a function to determine the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public SetRandomLookTarget<BO> runFor(ToIntFunction<BO> timeProvider) {
		return (SetRandomLookTarget<BO>)super.runFor(timeProvider);
	}
	
	/// Disable the tick-based timeout for this behaviour and instead rely exclusively on other conditions
	/// such as [#getMemoryRequirements()] failing or [#stopIf(Predicate)]
	@ApiStatus.NonExtendable
	@Override
	public SetRandomLookTarget<BO> noTimeout() {
		return (SetRandomLookTarget<BO>)super.noTimeout();
	}
	
	/// Set the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public SetRandomLookTarget<BO> cooldownFor(int ticks) {
		return (SetRandomLookTarget<BO>)super.cooldownFor(ticks);
	}
	
	/// Set the range of ticks that this behaviour should be prevented from starting again after it has finished<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is stopped
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public SetRandomLookTarget<BO> cooldownFor(int minTicks, int maxTicks) {
		return (SetRandomLookTarget<BO>)super.cooldownFor(minTicks, maxTicks);
	}
	
	/// Set a function to determine the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public SetRandomLookTarget<BO> cooldownFor(ToIntFunction<BO> timeProvider) {
		return (SetRandomLookTarget<BO>)super.cooldownFor(timeProvider);
	}
	
	/// Set an additional condition for the behaviour to be able to start
	///
	/// Prevents this behaviour starting unless this predicate returns true.
	///
	/// @param predicate The condition for starting
	@ApiStatus.NonExtendable
	@Override
	public SetRandomLookTarget<BO> startCondition(Predicate<BO> predicate) {
		return (SetRandomLookTarget<BO>)super.startCondition(predicate);
	}
	
	/// Set a condition under which the behaviour should automatically stop<br/>
	/// Has no effect on one-shot behaviours that don't tick or have a runtime
	/// 
	/// Stops the behaviour immediately if the predicate returns true, ready to run again
	@ApiStatus.NonExtendable
	@Override
	public SetRandomLookTarget<BO> stopIf(Predicate<BO> predicate) {
		return (SetRandomLookTarget<BO>)super.stopIf(predicate);
	}
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
	
	/// Check any extra conditions required for this behaviour to start
	///
	/// By this stage, memory conditions from [#getMemoryRequirements()] have already been checked
	///
	/// @return Whether the conditions have been met to start the behaviour
	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, BO entity) {
		return RandomUtil.percentChance(this.runChance.applyAsFloat(entity));
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
		double angle = Mth.TWO_PI * entity.getRandom().nextDouble();

		BrainUtil.setForgettableMemory(entity, MemoryModuleType.LOOK_TARGET, new ExactPositionTracker(entity.getEyePosition().add(Math.cos(angle), 0, Math.sin(angle))), this.lookTime.applyAsInt(entity));
	}
	//</editor-fold>
}
