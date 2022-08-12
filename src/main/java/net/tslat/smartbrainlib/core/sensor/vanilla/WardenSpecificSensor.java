package net.tslat.smartbrainlib.core.sensor.vanilla;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.tslat.smartbrainlib.api.BrainUtils;

import java.util.List;

/**
 * A replication of vanilla's {@link net.minecraft.world.entity.ai.sensing.WardenEntitySensor}. Not really useful, but included for completeness' sake and legibility. <br/>
 * Handle's the Warden's nearest attackable target, prioritising players.
 * @param <E> The entity
 */
public class WardenSpecificSensor<E extends Warden> extends NearbyLivingEntitySensor<E> {
	private static final List<MemoryModuleType<?>> MEMORIES = ObjectArrayList.of(MemoryModuleType.NEAREST_ATTACKABLE, MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);

	public WardenSpecificSensor() {
		setRadius(24);
		setPredicate((target, entity) -> entity.canTargetEntity(target));
	}

	@Override
	protected List<MemoryModuleType<?>> memoriesUsed() {
		return MEMORIES;
	}

	@Override
	protected void doTick(ServerLevel level, E entity) {
		super.doTick(level, entity);

		BrainUtils.withMemory(entity, MemoryModuleType.NEAREST_LIVING_ENTITIES, entities -> {
			LivingEntity fallbackTarget = null;

			for (LivingEntity target : entities) {
				if (target instanceof Player) {
					BrainUtils.setMemory(entity, MemoryModuleType.NEAREST_ATTACKABLE, target);

					return;
				}
				else if (fallbackTarget == null) {
					fallbackTarget = target;
				}
			}

			if (fallbackTarget != null) {
				BrainUtils.setMemory(entity, MemoryModuleType.NEAREST_ATTACKABLE, fallbackTarget);
			}
			else {
				BrainUtils.clearMemory(entity, MemoryModuleType.NEAREST_ATTACKABLE);
			}
		});
	}
}
