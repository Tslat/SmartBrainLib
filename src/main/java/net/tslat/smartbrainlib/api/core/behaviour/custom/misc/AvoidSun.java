package net.tslat.smartbrainlib.api.core.behaviour.custom.misc;

import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;

import java.util.List;

/** Avoid the sun if not wearing a hat
 * @param <E> The entity
 */
public class AvoidSun<E extends PathfinderMob> extends ExtendedBehaviour<E> {
	@Override
	protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
		return List.of();
	}

	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
		return level.isDay() && entity.getItemBySlot(EquipmentSlot.HEAD).isEmpty() && entity.getNavigation() instanceof GroundPathNavigation;
	}

	@Override
	protected void start(E entity) {
		((GroundPathNavigation)entity.getNavigation()).setAvoidSun(true);
	}

	@Override
	protected void stop(E entity) {
		if (entity.getNavigation() instanceof GroundPathNavigation groundNavigation)
			groundNavigation.setAvoidSun(true);
	}
}
