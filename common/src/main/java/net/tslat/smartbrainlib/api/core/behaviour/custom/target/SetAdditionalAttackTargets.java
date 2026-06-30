package net.tslat.smartbrainlib.api.core.behaviour.custom.target;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.player.Player;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;
import net.tslat.smartbrainlib.library.interfaces.TriPredicate;
import net.tslat.smartbrainlib.library.object.MemoryTest;
import net.tslat.smartbrainlib.util.BrainUtil;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/// Special-case behaviour for setting secondary, tertiary, etc. attack targets
///
/// This is useful for entities that concurrently target multiple entities, and use additional memory modules to store the additional targets<br/>
/// Uses [MemoryModuleType#NEAREST_PLAYERS] and [MemoryModuleType#NEAREST_VISIBLE_LIVING_ENTITIES] for its retrieval of additional targets<br/>
/// This behaviour will skip the usual pathing and alerting functionality as it is assumed they will be handled under the primary target
///
/// @param <BO> The brain owner entity
public class SetAdditionalAttackTargets<BO extends Mob> extends ExtendedBehaviour<BO> {
	protected static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(1).usesMemories(MemoryModuleType.NEAREST_PLAYERS, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
	protected final List<MemoryModuleType<? extends LivingEntity>> targetingMemories = new ReferenceArrayList<>();

	protected TriPredicate<BO, MemoryModuleType<? extends LivingEntity>, LivingEntity> canAttackPredicate = (owner, memory, target) -> target.isAlive() && target instanceof Player player && !player.getAbilities().invulnerable;
	protected TriConsumer<BO, MemoryModuleType<? extends LivingEntity>, LivingEntity> targetCallback = (owner, memory, target) -> {};
	protected boolean allowDuplicateTargets = true;

	/// Set the predicate to determine whether a given entity should be additionally targeted or not
	public SetAdditionalAttackTargets<BO> canTargetIf(TriPredicate<BO, MemoryModuleType<? extends LivingEntity>, LivingEntity> predicate) {
		this.canAttackPredicate = predicate;

		return this;
	}

	/// Sets the callback for when a target is being successfully set to a memory
	public SetAdditionalAttackTargets<BO> whenTargeting(TriConsumer<BO, MemoryModuleType<? extends LivingEntity>, LivingEntity> callback) {
		this.targetCallback = callback;

		return this;
	}

	/// Add [memories][MemoryModuleType] to the list of tertiary memories to set targets for
	///
	/// This appends to any existing memories already added to this behaviour, and the functionality of this behaviour is order-dependent
	@SuppressWarnings("unchecked")
	public SetAdditionalAttackTargets<BO> withMemories(MemoryModuleType<? extends LivingEntity>... targetMemories) {
		this.targetingMemories.addAll(Arrays.asList(targetMemories));

		return this;
	}

	/// Allow for the tertiary target memories to be set to the same as the previous modules if no new target is available
	public SetAdditionalAttackTargets<BO> allowDuplicateTargeting() {
		this.allowDuplicateTargets = true;

		return this;
	}
	
	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// Set a callback for when the behaviour successfully begins
	///
	/// This is called immediately prior to [ExtendedBehaviour#start(LivingEntity)]
	@ApiStatus.NonExtendable
	@Override
	public SetAdditionalAttackTargets<BO> whenStarting(Consumer<BO> callback) {
		return (SetAdditionalAttackTargets<BO>)super.whenStarting(callback);
	}
	
	/// Set a callback for when the behaviour stops
	///
	/// This is called immediately prior to [ExtendedBehaviour#stop(LivingEntity)]
	///
	/// Note that the behaviour stopping does not necessarily mean it was successful
	@ApiStatus.NonExtendable
	@Override
	public SetAdditionalAttackTargets<BO> whenStopping(Consumer<BO> callback) {
		return (SetAdditionalAttackTargets<BO>)super.whenStopping(callback);
	}
	
	/// Set the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public SetAdditionalAttackTargets<BO> runFor(int ticks) {
		return (SetAdditionalAttackTargets<BO>)super.runFor(ticks);
	}
	
