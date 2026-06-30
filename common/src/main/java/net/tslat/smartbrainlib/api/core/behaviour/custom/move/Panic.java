package net.tslat.smartbrainlib.api.core.behaviour.custom.move;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;
import net.tslat.smartbrainlib.library.interfaces.ToFloatTriFunction;
import net.tslat.smartbrainlib.library.interfaces.TriPredicate;
import net.tslat.smartbrainlib.library.object.MemoryTest;
import net.tslat.smartbrainlib.library.object.SquareRadius;
import net.tslat.smartbrainlib.util.BrainUtil;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.function.*;

/// Rapidly sets a nearby [walk target][MemoryModuleType#WALK_TARGET] if recently hurt
///
/// Functional equivalent of the goal system's [panic goal][net.minecraft.world.entity.ai.goal.PanicGoal]
///
/// @param <BO> The brain owner entity
@SuppressWarnings("NullableProblems")
public class Panic<BO extends PathfinderMob> extends ExtendedBehaviour<BO> {
	protected static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(3).usesMemories(MemoryModuleType.HURT_BY, MemoryModuleType.WALK_TARGET, MemoryModuleType.IS_PANICKING);

	protected BiPredicate<BO, @Nullable DamageSource> shouldPanic = (entity, damageSource) -> damageSource != null && damageSource.is(DamageTypeTags.PANIC_CAUSES);
	protected ToIntBiFunction<BO, @Nullable DamageSource> panicTicks = (entity, _) -> entity.getRandom().nextInt(100, 120);
	protected ToFloatTriFunction<BO, @Nullable DamageSource, Vec3> speedModifier = (_, _, _) -> 1.25f;
	protected BiFunction<BO, @Nullable DamageSource, SquareRadius> radius = (_, _) -> new SquareRadius(5, 4);
	protected TriPredicate<BO, @Nullable DamageSource, Vec3> validPositionPredicate = (_, _, _) -> true;

	protected @Nullable Vec3 targetPos = null;
	protected @Nullable DamageSource lastDamage = null;
	protected int panicEndTime = 0;

	public Panic() {
		noTimeout();
	}

	/// Override the default predicate that determines whether an entity should panic or not when hurt by an entity
	@ApiStatus.NonExtendable
	public Panic<BO> panicIf(final BiPredicate<BO, DamageSource> predicate) {
		this.shouldPanic = predicate;

		return this;
	}

	/// Set a function to determine the length of time (in ticks) that the entity should panic for once starting
	@ApiStatus.NonExtendable
	public Panic<BO> panicFor(final ToIntBiFunction<BO, DamageSource> function) {
		this.panicTicks = function;

		return this;
	}
	
	/// Set the movement speed modifier for the path when chosen
	@ApiStatus.NonExtendable
	public Panic<BO> speedModifier(float modifier) {
		return speedModifier((_, _, _) -> modifier);
	}
	
	/// Set the function to determine the movement speed modifier for the path when chosen
	@ApiStatus.NonExtendable
	public Panic<BO> speedModifier(ToFloatTriFunction<BO, @Nullable DamageSource, Vec3> function) {
		this.speedModifier = function;
		
		return this;
	}
	
	/// Set the radius (in blocks) to look for flight positions
	@ApiStatus.NonExtendable
	public Panic<BO> setRadius(double radius) {
		return setRadius(radius, radius);
	}
	
	/// Set the radius (in blocks) to look for flight positions
	@ApiStatus.NonExtendable
	public Panic<BO> setRadius(double xz, double y) {
		return setRadius((_, _) -> new SquareRadius(xz, y));
	}
	
	/// Set the function to determine the radius (in blocks) to look for flight positions
	@ApiStatus.NonExtendable
	public Panic<BO> setRadius(BiFunction<BO, @Nullable DamageSource, SquareRadius> function) {
		this.radius = function;
		
		return this;
	}
	
