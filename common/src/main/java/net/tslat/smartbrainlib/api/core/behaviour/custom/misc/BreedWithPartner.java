package net.tslat.smartbrainlib.api.core.behaviour.custom.misc;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.animal.Animal;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;
import net.tslat.smartbrainlib.library.interfaces.ToFloatBiFunction;
import net.tslat.smartbrainlib.library.object.MemoryTest;
import net.tslat.smartbrainlib.util.BrainUtil;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.function.*;

/// Functional replacement for vanilla's [AnimalMakeLove][net.minecraft.world.entity.ai.behavior.AnimalMakeLove]
///
/// Makes the entity find, move to, and breed with its target mate, producing offspring
///
/// @param <BO> The brain owner entity
public class BreedWithPartner<BO extends Animal> extends ExtendedBehaviour<BO> {
	protected static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(4).hasMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).noMemory(MemoryModuleType.BREED_TARGET).usesMemories(MemoryModuleType.LOOK_TARGET, MemoryModuleType.WALK_TARGET);

	protected ToFloatBiFunction<BO, Animal> speedModifier = (_, _) -> 1f;
	protected ToIntBiFunction<BO, Animal> closeEnoughDist = (_, _) -> 2;
	protected ToIntBiFunction<BO, Animal> breedTime = (entity, _) -> entity.getRandom().nextInt(60, 110);
	protected BiPredicate<BO, Animal> validPartner = (entity, partner) -> entity.getType() == partner.getType() && entity.canMate(partner);

	protected int childBreedTick = -1;
	protected @Nullable Animal partner = null;

	public BreedWithPartner() {
		noTimeout();
	}
	
	/// Set the length of time (in ticks) that it takes to fully breed
	@ApiStatus.NonExtendable
	public BreedWithPartner<BO> validPartners(EntityType<?> entityType) {
		return validPartners((entity, partner) -> partner.getType() == entityType && entity.canMate(partner));
	}
	
	/// Set the length of time (in ticks) that it takes to fully breed
	@ApiStatus.NonExtendable
	public BreedWithPartner<BO> validPartners(Class<? extends Animal> entityClass) {
		return validPartners((entity, partner) -> entityClass.isAssignableFrom(partner.getClass()) && entity.canMate(partner));
	}
	
	/// Set the length of time (in ticks) that it takes to fully breed
	@ApiStatus.NonExtendable
	public BreedWithPartner<BO> validPartners(TagKey<EntityType<?>> tag) {
		return validPartners((entity, partner) -> partner.is(tag) && entity.canMate(partner));
	}
	
	/// Set a predicate to determine whether a given [Animal] is a valid breeding partner, ready for breeding
	///
	/// @see #validPartner
	@ApiStatus.NonExtendable
	public BreedWithPartner<BO> validPartners(BiPredicate<BO, Animal> predicate) {
		this.validPartner = predicate;
		
		return this;
	}
	
	/// Set the length of time (in ticks) that it takes to fully breed
	@ApiStatus.NonExtendable
	public BreedWithPartner<BO> timeToBreed(int ticks) {
		return timeToBreed((_, _) -> ticks);
	}
	
	/// Set a function to determine the length of time (in ticks) that it takes to fully breed
	@ApiStatus.NonExtendable
	public BreedWithPartner<BO> timeToBreed(ToIntBiFunction<BO, Animal> function) {
		this.breedTime = function;
		
		return this;
	}
	
	/// Set the movement speed modifier for the path when chosen
	@ApiStatus.NonExtendable
	public BreedWithPartner<BO> speedModifier(float modifier) {
		return speedModifier((_, _) -> modifier);
	}
	
	/// Set the function to determine the movement speed modifier for the path when chosen
	@ApiStatus.NonExtendable
	public BreedWithPartner<BO> speedModifier(ToFloatBiFunction<BO, Animal> function) {
		this.speedModifier = function;
		
		return this;
	}
	
	/// Set the distance (in blocks) that the entity should be considered 'close enough' to the target position to have arrived
	@ApiStatus.NonExtendable
	public BreedWithPartner<BO> closeEnoughDist(int distance) {
		return closeEnoughDist((_, _) -> distance);
	}
	
	/// Set the function to determine the distance (in blocks) that the entity should be considered 'close enough' to the target position to have arrived
	@ApiStatus.NonExtendable
	public BreedWithPartner<BO> closeEnoughDist(ToIntBiFunction<BO, Animal> function) {
		this.closeEnoughDist = function;
		
		return this;
	}
	
	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// Set a callback for when the behaviour successfully begins
	///
	/// This is called immediately prior to [ExtendedBehaviour#start(LivingEntity)]
	@ApiStatus.NonExtendable
	@Override
	public BreedWithPartner<BO> whenStarting(Consumer<BO> callback) {
		return (BreedWithPartner<BO>)super.whenStarting(callback);
	}
	
	/// Set a callback for when the behaviour stops
	///
	/// This is called immediately prior to [ExtendedBehaviour#stop(LivingEntity)]
	///
	/// Note that the behaviour stopping does not necessarily mean it was successful
	@ApiStatus.NonExtendable
	@Override
	public BreedWithPartner<BO> whenStopping(Consumer<BO> callback) {
		return (BreedWithPartner<BO>)super.whenStopping(callback);
	}
	
	/// Set the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public BreedWithPartner<BO> runFor(int ticks) {
		return (BreedWithPartner<BO>)super.runFor(ticks);
	}
	
	/// Set the range of ticks the behaviour should try to run for, once started<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is run
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public BreedWithPartner<BO> runFor(int minTicks, int maxTicks) {
		return (BreedWithPartner<BO>)super.runFor(minTicks, maxTicks);
	}
	
	/// Set a function to determine the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public BreedWithPartner<BO> runFor(ToIntFunction<BO> timeProvider) {
		return (BreedWithPartner<BO>)super.runFor(timeProvider);
	}
	
	/// Disable the tick-based timeout for this behaviour and instead rely exclusively on other conditions
	/// such as [#getMemoryRequirements()] failing or [#stopIf(Predicate)]
	@ApiStatus.NonExtendable
	@Override
	public BreedWithPartner<BO> noTimeout() {
		return (BreedWithPartner<BO>)super.noTimeout();
	}
	
	/// Set the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public BreedWithPartner<BO> cooldownFor(int ticks) {
		return (BreedWithPartner<BO>)super.cooldownFor(ticks);
	}
	
	/// Set the range of ticks that this behaviour should be prevented from starting again after it has finished<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is stopped
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public BreedWithPartner<BO> cooldownFor(int minTicks, int maxTicks) {
		return (BreedWithPartner<BO>)super.cooldownFor(minTicks, maxTicks);
	}
	
	/// Set a function to determine the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public BreedWithPartner<BO> cooldownFor(ToIntFunction<BO> timeProvider) {
		return (BreedWithPartner<BO>)super.cooldownFor(timeProvider);
	}
	
	/// Set an additional condition for the behaviour to be able to start
	///
	/// Prevents this behaviour starting unless this predicate returns true
	@ApiStatus.NonExtendable
	@Override
	public BreedWithPartner<BO> startCondition(Predicate<BO> predicate) {
		return (BreedWithPartner<BO>)super.startCondition(predicate);
	}
	
	/// Set a condition under which the behaviour should automatically stop<br/>
	/// Has no effect on one-shot behaviours that don't tick or have a runtime
	///
	/// Stops the behaviour immediately if the predicate returns true, ready to run again
	@ApiStatus.NonExtendable
	@Override
	public BreedWithPartner<BO> stopIf(Predicate<BO> predicate) {
		return (BreedWithPartner<BO>)super.stopIf(predicate);
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
		if (!entity.isInLove())
			return false;

		this.partner = findPartner(entity);

		return this.partner != null;
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
		this.childBreedTick = entity.tickCount + this.breedTime.applyAsInt(entity, this.partner);

		BrainUtil.setMemory(entity, MemoryModuleType.BREED_TARGET, this.partner);
		BrainUtil.setMemory(this.partner, MemoryModuleType.BREED_TARGET, entity);
		BehaviorUtils.lockGazeAndWalkToEachOther(entity, this.partner, this.speedModifier.applyAsFloat(entity, this.partner), this.closeEnoughDist.applyAsInt(entity, this.partner));
	}
	
	/// Check any additional conditions for whether the behaviour should continue running<br/>
	/// This is checked before [ExtendedBehaviour#tick(LivingEntity)]
	///
	/// The behaviour's [runtime][#runFor] has already been checked at this stage, and [#stopIf(Predicate)] will be checked immediately after this method if returning true
	///
	/// Memories are not guaranteed to be in their required state here, so if you have required memories, it might be worth checking them here
	@MustBeInvokedByOverriders
	@Override
	protected boolean shouldKeepRunning(BO entity) {
		return this.partner != null && this.partner.isAlive() && entity.tickCount <= this.childBreedTick && BrainUtil.canSee(entity, this.partner) && this.validPartner.test(entity, this.partner);
	}
	
	/// Run the per-tick behaviour for this behaviour<br/>
	/// This is called <u>every tick</u> this behaviour is active, so be aware of any performance-intensive functionality handled here
	///
	/// [ExtendedBehaviour#shouldKeepRunning(LivingEntity)] and [#stopIf(Predicate)] have both been checked immediately prior to this method being called
	///
	/// NOTE: Memory requirements are __not__ guaranteed at this stage. If you are retrieving brain memories, you may need to check their presence before use
	@Override
	protected void tick(BO entity) {
		//noinspection DataFlowIssue
		final int closeEnoughDist = this.closeEnoughDist.applyAsInt(entity, this.partner);
		BehaviorUtils.lockGazeAndWalkToEachOther(entity, this.partner, this.speedModifier.applyAsFloat(entity, this.partner), closeEnoughDist);

		if (entity.closerThan(this.partner, closeEnoughDist) && entity.tickCount == this.childBreedTick) {
			entity.spawnChildFromBreeding((ServerLevel)entity.level(), this.partner);
			BrainUtil.clearMemory(entity, MemoryModuleType.BREED_TARGET);
			BrainUtil.clearMemory(this.partner, MemoryModuleType.BREED_TARGET);
		}
	}
	
	/// Called when this behaviour is instructed to stop<br/>
	/// This may be due to timing out, failing to meet a condition, or for any other reason
	///
	/// This method is non-negotiable, and it must be safe to assume that if this method is called, this behaviour is safe to discard
	@MustBeInvokedByOverriders
	@Override
	protected void stop(BO entity) {
		BrainUtil.clearMemories(entity, MemoryModuleType.BREED_TARGET, MemoryModuleType.LOOK_TARGET, MemoryModuleType.WALK_TARGET);

		if (this.partner != null)
			BrainUtil.clearMemories(this.partner, MemoryModuleType.BREED_TARGET, MemoryModuleType.LOOK_TARGET, MemoryModuleType.WALK_TARGET);

		this.childBreedTick = -1;
		this.partner = null;
	}

	/// Find a valid partner to breed with, typically from the [MemoryModuleType#NEAREST_VISIBLE_LIVING_ENTITIES] memory list
	protected @Nullable Animal findPartner(BO entity) {
		return BrainUtil.memoryOrDefault(entity, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, NearestVisibleLivingEntities.empty())
		                .findClosest(entity2 -> entity2 instanceof Animal animal && this.validPartner.test(entity, animal))
		                .map(Animal.class::cast)
		                .orElse(null);
	}
	//</editor-fold>
}