	/// Set the range of ticks the behaviour should try to run for, once started<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is run
	/// 
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public SetAdditionalAttackTargets<BO> runFor(int minTicks, int maxTicks) {
		return (SetAdditionalAttackTargets<BO>)super.runFor(minTicks, maxTicks);
	}
	
	/// Set a function to determine the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public SetAdditionalAttackTargets<BO> runFor(ToIntFunction<BO> timeProvider) {
		return (SetAdditionalAttackTargets<BO>)super.runFor(timeProvider);
	}
	
	/// Disable the tick-based timeout for this behaviour and instead rely exclusively on other conditions
	/// such as [#getMemoryRequirements()] failing or [#stopIf(Predicate)]
	@ApiStatus.NonExtendable
	@Override
	public SetAdditionalAttackTargets<BO> noTimeout() {
		return (SetAdditionalAttackTargets<BO>)super.noTimeout();
	}
	
	/// Set the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public SetAdditionalAttackTargets<BO> cooldownFor(int ticks) {
		return (SetAdditionalAttackTargets<BO>)super.cooldownFor(ticks);
	}
	
	/// Set the range of ticks that this behaviour should be prevented from starting again after it has finished<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is stopped
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public SetAdditionalAttackTargets<BO> cooldownFor(int minTicks, int maxTicks) {
		return (SetAdditionalAttackTargets<BO>)super.cooldownFor(minTicks, maxTicks);
	}
	
	/// Set a function to determine the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public SetAdditionalAttackTargets<BO> cooldownFor(ToIntFunction<BO> timeProvider) {
		return (SetAdditionalAttackTargets<BO>)super.cooldownFor(timeProvider);
	}
	
	/// Set an additional condition for the behaviour to be able to start
	///
	/// Prevents this behaviour starting unless this predicate returns true.
	///
	/// @param predicate The condition for starting
	@ApiStatus.NonExtendable
	@Override
	public SetAdditionalAttackTargets<BO> startCondition(Predicate<BO> predicate) {
		return (SetAdditionalAttackTargets<BO>)super.startCondition(predicate);
	}
	
	/// Set a condition under which the behaviour should automatically stop<br/>
	/// Has no effect on one-shot behaviours that don't tick or have a runtime
	/// 
	/// Stops the behaviour immediately if the predicate returns true, ready to run again
	@ApiStatus.NonExtendable
	@Override
	public SetAdditionalAttackTargets<BO> stopIf(Predicate<BO> predicate) {
		return (SetAdditionalAttackTargets<BO>)super.stopIf(predicate);
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
		final Brain<?> brain = entity.getBrain();

		if (!BrainUtil.hasMemory(brain, MemoryModuleType.NEAREST_PLAYERS) && !BrainUtil.hasMemory(brain, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES))
			return false;
		
		for (MemoryModuleType<? extends LivingEntity> memory : this.targetingMemories) {
			if (!BrainUtil.hasMemory(brain, memory))
				return true;
		}

		return false;
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
		final Brain<?> brain = entity.getBrain();
		final Set<LivingEntity> targetPool = new ReferenceOpenHashSet<>();

		BrainUtil.withMemory(brain, MemoryModuleType.NEAREST_PLAYERS, targetPool::addAll);
		BrainUtil.withMemory(brain, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, memory -> memory.findAll(_ -> true).forEach(targetPool::add));

		if (targetPool.isEmpty())
			return;
		
		for (Iterator<MemoryModuleType<? extends LivingEntity>> memoryIterator = this.targetingMemories.iterator(); memoryIterator.hasNext() && !targetPool.isEmpty();) {
			final MemoryModuleType<? extends LivingEntity> memory = memoryIterator.next();
			
			if (!BrainUtil.hasMemory(brain, memory))
				continue;
			
			for (Iterator<LivingEntity> targetIterator = targetPool.iterator(); targetIterator.hasNext();) {
				final LivingEntity target = targetIterator.next();
				
				if (this.canAttackPredicate.test(entity, memory, target)) {
					//noinspection rawtypes,unchecked
					BrainUtil.setMemory(brain, (MemoryModuleType)memory, target);
					this.targetCallback.accept(entity, memory, target);
					
					if (!this.allowDuplicateTargets)
						targetIterator.remove();
					
					break;
				}
			}
		}
	}
	//</editor-fold>
}
