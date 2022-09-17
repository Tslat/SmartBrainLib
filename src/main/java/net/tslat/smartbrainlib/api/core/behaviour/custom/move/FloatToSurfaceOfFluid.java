package net.tslat.smartbrainlib.api.core.behaviour.custom.move;

import java.util.ArrayList;
import java.util.List;

import com.mojang.datafixers.util.Pair;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.world.server.ServerWorld;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;

/**
 * Replacement for {@link net.minecraft.world.entity.ai.goal.FloatGoal} or {@link net.minecraft.world.entity.ai.behavior.Swim}. <br>
 * Causes the entity to rise to the surface of water and float at the surface.
 * Defaults:
 * <ul>
 *     <li>80% chance per tick to jump</li>
 *     <li>Applies to water</li>
 * </ul>
 */
public class FloatToSurfaceOfFluid<E extends CreatureEntity> extends ExtendedBehaviour<E> {
	private float riseChance = 0.8f;

	@Override
	protected List<Pair<MemoryModuleType<?>, MemoryModuleStatus>> getMemoryRequirements() {
		return new ArrayList<>();
	}

	/**
	 * Set the chance per tick that the entity will 'jump' in water, rising up towards the surface.
	 * @param chance The chance, between 0 and 1 (inclusive)
	 * @return this
	 */
	public FloatToSurfaceOfFluid<E> riseChance(float chance) {
		this.riseChance = chance;

		return this;
	}

	@Override
	protected boolean checkExtraStartConditions(ServerWorld level, E entity) {
		return entity.isInFluidType((fluidType, height) -> entity.canSwimInFluidType(fluidType) && height > entity.getFluidJumpThreshold());
	}

	@Override
	protected boolean canStillUse(ServerWorld level, E entity, long gameTime) {
		return checkExtraStartConditions(level, entity);
	}

	@Override
	protected void tick(E entity) {
		if (entity.getRandom().nextFloat() < this.riseChance)
			entity.getJumpControl().jump();
	}
}
