package net.tslat.smartbrainlib.core.behaviour.custom.target;

import java.util.List;
import java.util.function.Predicate;

import com.mojang.datafixers.util.Pair;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.server.ServerWorld;
import net.tslat.smartbrainlib.api.util.BrainUtils;
import net.tslat.smartbrainlib.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.object.NearestVisibleLivingEntities;
import net.tslat.smartbrainlib.registry.SBLMemoryTypes;

/**
 * Sets the attack target of the entity based, utilising a few sources of targets. <br>
 * In order:
 * <ol>
 *     <li>The {@link MemoryModuleType#NEAREST_ATTACKABLE} memory value</li>
 *     <li>The {@link MemoryModuleType#HURT_BY_ENTITY} memory value</li>
 *     <li>The closest applicable entity from the {@link MemoryModuleType#NEAREST_VISIBLE_LIVING_ENTITIES} memory value</li>
 * </ol>
 * Defaults:
 * <ul>
 *     <li>Targets any live entity, as long as it's not a creative-mode player</li>
 * </ul>
 * @param <E> The entity
 */
public class TargetOrRetaliate<E extends MobEntity> extends ExtendedBehaviour<E> {
	private static final List<Pair<MemoryModuleType<?>, MemoryModuleStatus>> MEMORY_REQUIREMENTS = ObjectArrayList.wrap(new Pair[] {Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryModuleStatus.VALUE_ABSENT), Pair.of(MemoryModuleType.HURT_BY, MemoryModuleStatus.REGISTERED), Pair.of(SBLMemoryTypes.NEAREST_ATTACKABLE.get(), MemoryModuleStatus.REGISTERED), Pair.of(SBLMemoryTypes.NEAREST_VISIBLE_LIVING_ENTITIES.get(), MemoryModuleStatus.REGISTERED)});

	private Predicate<LivingEntity> canAttackPredicate = entity -> entity.isAlive() && (!(entity instanceof PlayerEntity) || !((PlayerEntity)entity).isCreative());

	private LivingEntity toTarget = null;
	private MemoryModuleType<? extends LivingEntity> priorityTargetMemory = SBLMemoryTypes.NEAREST_ATTACKABLE.get();

	/**
	 * Set the predicate to determine whether a given entity should be targeted or not.
	 * @param predicate The predicate
	 * @return this
	 */
	public TargetOrRetaliate<E> attackablePredicate(Predicate<LivingEntity> predicate) {
		this.canAttackPredicate = predicate;

		return this;
	}

	/**
	 * Set the memory type that is checked first to target an entity.
	 * Useful for switching to player-only targeting
	 * @return this
	 */
	public TargetOrRetaliate<E> useMemory(MemoryModuleType<? extends LivingEntity> memory) {
		this.priorityTargetMemory = memory;

		return this;
	}

	@Override
	protected List<Pair<MemoryModuleType<?>, MemoryModuleStatus>> getMemoryRequirements() {
		return MEMORY_REQUIREMENTS;
	}

	@Override
	protected boolean checkExtraStartConditions(ServerWorld pLevel, E owner) {
		Brain<?> brain = owner.getBrain();
		this.toTarget = BrainUtils.getMemory(brain, this.priorityTargetMemory);

		if (this.toTarget == null)
			this.toTarget = BrainUtils.getMemory(brain, MemoryModuleType.HURT_BY_ENTITY);

		if (this.toTarget == null) {
			NearestVisibleLivingEntities nearbyEntities = BrainUtils.getMemory(brain, SBLMemoryTypes.NEAREST_VISIBLE_LIVING_ENTITIES.get());

			if (nearbyEntities != null)
				this.toTarget = nearbyEntities.findFirstMatchingEntry(canAttackPredicate).orElse(null);

			if (this.toTarget != null)
				return true;
		}

		return this.toTarget != null && this.canAttackPredicate.test(this.toTarget);
	}

	@Override
	protected void start(E entity) {
		BrainUtils.setTargetOfEntity(entity, this.toTarget);
		BrainUtils.clearMemory(entity, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);

		this.toTarget = null;

		doStop((ServerWorld)entity.level, entity, entity.level.getGameTime());
	}
}
