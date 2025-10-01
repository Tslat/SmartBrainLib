package net.tslat.smartbrainlib.api.core.behaviour.custom.move;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2IntFunction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.player.Player;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.object.MemoryTest;
import net.tslat.smartbrainlib.object.ToFloatBiFunction;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.List;
import java.util.function.BiPredicate;

/**
 * {@link ExtendedBehaviour ExtendedBehaviour} equivalent of vanilla's {@link net.minecraft.world.entity.ai.behavior.FollowTemptation FollowTemptation}.<br>
 * Has the entity follow a relevant temptation target (I.E. a player holding a tempting item).<br>
 * Will continue running for as long as the entity is being tempted.<br>
 * Defaults:
 * <ul>
 *     <li>Follows the temptation target indefinitely</li>
 *     <li>Will stop following if panicked or if it has an active breed target</li>
 *     <li>Will not follow a temptation target again for 5 seconds after stopping</li>
 *     <li>Considers 2.5 blocks 'close enough' for the purposes of following temptation</li>
 *     <li>1x speed modifier while following</li>
 * </ul>
 */
public class FollowTemptation<E extends PathfinderMob> extends ExtendedBehaviour<E> {
	private static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(7).hasMemory(MemoryModuleType.TEMPTING_PLAYER).noMemory(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS).usesMemories(MemoryModuleType.LOOK_TARGET, MemoryModuleType.WALK_TARGET, MemoryModuleType.IS_TEMPTED, MemoryModuleType.IS_PANICKING, MemoryModuleType.BREED_TARGET);

	protected ToFloatBiFunction<E, Player> speedMod = (entity, temptingPlayer) -> 1f;
	protected BiPredicate<E, Player> shouldFollow = (entity, temptingPlayer) -> !entity.hasPassenger(temptingPlayer);
	protected ToFloatBiFunction<E, Player> closeEnoughWhen = (owner, temptingPlayer) -> 2.5f;
	protected Object2IntFunction<E> temptationCooldown = entity -> 100;

	public FollowTemptation() {
		super();

		noTimeout();
	}

	/**
	 * Set the movespeed modifier for the entity when following the tempting player.
	 * @param speedModifier The movespeed modifier/multiplier
	 * @return this
	 */
	public FollowTemptation<E> speedMod(final ToFloatBiFunction<E, Player> speedModifier) {
		this.speedMod = speedModifier;

		return this;
	}

	/**
	 * Determine whether the entity should follow the tempting player or not
	 * @param predicate The temptation predicate
	 * @return this
	 */
	public FollowTemptation<E> followIf(final BiPredicate<E, Player> predicate) {
		this.shouldFollow = predicate;

		return this;
	}

	/**
	 * Sets the amount (in blocks) that the mob can be considered 'close enough' to their temptation that they can stop pathfinding
	 * @param closeEnoughMod The distance modifier
	 * @return this
	 */
	public FollowTemptation<E> closeEnoughDist(final ToFloatBiFunction<E, Player> closeEnoughMod) {
		this.closeEnoughWhen = closeEnoughMod;

		return this;
	}

	/**
	 * Sets the length of time (in ticks) the entity should ignore temptation after having previously been tempted.<br>
	 * NOTE: This could be ignored if the {@link FollowTemptation#followIf} predicate has been overriden
	 * @param cooldownFunction The cooldown function
	 * @return this
	 */
	public FollowTemptation<E> temptationCooldown(final Object2IntFunction<E> cooldownFunction) {
		this.temptationCooldown = cooldownFunction;

		return this;
	}

	@Override
	protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
		return MEMORY_REQUIREMENTS;
	}

	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
		return this.shouldFollow.test(entity, BrainUtil.getMemory(entity, MemoryModuleType.TEMPTING_PLAYER));
	}

	@Override
	protected boolean shouldKeepRunning(E entity) {
		return BrainUtil.hasMemory(entity, MemoryModuleType.TEMPTING_PLAYER) &&
				!BrainUtil.hasMemory(entity, MemoryModuleType.TEMPTATION_COOLDOWN_TICKS) &&
				!BrainUtil.hasMemory(entity, MemoryModuleType.BREED_TARGET) &&
				this.shouldFollow.test(entity, BrainUtil.getMemory(entity, MemoryModuleType.TEMPTING_PLAYER));
	}

	@Override
	protected void start(E entity) {
		BrainUtil.setMemory(entity, MemoryModuleType.IS_TEMPTED, true);
	}

	@Override
	protected void tick(E entity) {
		final Player temptingPlayer = BrainUtil.getMemory(entity, MemoryModuleType.TEMPTING_PLAYER);
		final float closeEnough = this.closeEnoughWhen.applyAsFloat(entity, temptingPlayer);

		BrainUtil.setMemory(entity, MemoryModuleType.LOOK_TARGET, new EntityTracker(temptingPlayer, true));

		if (entity.distanceToSqr(temptingPlayer) < closeEnough * closeEnough) {
			BrainUtil.clearMemory(entity, MemoryModuleType.WALK_TARGET);
		}
		else {
			BrainUtil.setMemory(entity, MemoryModuleType.WALK_TARGET, new WalkTarget(new EntityTracker(temptingPlayer, false), this.speedMod.applyAsFloat(entity, temptingPlayer), (int)closeEnough));
		}
	}

	@Override
	protected void stop(E entity) {
		final int cooldownTicks = this.temptationCooldown.apply(entity);

		BrainUtil.setForgettableMemory(entity, MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, cooldownTicks, cooldownTicks);
		BrainUtil.setMemory(entity, MemoryModuleType.IS_TEMPTED, false);
		BrainUtil.clearMemories(entity, MemoryModuleType.WALK_TARGET, MemoryModuleType.LOOK_TARGET);
	}
}
