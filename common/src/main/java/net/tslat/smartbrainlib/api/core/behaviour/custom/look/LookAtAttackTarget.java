package net.tslat.smartbrainlib.api.core.behaviour.custom.look;

import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.object.MemoryTest;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.List;

/**
 * Set the {@link MemoryModuleType#LOOK_TARGET} of the brain owner to the current {@link MemoryModuleType#ATTACK_TARGET}, replacing the existing look target.<br>
 * This is mostly superceded by {@link net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetWalkTargetToAttackTarget SetWalkTargetToAttackTarget}, but can be useful if you want the brain owner to look at the target without pathing to it
 * @param <E> The entity
 */
public class LookAtAttackTarget<E extends LivingEntity> extends ExtendedBehaviour<E> {
	private static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(2).hasMemory(MemoryModuleType.ATTACK_TARGET).usesMemory(MemoryModuleType.LOOK_TARGET);

	protected LivingEntity target = null;

	@Override
	protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
		return MEMORY_REQUIREMENTS;
	}

	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
		this.target = BrainUtil.getTargetOfEntity(entity);

		return !(BrainUtil.getMemory(entity, MemoryModuleType.LOOK_TARGET) instanceof EntityTracker entityTracker) || (entityTracker.getEntity() != this.target && entityTracker.getEntity().isAlive());
	}

	@Override
	protected void start(E entity) {
		BrainUtil.setMemory(entity, MemoryModuleType.LOOK_TARGET, new EntityTracker(this.target, true));
	}

	@Override
	protected void stop(E entity) {
		this.target = null;
	}
}
