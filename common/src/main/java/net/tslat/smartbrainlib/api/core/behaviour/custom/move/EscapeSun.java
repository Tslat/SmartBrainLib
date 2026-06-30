package net.tslat.smartbrainlib.api.core.behaviour.custom.move;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.entity.ai.goal.FleeSunGoal;
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
import org.jspecify.annotations.Nullable;

import java.util.Set;
import java.util.function.*;

/// Sets the [walk target][MemoryModuleType#WALK_TARGET] to a safe position if caught in the sun
///
/// @see FleeSunGoal
/// @param <BO> The brain owner entity
public class EscapeSun<BO extends PathfinderMob> extends ExtendedBehaviour<BO> {
	protected static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(2).noMemory(MemoryModuleType.ATTACK_TARGET).usesMemory(MemoryModuleType.WALK_TARGET);

	protected Predicate<BO> hideIf = entity -> entity.level().isBrightOutside() && entity.isOnFire() && entity.level().canSeeSky(entity.blockPosition()) && entity.hasItemInSlot(EquipmentSlot.HEAD);
	protected ToFloatBiFunction<BO, Vec3> speedModifier = (_, _) -> 1f;
	protected Function<BO, SquareRadius> searchRadius = _ -> new SquareRadius(10, 3);
	protected BiPredicate<BO, BlockPos> validPositionPredicate = (entity, pos) -> !entity.level().canSeeSky(pos) && entity.getWalkTargetValue(pos) < 0;
	
	protected @Nullable WalkTarget hidePos = null;

	/// Override the predicate that determines when the entity should hide
	@ApiStatus.NonExtendable
	public EscapeSun<BO> hideIf(Predicate<BO> predicate) {
		this.hideIf = predicate;
		
		return this;
	}
	
	/// Set the movement speed modifier for the path when chosen
	@ApiStatus.NonExtendable
	public EscapeSun<BO> speedModifier(float modifier) {
		return speedModifier((_, _) -> modifier);
	}
	
	/// Set the function to determine the movement speed modifier for the path when chosen
	@ApiStatus.NonExtendable
	public EscapeSun<BO> speedModifier(ToFloatBiFunction<BO, Vec3> function) {
		this.speedModifier = function;
		
		return this;
	}
	
	/// Sets a predicate to check whether a target movement position is valid or not
	@ApiStatus.NonExtendable
	public EscapeSun<BO> isValidPositionIf(BiPredicate<BO, BlockPos> predicate) {
		this.validPositionPredicate = predicate;
		
		return this;
	}
	
	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// Set a callback for when the behaviour successfully begins
	///
	/// This is called immediately prior to [ExtendedBehaviour#start(LivingEntity)]
	@ApiStatus.NonExtendable
	@Override
	public EscapeSun<BO> whenStarting(Consumer<BO> callback) {
		return (EscapeSun<BO>)super.whenStarting(callback);
	}
	
	/// Set a callback for when the behaviour stops
	///
	/// This is called immediately prior to [ExtendedBehaviour#stop(LivingEntity)]
	///
	/// Note that the behaviour stopping does not necessarily mean it was successful
	@ApiStatus.NonExtendable
	@Override
	public EscapeSun<BO> whenStopping(Consumer<BO> callback) {
		return (EscapeSun<BO>)super.whenStopping(callback);
	}
	
	/// Set the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public EscapeSun<BO> runFor(int ticks) {
		return (EscapeSun<BO>)super.runFor(ticks);
	}
	
	/// Set the range of ticks the behaviour should try to run for, once started<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is run
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public EscapeSun<BO> runFor(int minTicks, int maxTicks) {
		return (EscapeSun<BO>)super.runFor(minTicks, maxTicks);
	}
	
	/// Set a function to determine the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public EscapeSun<BO> runFor(ToIntFunction<BO> timeProvider) {
		return (EscapeSun<BO>)super.runFor(timeProvider);
	}
	
	/// Disable the tick-based timeout for this behaviour and instead rely exclusively on other conditions
	/// such as [#getMemoryRequirements()] failing or [#stopIf(Predicate)]
	@ApiStatus.NonExtendable
	@Override
	public EscapeSun<BO> noTimeout() {
		return (EscapeSun<BO>)super.noTimeout();
	}
	
	/// Set the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public EscapeSun<BO> cooldownFor(int ticks) {
		return (EscapeSun<BO>)super.cooldownFor(ticks);
	}
	
	/// Set the range of ticks that this behaviour should be prevented from starting again after it has finished<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is stopped
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public EscapeSun<BO> cooldownFor(int minTicks, int maxTicks) {
		return (EscapeSun<BO>)super.cooldownFor(minTicks, maxTicks);
	}
	
	/// Set a function to determine the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public EscapeSun<BO> cooldownFor(ToIntFunction<BO> timeProvider) {
		return (EscapeSun<BO>)super.cooldownFor(timeProvider);
	}
	
	/// Set an additional condition for the behaviour to be able to start
	///
	/// Prevents this behaviour starting unless this predicate returns true
	@ApiStatus.NonExtendable
	@Override
	public EscapeSun<BO> startCondition(Predicate<BO> predicate) {
		return (EscapeSun<BO>)super.startCondition(predicate);
	}
	
	/// Set a condition under which the behaviour should automatically stop<br/>
	/// Has no effect on one-shot behaviours that don't tick or have a runtime
	///
	/// Stops the behaviour immediately if the predicate returns true, ready to run again
	@ApiStatus.NonExtendable
	@Override
	public EscapeSun<BO> stopIf(Predicate<BO> predicate) {
		return (EscapeSun<BO>)super.stopIf(predicate);
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
		if (this.hidePos != null && this.hidePos == BrainUtil.getMemory(entity, MemoryModuleType.WALK_TARGET))
			return false;
		
		this.hidePos = null;
		final Vec3 hidePos = getHidePos(entity);
		
		if (hidePos != null)
			this.hidePos = new WalkTarget(hidePos, this.speedModifier.applyAsFloat(entity, hidePos), 0);
		
		return this.hidePos != null;
	}
	
	/// Run the core functionality this behaviour has when starting<br/>
	/// This method is called once per behaviour run
	///
	/// By this stage any memory requirements set in [#getMemoryRequirements()] are true, so any memories paired with [MemoryStatus#VALUE_PRESENT] are safe to retrieve
	///
	/// If you are not making a custom behaviour and are instead just using an existing behaviour; you would use [#whenStarting(Consumer)] instead of overriding this method
	@Override
	protected void start(BO entity) {
		//noinspection DataFlowIssue
		BrainUtil.setMemory(entity, MemoryModuleType.WALK_TARGET, this.hidePos);
	}
	
	/// Attempt to find a suitable nearby hiding spot from the sun
	protected @Nullable Vec3 getHidePos(BO entity) {
		final SquareRadius searchRadius = this.searchRadius.apply(entity);
		
		for (int i = 0; i < 10; ++i) {
			final Vec3 hidePos = searchRadius.getRandomPos(entity.position(), entity.getRandom());

			if (this.validPositionPredicate.test(entity, BlockPos.containing(hidePos)))
				return hidePos;
		}

		return null;
	}
	//</editor-fold>
}
