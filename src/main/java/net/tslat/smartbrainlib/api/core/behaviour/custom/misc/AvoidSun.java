package net.tslat.smartbrainlib.api.core.behaviour.custom.misc;

import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.world.server.ServerWorld;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;

import java.util.ArrayList;
import java.util.List;

/** Avoid the sun if not wearing a hat
 * @param <E> The entity
 */
public class AvoidSun<E extends MobEntity> extends ExtendedBehaviour<E> {
	@Override
	protected List<Pair<MemoryModuleType<?>, MemoryModuleStatus>> getMemoryRequirements() {
		return new ArrayList<>();
	}

	@Override
	protected boolean checkExtraStartConditions(ServerWorld level, E entity) {
		return level.isDay() && entity.getItemBySlot(EquipmentSlotType.HEAD).isEmpty() && entity.getNavigation() instanceof GroundPathNavigator;
	}

	@Override
	protected void start(E entity) {
		((GroundPathNavigator)entity.getNavigation()).setAvoidSun(true);
	}

	@Override
	protected void stop(E entity) {
		if (entity.getNavigation() instanceof GroundPathNavigator)
			((GroundPathNavigator)entity.getNavigation()).setAvoidSun(true);
	}
}
