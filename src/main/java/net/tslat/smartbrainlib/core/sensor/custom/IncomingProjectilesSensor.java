package net.tslat.smartbrainlib.core.sensor.custom;

import java.util.Comparator;
import java.util.List;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.projectile.Projectile;
import net.tslat.smartbrainlib.api.util.BrainUtils;
import net.tslat.smartbrainlib.api.util.EntityRetrievalUtil;
import net.tslat.smartbrainlib.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.core.sensor.PredicateSensor;
import net.tslat.smartbrainlib.registry.SBLMemoryTypes;
import net.tslat.smartbrainlib.registry.SBLSensors;

/**
 * Custom sensor that detects incoming projectiles. Defaults:
 * <ul>
 * <li>3-tick scan rate</li>
 * <li>Only projectiles that are still in flight</li>
 * <li>Only projectiles that will hit the entity before the next scan</li>
 * </ul>
 * 
 * @param <E>
 */
public class IncomingProjectilesSensor<E extends LivingEntity> extends PredicateSensor<Projectile, E> {
	private static final List<MemoryModuleType<?>> MEMORIES = ObjectArrayList.of(SBLMemoryTypes.INCOMING_PROJECTILES);

	public IncomingProjectilesSensor() {
		setScanRate(entity -> 3);
		setPredicate((projectile, entity) -> {
			if (projectile.isOnGround() || projectile.horizontalCollision || projectile.verticalCollision)
				return false;

			return entity.getBoundingBox().clip(projectile.position(),
					projectile.position().add(projectile.getDeltaMovement().multiply(3, 3, 3))).isPresent();
		});
	}

	@Override
	public List<MemoryModuleType<?>> memoriesUsed() {
		return MEMORIES;
	}

	@Override
	public SensorType<? extends ExtendedSensor<?>> type() {
		return SBLSensors.INCOMING_PROJECTILES;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void doTick(ServerLevel level, LivingEntity entity) {
		List<Projectile> projectiles = EntityRetrievalUtil.getEntities(level, entity.getBoundingBox().inflate(7),
				target -> target instanceof Projectile projectile && predicate().test(projectile, (E) entity));

		if (!projectiles.isEmpty()) {
			projectiles.sort(Comparator.comparingDouble(entity::distanceToSqr));
			BrainUtils.setMemory(entity, SBLMemoryTypes.INCOMING_PROJECTILES, projectiles);
		} else {
			BrainUtils.clearMemory(entity, SBLMemoryTypes.INCOMING_PROJECTILES);
		}
	}
}
