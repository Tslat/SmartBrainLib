package net.tslat.smartbrainlib.api.core.behaviour.custom.move;

import it.unimi.dsi.fastutil.objects.Object2IntFunction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.player.Player;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;
import net.tslat.smartbrainlib.library.interfaces.ToFloatBiFunction;
import net.tslat.smartbrainlib.library.object.MemoryTest;
import net.tslat.smartbrainlib.util.BrainUtil;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/// [ExtendedBehaviour] equivalent of vanilla's [net.minecraft.world.entity.ai.behavior.FollowTemptation]<br/>
/// Has the entity follow a relevant [temptation target][MemoryModuleType#TEMPTING_PLAYER] as long as it's not already busy with something else
///
/// Will continue running for as long as the entity is being tempted
///
/// @param <BO> The brain owner entity
public class FollowTemptation<BO extends PathfinderMob> extends ExtendedBehaviour<BO> {
	protected static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(7).hasMemory(MemoryModuleType.TEMPTING_PLAYER).noMemory(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS).usesMemories(MemoryModuleType.LOOK_TARGET, MemoryModuleType.WALK_TARGET, MemoryModuleType.IS_TEMPTED, MemoryModuleType.IS_PANICKING, MemoryModuleType.BREED_TARGET);
	
	protected ToFloatBiFunction<BO, Player> speedModifier = (_, _) -> 1;
	protected ToFloatBiFunction<BO, Player> closeEnoughDist = (_, _) -> 2.5f;
	protected BiPredicate<BO, Player> shouldFollow = (entity, temptingPlayer) -> !entity.hasPassenger(temptingPlayer);
	protected Object2IntFunction<BO> temptationCooldown = _ -> 100;

	public FollowTemptation() {
		super();

		noTimeout();
	}
	
	/// Set a function to determine whether the entity should follow a given player
	@ApiStatus.NonExtendable
	public FollowTemptation<BO> followIf(final BiPredicate<BO, Player> predicate) {
		this.shouldFollow = predicate;
		
		return this;
	}
	
	/// Set the length of time (in ticks) that the entity should be unable to be tempted once this behaviour finishes
	@ApiStatus.NonExtendable
	public FollowTemptation<BO> temptationCooldown(int ticks) {
		return temptationCooldown(_ -> ticks);
	}
	
	/// Set a function to determine the length of time (in ticks) that the entity should be unable to be tempted once this behaviour finishes
	@ApiStatus.NonExtendable
	public FollowTemptation<BO> temptationCooldown(final Object2IntFunction<BO> cooldownFunction) {
		this.temptationCooldown = cooldownFunction;
		
		return this;
	}
	
	/// Set the movement speed modifier for the path when chosen
	@ApiStatus.NonExtendable
	public FollowTemptation<BO> speedModifier(float modifier) {
		return speedModifier((_, _) -> modifier);
	}
	
	/// Set the function to determine the movement speed modifier for the path when chosen
	@ApiStatus.NonExtendable
	public FollowTemptation<BO> speedModifier(ToFloatBiFunction<BO, Player> function) {
		this.speedModifier = function;
		
		return this;
	}
	
	/// Set the distance (in blocks) from the followed player to path to when following
	@ApiStatus.NonExtendable
	public FollowTemptation<BO> closeEnoughDist(float dist) {
		return closeEnoughDist((_, _) -> dist);
	}
	
	/// Set a function to determine the distance (in blocks) from the followed player to path to when following
	@ApiStatus.NonExtendable
	public FollowTemptation<BO> closeEnoughDist(ToFloatBiFunction<BO, Player> function) {
		this.closeEnoughDist = function;
		
		return this;
	}
	
	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// Set a callback for when the behaviour successfully begins
	///
	/// This is called immediately prior to [ExtendedBehaviour#start(LivingEntity)]
	@ApiStatus.NonExtendable
	@Override
	public FollowTemptation<BO> whenStarting(Consumer<BO> callback) {
		return (FollowTemptation<BO>)super.whenStarting(callback);
	}
	
	/// Set a callback for when the behaviour stops
	///
	/// This is called immediately prior to [ExtendedBehaviour#stop(LivingEntity)]
	///
	/// Note that the behaviour stopping does not necessarily mean it was successful
	@ApiStatus.NonExtendable
	@Override
	public FollowTemptation<BO> whenStopping(Consumer<BO> callback) {
		return (FollowTemptation<BO>)super.whenStopping(callback);
	}
	
