package net.tslat.smartbrainlib.api.core.behaviour.custom.move;

import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;

import java.util.List;

/**
 * Replacement for {@link net.minecraft.world.entity.ai.goal.FloatGoal} or {@link net.minecraft.world.entity.ai.behavior.Swim}. <br>
 * Causes the entity to rise to the surface of water and float at the surface.
 * Defaults:
 * <ul>
 *     <li>80% chance per tick to jump</li>
 *     <li>Applies to water</li>
 * </ul>
 */
public class FloatToSurfaceOfFluid<E extends Mob> extends ExtendedBehaviour<E> {
	protected float riseChance = 0.8f;
	private boolean canFloatPrevious;

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
	protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
		return List.of();
	}

	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
		return entity.isInWater() && entity.getFluidHeight(FluidTags.WATER) > entity.getFluidJumpThreshold() || entity.isInLava();
	}

	@Override
	protected boolean shouldKeepRunning(E entity) {
		return checkExtraStartConditions((ServerLevel)entity.level(), entity);
	}

	@Override
	protected void start(E entity) {
		super.start(entity);
		this.canFloatPrevious = entity.getNavigation().canFloat();
		entity.getNavigation().setCanFloat(true);
	}

	@Override
	protected void tick(E entity) {
		if (entity.getRandom().nextFloat() < this.riseChance)
			entity.getJumpControl().jump();
	}

	@Override
	protected void stop(E entity) {
		super.stop(entity);
		entity.getNavigation().setCanFloat(this.canFloatPrevious);
	}
}