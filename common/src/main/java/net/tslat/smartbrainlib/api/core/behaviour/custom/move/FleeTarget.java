package net.tslat.smartbrainlib.api.core.behaviour.custom.move;

import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;
import net.tslat.smartbrainlib.library.interfaces.ToFloatBiFunction;
import net.tslat.smartbrainlib.library.interfaces.ToFloatTriFunction;
import net.tslat.smartbrainlib.library.interfaces.TriPredicate;
import net.tslat.smartbrainlib.library.object.MemoryTest;
import net.tslat.smartbrainlib.util.BrainUtil;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/// Set a [MemoryModuleType#WALK_TARGET] away from the current [MemoryModuleType#ATTACK_TARGET], essentially acting as a fleeing mechanic
///
/// @param <BO> The brain owner entity
public class FleeTarget<BO extends PathfinderMob> extends AvoidEntity<BO> {
	protected static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(2).hasMemory(MemoryModuleType.ATTACK_TARGET).usesMemories(MemoryModuleType.WALK_TARGET);
	
	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// Set the distance (in blocks) that the entity should start avoiding the target entity
	@ApiStatus.NonExtendable
	public FleeTarget<BO> noCloserThan(float blocks) {
		return (FleeTarget<BO>)super.noCloserThan(blocks);
	}
	
	/// Set a function to determine the distance (in blocks) that the entity should start avoiding the target entity
	@ApiStatus.NonExtendable
	public FleeTarget<BO> noCloserThan(ToFloatBiFunction<BO, LivingEntity> function) {
		return (FleeTarget<BO>)super.noCloserThan(function);
	}
	
	/// Set the distance (in blocks) the entity should put between it and the entity to avoid once triggered
	@ApiStatus.NonExtendable
	public FleeTarget<BO> runNBlocksAway(float blocks) {
		return (FleeTarget<BO>)super.runNBlocksAway(blocks);
	}
	
	/// Set a function to determine the distance (in blocks) the entity should put between it and the target entity to avoid once triggered
	@ApiStatus.NonExtendable
	public FleeTarget<BO> runNBlocksAway(ToFloatBiFunction<BO, LivingEntity> function) {
		return (FleeTarget<BO>)super.runNBlocksAway(function);
	}
	
	/// Set the behaviour to avoid entities of a specific [TagKey]
	@ApiStatus.NonExtendable
	public FleeTarget<BO> avoiding(TagKey<EntityType<?>> tag) {
		return (FleeTarget<BO>)super.avoiding(tag);
	}
	
	/// Set the behaviour to avoid entities of a specific [EntityType]
	@ApiStatus.NonExtendable
	public FleeTarget<BO> avoiding(EntityType<?> entityType) {
		return (FleeTarget<BO>)super.avoiding(entityType);
	}
	
	/// Set the behaviour to avoid entities of, or a subclass of a specific class
	@ApiStatus.NonExtendable
	public FleeTarget<BO> avoiding(Class<? extends LivingEntity> entityClass) {
		return (FleeTarget<BO>)super.avoiding(entityClass);
	}
	
	/// Set a predicate to determine which entities to try to avoid
	@ApiStatus.NonExtendable
	public FleeTarget<BO> avoiding(BiPredicate<BO, LivingEntity> predicate) {
		return (FleeTarget<BO>)super.avoiding(predicate);
	}
	
	/// Set the movement speed modifier for the path when chosen
	@ApiStatus.NonExtendable
	public FleeTarget<BO> speedModifier(float modifier) {
		return (FleeTarget<BO>)super.speedModifier(modifier);
	}
	
	/// Set the function to determine the movement speed modifier for the path when chosen
	@ApiStatus.NonExtendable
	public FleeTarget<BO> speedModifier(ToFloatTriFunction<BO, LivingEntity, Vec3> function) {
		return (FleeTarget<BO>)super.speedModifier(function);
	}
	
	/// Sets a predicate to check whether a target movement position is valid or not
	@ApiStatus.NonExtendable
	public FleeTarget<BO> isValidPositionIf(TriPredicate<BO, LivingEntity, Vec3> predicate) {
		return (FleeTarget<BO>)super.isValidPositionIf(predicate);
	}
	