	/// Sets a predicate to check whether a target movement position is valid or not
	@ApiStatus.NonExtendable
	public Panic<BO> isValidPositionIf(TriPredicate<BO, @Nullable DamageSource, Vec3> predicate) {
		this.validPositionPredicate = predicate;
		
		return this;
	}
	
	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// Set a callback for when the behaviour successfully begins
	///
	/// This is called immediately prior to [ExtendedBehaviour#start(LivingEntity)]
	@ApiStatus.NonExtendable
	@Override
	public Panic<BO> whenStarting(Consumer<BO> callback) {
		return (Panic<BO>)super.whenStarting(callback);
	}
	
	/// Set a callback for when the behaviour stops
	///
	/// This is called immediately prior to [ExtendedBehaviour#stop(LivingEntity)]
	///
	/// Note that the behaviour stopping does not necessarily mean it was successful
	@ApiStatus.NonExtendable
	@Override
	public Panic<BO> whenStopping(Consumer<BO> callback) {
		return (Panic<BO>)super.whenStopping(callback);
	}
	
	/// Set the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public Panic<BO> runFor(int ticks) {
		return (Panic<BO>)super.runFor(ticks);
	}
	
	/// Set the range of ticks the behaviour should try to run for, once started<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is run
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public Panic<BO> runFor(int minTicks, int maxTicks) {
		return (Panic<BO>)super.runFor(minTicks, maxTicks);
	}
	
	/// Set a function to determine the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public Panic<BO> runFor(ToIntFunction<BO> timeProvider) {
		return (Panic<BO>)super.runFor(timeProvider);
	}
	
	/// Disable the tick-based timeout for this behaviour and instead rely exclusively on other conditions
	/// such as [#getMemoryRequirements()] failing or [#stopIf(Predicate)]
	@ApiStatus.NonExtendable
	@Override
	public Panic<BO> noTimeout() {
		return (Panic<BO>)super.noTimeout();
	}
	
	/// Set the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public Panic<BO> cooldownFor(int ticks) {
		return (Panic<BO>)super.cooldownFor(ticks);
	}
	
	/// Set the range of ticks that this behaviour should be prevented from starting again after it has finished<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is stopped
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public Panic<BO> cooldownFor(int minTicks, int maxTicks) {
		return (Panic<BO>)super.cooldownFor(minTicks, maxTicks);
	}
	
	/// Set a function to determine the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public Panic<BO> cooldownFor(ToIntFunction<BO> timeProvider) {
		return (Panic<BO>)super.cooldownFor(timeProvider);
	}
	
	/// Set an additional condition for the behaviour to be able to start
	///
	/// Prevents this behaviour starting unless this predicate returns true
	@ApiStatus.NonExtendable
	@Override
	public Panic<BO> startCondition(Predicate<BO> predicate) {
		return (Panic<BO>)super.startCondition(predicate);
	}
	
	/// Set a condition under which the behaviour should automatically stop<br/>
	/// Has no effect on one-shot behaviours that don't tick or have a runtime
	///
	/// Stops the behaviour immediately if the predicate returns true, ready to run again
	@ApiStatus.NonExtendable
	@Override
	public Panic<BO> stopIf(Predicate<BO> predicate) {
		return (Panic<BO>)super.stopIf(predicate);
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
		this.lastDamage = BrainUtil.getMemory(entity, MemoryModuleType.HURT_BY);
		
		if (this.shouldPanic.test(entity, this.lastDamage) && setPanicTarget(entity))
			return true;
		
		this.lastDamage = null;
		this.targetPos = null;
		
		return false;
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
		BrainUtil.setMemory(entity, MemoryModuleType.WALK_TARGET, new WalkTarget(this.targetPos, this.speedModifier.applyAsFloat(entity, this.lastDamage, this.targetPos), 0));
		BrainUtil.setMemory(entity, MemoryModuleType.IS_PANICKING, true);

		this.panicEndTime = entity.tickCount + this.panicTicks.applyAsInt(entity, this.lastDamage);
	}
	
	/// Check any additional conditions for whether the behaviour should continue running<br/>
	/// This is checked before [ExtendedBehaviour#tick(LivingEntity)]
	///
	/// The behaviour's [runtime][#runFor] has already been checked at this stage, and [#stopIf(Predicate)] will be checked immediately after this method if returning true
	///
	/// Memories are not guaranteed to be in their required state here, so if you have required memories, it might be worth checking them here
	@Override
	protected boolean shouldKeepRunning(BO entity) {
		return entity.tickCount < this.panicEndTime;
	}
	
	/// Run the per-tick behaviour for this behaviour<br/>
	/// This is called <u>every tick</u> this behaviour is active, so be aware of any performance-intensive functionality handled here
	///
	/// [ExtendedBehaviour#shouldKeepRunning(LivingEntity)] and [#stopIf(Predicate)] have both been checked immediately prior to this method being called
	///
	/// NOTE: Memory requirements are __not__ guaranteed at this stage. If you are retrieving brain memories, you may need to check their presence before use
	@Override
	protected void tick(BO entity) {
		if (!entity.getNavigation().isDone())
			return;
		
		this.targetPos = null;
		
		if (setPanicTarget(entity)) {
			BrainUtil.clearMemory(entity, MemoryModuleType.PATH);
			//noinspection DataFlowIssue
			BrainUtil.setMemory(entity, MemoryModuleType.WALK_TARGET, new WalkTarget(this.targetPos, this.speedModifier.applyAsFloat(entity, this.lastDamage, this.targetPos), 1));
		}
	}
	
	/// Called when this behaviour is instructed to stop<br/>
	/// This may be due to timing out, failing to meet a condition, or for any other reason
	///
	/// This method is non-negotiable, and it must be safe to assume that if this method is called, this behaviour is safe to discard
	@Override
	protected void stop(BO entity) {
		this.targetPos = null;
		this.lastDamage = null;
		this.panicEndTime = 0;

		BrainUtil.setMemory(entity, MemoryModuleType.IS_PANICKING, false);
	}

	/// Find a suitable panic walk target location and set it
	///
	/// @return true if the [#targetPos] is set
	protected boolean setPanicTarget(BO entity) {
		final SquareRadius radius = this.radius.apply(entity, this.lastDamage);
		
		if (entity.isOnFire())
			this.targetPos = findNearbyWater(entity, radius);

		if (!isValidPosition(entity, this.targetPos)) {
			if (this.lastDamage != null && this.lastDamage.getSourcePosition() != null)
				this.targetPos = DefaultRandomPos.getPosAway(entity, Mth.floor(radius.xzRadius()), Mth.floor(radius.yRadius()), this.lastDamage.getSourcePosition());

			if (!isValidPosition(entity, this.targetPos))
				this.targetPos = DefaultRandomPos.getPos(entity, Mth.floor(radius.xzRadius()), Mth.floor(radius.yRadius()));
		}
		
		return isValidPosition(entity, this.targetPos);
	}
	
	/// Check if the given position is not null and is valid per the [predicate][#validPositionPredicate]
	protected boolean isValidPosition(BO entity, @Nullable Vec3 position) {
		//noinspection DataFlowIssue
		return position != null && this.validPositionPredicate.test(entity, this.lastDamage, position);
	}
	
	/// Find a nearby water block position to run to
	protected @Nullable Vec3 findNearbyWater(BO entity, SquareRadius radius) {
		final BlockPos pos = entity.blockPosition();
		final Level level = entity.level();
		
		return !level.getBlockState(pos).getCollisionShape(level, pos).isEmpty() ?
		       null :
		       BlockPos.findClosestMatch(pos, Mth.floor(radius.xzRadius()), Mth.floor(radius.yRadius()), checkPos -> level.getFluidState(checkPos).is(FluidTags.WATER))
		               .map(Vec3::atBottomCenterOf)
		               .orElse(null);
	}
	//</editor-fold>
}
