package net.tslat.smartbrainlib.api.core.behaviour.custom.move;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;
import net.tslat.smartbrainlib.library.interfaces.ToFloatBiFunction;
import net.tslat.smartbrainlib.library.interfaces.ToFloatTriFunction;
import net.tslat.smartbrainlib.library.interfaces.TriPredicate;
import net.tslat.smartbrainlib.library.object.MemoryTest;
import net.tslat.smartbrainlib.util.BrainUtil;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/// Attempt to avoid specified entities by setting the [MemoryModuleType#WALK_TARGET] away from it
///
/// @param <BO> The brain owner entity
public class AvoidEntity<BO extends PathfinderMob> extends ExtendedBehaviour<BO> {
	protected static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(1).hasMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).usesMemories(MemoryModuleType.WALK_TARGET);

	protected BiPredicate<BO, LivingEntity> avoidIf = (_, _) -> false;
	protected ToFloatBiFunction<BO, LivingEntity> tooCloseDist = (_, _) -> 3;
	protected ToFloatBiFunction<BO, LivingEntity> safeDist = (_, _) -> 7;
	protected ToFloatTriFunction<BO, LivingEntity, Vec3> speedModifier = (_, _, _) -> 1;
	protected TriPredicate<BO, LivingEntity, Vec3> validPositionPredicate = (_, _, _) -> true;

	protected @Nullable WalkTarget runPos = null;

	/// Set the distance (in blocks) that the entity should start avoiding the target entity
	@ApiStatus.NonExtendable
	public AvoidEntity<BO> noCloserThan(float blocks) {
		return noCloserThan((_, _) -> blocks);
	}

	/// Set a function to determine the distance (in blocks) that the entity should start avoiding the target entity
	@ApiStatus.NonExtendable
	public AvoidEntity<BO> noCloserThan(ToFloatBiFunction<BO, LivingEntity> function) {
		this.tooCloseDist = function;

		return this;
	}

	/// Set the distance (in blocks) the entity should put between it and the entity to avoid once triggered
	@ApiStatus.NonExtendable
	public AvoidEntity<BO> runNBlocksAway(float blocks) {
		return runNBlocksAway((_, _) -> blocks);
	}

	/// Set a function to determine the distance (in blocks) the entity should put between it and the target entity to avoid once triggered
	@ApiStatus.NonExtendable
	public AvoidEntity<BO> runNBlocksAway(ToFloatBiFunction<BO, LivingEntity> function) {
		this.safeDist = function;

		return this;
	}

	/// Set the behaviour to avoid entities of a specific [TagKey]
	@ApiStatus.NonExtendable
	public AvoidEntity<BO> avoiding(TagKey<EntityType<?>> tag) {
		return avoiding((_, target) -> target.isAlive() && target.is(tag));
	}

	/// Set the behaviour to avoid entities of a specific [EntityType]
	@ApiStatus.NonExtendable
	public AvoidEntity<BO> avoiding(EntityType<?> entityType) {
		return avoiding((_, target) -> target.isAlive() && target.is(entityType));
	}

	/// Set the behaviour to avoid entities of, or a subclass of a specific class
	@ApiStatus.NonExtendable
	public AvoidEntity<BO> avoiding(Class<? extends LivingEntity> entityClass) {
		return avoiding((_, target) -> target.isAlive() && entityClass.isAssignableFrom(target.getClass()));
	}

	/// Set a predicate to determine which entities to try to avoid
	@ApiStatus.NonExtendable
	public AvoidEntity<BO> avoiding(BiPredicate<BO, LivingEntity> predicate) {
		this.avoidIf = predicate;

		return this;
	}
	
	/// Set the movement speed modifier for the path when chosen
	@ApiStatus.NonExtendable
	public AvoidEntity<BO> speedModifier(float modifier) {
		return speedModifier((_, _, _) -> modifier);
	}
	
	/// Set the function to determine the movement speed modifier for the path when chosen
	@ApiStatus.NonExtendable
	public AvoidEntity<BO> speedModifier(ToFloatTriFunction<BO, LivingEntity, Vec3> function) {
		this.speedModifier = function;
		
		return this;
	}
	
	/// Sets a predicate to check whether a target movement position is valid or not
	@ApiStatus.NonExtendable
	public AvoidEntity<BO> isValidPositionIf(TriPredicate<BO, LivingEntity, Vec3> predicate) {
		this.validPositionPredicate = predicate;
		
		return this;
	}
	
	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// Set a callback for when the behaviour successfully begins
	///
	/// This is called immediately prior to [ExtendedBehaviour#start(LivingEntity)]
	@ApiStatus.NonExtendable
	@Override
	public AvoidEntity<BO> whenStarting(Consumer<BO> callback) {
		return (AvoidEntity<BO>)super.whenStarting(callback);
	}
	
	/// Set a callback for when the behaviour stops
	///
	/// This is called immediately prior to [ExtendedBehaviour#stop(LivingEntity)]
	///
	/// Note that the behaviour stopping does not necessarily mean it was successful
	@ApiStatus.NonExtendable
	@Override
	public AvoidEntity<BO> whenStopping(Consumer<BO> callback) {
		return (AvoidEntity<BO>)super.whenStopping(callback);
	}
	
	/// Set the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public AvoidEntity<BO> runFor(int ticks) {
		return (AvoidEntity<BO>)super.runFor(ticks);
	}
	
	/// Set the range of ticks the behaviour should try to run for, once started<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is run
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public AvoidEntity<BO> runFor(int minTicks, int maxTicks) {
		return (AvoidEntity<BO>)super.runFor(minTicks, maxTicks);
	}
	
	/// Set a function to determine the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public AvoidEntity<BO> runFor(ToIntFunction<BO> timeProvider) {
		return (AvoidEntity<BO>)super.runFor(timeProvider);
	}
	
	/// Disable the tick-based timeout for this behaviour and instead rely exclusively on other conditions
	/// such as [#getMemoryRequirements()] failing or [#stopIf(Predicate)]
	@ApiStatus.NonExtendable
	@Override
	public AvoidEntity<BO> noTimeout() {
		return (AvoidEntity<BO>)super.noTimeout();
	}
	
	/// Set the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public AvoidEntity<BO> cooldownFor(int ticks) {
		return (AvoidEntity<BO>)super.cooldownFor(ticks);
	}
	
	/// Set the range of ticks that this behaviour should be prevented from starting again after it has finished<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is stopped
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public AvoidEntity<BO> cooldownFor(int minTicks, int maxTicks) {
		return (AvoidEntity<BO>)super.cooldownFor(minTicks, maxTicks);
	}
	
	/// Set a function to determine the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public AvoidEntity<BO> cooldownFor(ToIntFunction<BO> timeProvider) {
		return (AvoidEntity<BO>)super.cooldownFor(timeProvider);
	}
	
	/// Set an additional condition for the behaviour to be able to start
	///
	/// Prevents this behaviour starting unless this predicate returns true
	@ApiStatus.NonExtendable
	@Override
	public AvoidEntity<BO> startCondition(Predicate<BO> predicate) {
		return (AvoidEntity<BO>)super.startCondition(predicate);
	}
	
	/// Set a condition under which the behaviour should automatically stop<br/>
	/// Has no effect on one-shot behaviours that don't tick or have a runtime
	///
	/// Stops the behaviour immediately if the predicate returns true, ready to run again
	@ApiStatus.NonExtendable
	@Override
	public AvoidEntity<BO> stopIf(Predicate<BO> predicate) {
		return (AvoidEntity<BO>)super.stopIf(predicate);
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
	@MustBeInvokedByOverriders
	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, BO entity) {
		if (this.runPos != null && this.runPos == BrainUtil.getMemory(entity, MemoryModuleType.WALK_TARGET))
			return false;
		
		LivingEntity avoidingEntity = getEntityToAvoid(entity);
		this.runPos = null;
		
		if (avoidingEntity == null)
			return false;
		
		final float runDist = this.safeDist.applyAsFloat(entity, avoidingEntity);
		final Vec3 runPos = DefaultRandomPos.getPosAway(entity, Mth.ceil(runDist) + 1, 7, avoidingEntity.position());
		
		if (runPos != null && this.validPositionPredicate.test(entity, avoidingEntity, runPos))
			this.runPos = new WalkTarget(runPos, this.speedModifier.applyAsFloat(entity, avoidingEntity, runPos), 0);
			
		return this.runPos != null;
	}
	
	/// Get the nearest entity to actively run away from, if any
	protected @Nullable LivingEntity getEntityToAvoid(BO entity) {
		return BrainUtil.memoryOrDefault(entity, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, NearestVisibleLivingEntities.empty())
		                .findClosest(target -> this.avoidIf.test(entity, target) && entity.distanceToSqr(target) < Mth.square(this.tooCloseDist.applyAsFloat(entity, target)))
				       .orElse(null);
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
		//noinspection DataFlowIssue
		BrainUtil.setMemory(entity, MemoryModuleType.WALK_TARGET, this.runPos);
	}
	//</editor-fold>
}