	/// Set a callback for when the behaviour successfully begins
	///
	/// This is called immediately prior to [ExtendedBehaviour#start(LivingEntity)]
	@ApiStatus.NonExtendable
	@Override
	public FleeTarget<BO> whenStarting(Consumer<BO> callback) {
		return (FleeTarget<BO>)super.whenStarting(callback);
	}
	
	/// Set a callback for when the behaviour stops
	///
	/// This is called immediately prior to [ExtendedBehaviour#stop(LivingEntity)]
	///
	/// Note that the behaviour stopping does not necessarily mean it was successful
	@ApiStatus.NonExtendable
	@Override
	public FleeTarget<BO> whenStopping(Consumer<BO> callback) {
		return (FleeTarget<BO>)super.whenStopping(callback);
	}
	
	/// Set the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public FleeTarget<BO> runFor(int ticks) {
		return (FleeTarget<BO>)super.runFor(ticks);
	}
	
	/// Set the range of ticks the behaviour should try to run for, once started<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is run
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public FleeTarget<BO> runFor(int minTicks, int maxTicks) {
		return (FleeTarget<BO>)super.runFor(minTicks, maxTicks);
	}
	
	/// Set a function to determine the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public FleeTarget<BO> runFor(ToIntFunction<BO> timeProvider) {
		return (FleeTarget<BO>)super.runFor(timeProvider);
	}
	
	/// Disable the tick-based timeout for this behaviour and instead rely exclusively on other conditions
	/// such as [#getMemoryRequirements()] failing or [#stopIf(Predicate)]
	@ApiStatus.NonExtendable
	@Override
	public FleeTarget<BO> noTimeout() {
		return (FleeTarget<BO>)super.noTimeout();
	}
	
	/// Set the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public FleeTarget<BO> cooldownFor(int ticks) {
		return (FleeTarget<BO>)super.cooldownFor(ticks);
	}
	
	/// Set the range of ticks that this behaviour should be prevented from starting again after it has finished<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is stopped
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public FleeTarget<BO> cooldownFor(int minTicks, int maxTicks) {
		return (FleeTarget<BO>)super.cooldownFor(minTicks, maxTicks);
	}
	
	/// Set a function to determine the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public FleeTarget<BO> cooldownFor(ToIntFunction<BO> timeProvider) {
		return (FleeTarget<BO>)super.cooldownFor(timeProvider);
	}
	
	/// Set an additional condition for the behaviour to be able to start
	///
	/// Prevents this behaviour starting unless this predicate returns true
	@ApiStatus.NonExtendable
	@Override
	public FleeTarget<BO> startCondition(Predicate<BO> predicate) {
		return (FleeTarget<BO>)super.startCondition(predicate);
	}
	
	/// Set a condition under which the behaviour should automatically stop<br/>
	/// Has no effect on one-shot behaviours that don't tick or have a runtime
	///
	/// Stops the behaviour immediately if the predicate returns true, ready to run again
	@ApiStatus.NonExtendable
	@Override
	public FleeTarget<BO> stopIf(Predicate<BO> predicate) {
		return (FleeTarget<BO>)super.stopIf(predicate);
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
	
	/// Get the nearest entity to actively run away from, if any
	@Override
	protected @Nullable LivingEntity getEntityToAvoid(BO entity) {
		return BrainUtil.getTargetOfEntity(entity);
	}
	
	/// Run the core functionality this behaviour has when starting<br/>
	/// This method is called once per behaviour run
	///
	/// By this stage any memory requirements set in [#getMemoryRequirements()] are true, so any memories paired with [MemoryStatus#VALUE_PRESENT] are safe to retrieve
	///
	/// If you are not making a custom behaviour and are instead just using an existing behaviour; you would use [#whenStarting(Consumer)] instead of overriding this method
	@Override
	protected void start(BO entity) {
		super.start(entity);
		BrainUtil.clearMemory(entity, MemoryModuleType.ATTACK_TARGET);
		entity.setAggressive(false);
	}
	//</editor-fold>
}