	/// Set the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public FollowTemptation<BO> runFor(int ticks) {
		return (FollowTemptation<BO>)super.runFor(ticks);
	}
	
	/// Set the range of ticks the behaviour should try to run for, once started<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is run
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public FollowTemptation<BO> runFor(int minTicks, int maxTicks) {
		return (FollowTemptation<BO>)super.runFor(minTicks, maxTicks);
	}
	
	/// Set a function to determine the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public FollowTemptation<BO> runFor(ToIntFunction<BO> timeProvider) {
		return (FollowTemptation<BO>)super.runFor(timeProvider);
	}
	
	/// Disable the tick-based timeout for this behaviour and instead rely exclusively on other conditions
	/// such as [#getMemoryRequirements()] failing or [#stopIf(Predicate)]
	@ApiStatus.NonExtendable
	@Override
	public FollowTemptation<BO> noTimeout() {
		return (FollowTemptation<BO>)super.noTimeout();
	}
	
	/// Set the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public FollowTemptation<BO> cooldownFor(int ticks) {
		return (FollowTemptation<BO>)super.cooldownFor(ticks);
	}
	
	/// Set the range of ticks that this behaviour should be prevented from starting again after it has finished<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is stopped
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public FollowTemptation<BO> cooldownFor(int minTicks, int maxTicks) {
		return (FollowTemptation<BO>)super.cooldownFor(minTicks, maxTicks);
	}
	
	/// Set a function to determine the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public FollowTemptation<BO> cooldownFor(ToIntFunction<BO> timeProvider) {
		return (FollowTemptation<BO>)super.cooldownFor(timeProvider);
	}
	
	/// Set an additional condition for the behaviour to be able to start
	///
	/// Prevents this behaviour starting unless this predicate returns true
	@ApiStatus.NonExtendable
	@Override
	public FollowTemptation<BO> startCondition(Predicate<BO> predicate) {
		return (FollowTemptation<BO>)super.startCondition(predicate);
	}
	
	/// Set a condition under which the behaviour should automatically stop<br/>
	/// Has no effect on one-shot behaviours that don't tick or have a runtime
	///
	/// Stops the behaviour immediately if the predicate returns true, ready to run again
	@ApiStatus.NonExtendable
	@Override
	public FollowTemptation<BO> stopIf(Predicate<BO> predicate) {
		return (FollowTemptation<BO>)super.stopIf(predicate);
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
		//noinspection DataFlowIssue
		return this.shouldFollow.test(entity, BrainUtil.getMemory(entity, MemoryModuleType.TEMPTING_PLAYER));
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
		BrainUtil.setMemory(entity, MemoryModuleType.IS_TEMPTED, true);
	}
	
	/// Check any additional conditions for whether the behaviour should continue running<br/>
	/// This is checked before [ExtendedBehaviour#tick(LivingEntity)]
	///
	/// The behaviour's [runtime][#runFor] has already been checked at this stage, and [#stopIf(Predicate)] will be checked immediately after this method if returning true
	///
	/// Memories are not guaranteed to be in their required state here, so if you have required memories, it might be worth checking them here
	@Override
	protected boolean shouldKeepRunning(BO entity) {
		final Player temptingPlayer = BrainUtil.getMemory(entity, MemoryModuleType.TEMPTING_PLAYER);
		
		return temptingPlayer != null &&
		       !BrainUtil.hasMemory(entity, MemoryModuleType.TEMPTATION_COOLDOWN_TICKS) &&
		       !BrainUtil.hasMemory(entity, MemoryModuleType.BREED_TARGET) &&
		       this.shouldFollow.test(entity, temptingPlayer);
	}
	
	/// Run the per-tick behaviour for this behaviour<br/>
	/// This is called <u>every tick</u> this behaviour is active, so be aware of any performance-intensive functionality handled here
	///
	/// [ExtendedBehaviour#shouldKeepRunning(LivingEntity)] and [#stopIf(Predicate)] have both been checked immediately prior to this method being called
	///
	/// NOTE: Memory requirements are __not__ guaranteed at this stage. If you are retrieving brain memories, you may need to check their presence before use
	@Override
	protected void tick(BO entity) {
		final Player temptingPlayer = BrainUtil.getMemory(entity, MemoryModuleType.TEMPTING_PLAYER);
		@SuppressWarnings("DataFlowIssue")
		final float closeEnough = this.closeEnoughDist.applyAsFloat(entity, temptingPlayer);

		BrainUtil.setMemory(entity, MemoryModuleType.LOOK_TARGET, new EntityTracker(temptingPlayer, true));

		if (entity.closerThan(temptingPlayer, closeEnough)) {
			BrainUtil.clearMemory(entity, MemoryModuleType.WALK_TARGET);
		}
		else {
			final WalkTarget walkTarget = BrainUtil.getMemory(entity, MemoryModuleType.WALK_TARGET);
			
			if (walkTarget == null || entity.getNavigation().isDone())
				BrainUtil.setMemory(entity, MemoryModuleType.WALK_TARGET, new WalkTarget(new EntityTracker(temptingPlayer, false), this.speedModifier.applyAsFloat(entity, temptingPlayer), Mth.floor(closeEnough)));
		}
	}
	
	/// Called when this behaviour is instructed to stop<br/>
	/// This may be due to timing out, failing to meet a condition, or for any other reason
	///
	/// This method is non-negotiable, and it must be safe to assume that if this method is called, this behaviour is safe to discard
	@MustBeInvokedByOverriders
	@Override
	protected void stop(BO entity) {
		final int cooldownTicks = this.temptationCooldown.apply(entity);

		BrainUtil.setForgettableMemory(entity, MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, cooldownTicks, cooldownTicks);
		BrainUtil.clearMemories(entity, MemoryModuleType.WALK_TARGET, MemoryModuleType.LOOK_TARGET, MemoryModuleType.IS_TEMPTED);
	}
	//</editor-fold>
}
