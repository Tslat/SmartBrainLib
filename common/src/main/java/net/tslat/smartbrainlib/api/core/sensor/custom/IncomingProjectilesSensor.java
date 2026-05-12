package net.tslat.smartbrainlib.api.core.sensor.custom;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ToFloatFunction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.projectile.Projectile;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.base.PredicateSensor;
import net.tslat.smartbrainlib.registry.SBLMemoryTypes;
import net.tslat.smartbrainlib.registry.SBLSensors;
import net.tslat.smartbrainlib.util.BrainUtil;
import net.tslat.smartbrainlib.util.EntityRetrievalUtil;

import java.util.Comparator;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/// Custom sensor that detects incoming projectiles
///
/// The stored [INCOMING_PROJECTILES][SBLMemoryTypes.INCOMING_PROJECTILES] memory is a list of [Projectile]s sorted by distance to the entity
///
/// @param <BO> The brain owner entity
public class IncomingProjectilesSensor<BO extends LivingEntity> extends PredicateSensor<BO, Projectile> {
	protected static final List<MemoryModuleType<?>> MEMORIES = ObjectArrayList.of(SBLMemoryTypes.INCOMING_PROJECTILES.get());

	protected ToFloatFunction<BO> radius = _ -> 7;

	public IncomingProjectilesSensor() {
		scanRate(3);
		setPredicate((entity, projectile) -> {
			if (projectile.onGround() || projectile.horizontalCollision || projectile.verticalCollision)
				return false;

			return entity.getBoundingBox().clip(projectile.position(), projectile.position().add(projectile.getDeltaMovement().scale(this.scanRate.applyAsInt(entity)))).isPresent();
		});
	}

	/// Set a custom radius for detecting projectiles around the entity
	public IncomingProjectilesSensor<BO> detectionRadius(float radius) {
		return detectionRadius(_ -> radius);
	}

	/// Set a custom radius function for detecting projectiles around the entity
	public IncomingProjectilesSensor<BO> detectionRadius(ToFloatFunction<BO> radius) {
		this.radius = radius;

		return this;
	}

	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// Set the predicate for the sensor. The subclass of this class determines its usage
	public IncomingProjectilesSensor<BO> setPredicate(BiPredicate<BO, Projectile> predicate) {
		return (IncomingProjectilesSensor<BO>)super.setPredicate(predicate);
	}

	/// Set the scan rate for this sensor
	public IncomingProjectilesSensor<BO> scanRate(int scanRate) {
		return (IncomingProjectilesSensor<BO>)super.scanRate(scanRate);
	}

	/// Set the scan rate provider for this sensor
	///
	/// The provider will be sampled every time the sensor does a scan
	@Override
	public IncomingProjectilesSensor<BO> scanRate(ToIntFunction<BO> function) {
		return (IncomingProjectilesSensor<BO>)super.scanRate(function);
	}

	/// Set a callback function for when the sensor completes a scan
	@Override
	public IncomingProjectilesSensor<BO> afterScanning(Consumer<BO> callback) {
		return (IncomingProjectilesSensor<BO>)super.afterScanning(callback);
	}

	/// Set a condition that must be met in order to perform a scan
	///
	/// Failing the predicate will skip that scan tick and will not try again until the next scan tick as defined by [#scanRate]
	@Override
	public IncomingProjectilesSensor<BO> onlyScanIf(Predicate<BO> predicate) {
		return (IncomingProjectilesSensor<BO>)super.onlyScanIf(predicate);
	}
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="<Internal Handling>">
	/// @return The [SensorType] of the sensor, used for reverse lookups.
	@Override
	public SensorType<? extends ExtendedSensor<?>> type() {
		return SBLSensors.INCOMING_PROJECTILES.get();
	}

	/// The list of memory types this sensor saves to. This should contain any memory the sensor sets a value for in the brain<br/>
	/// Bonus points if it's a statically cached list
	///
	/// @return The list of memory types saves by this sensor
	@Override
	public List<MemoryModuleType<?>> memoriesUsed() {
		return MEMORIES;
	}

	/// Handle the Sensor's actual function here. Be wary of the performance implications of computation-heavy checks here
	///
	/// @param level The level the entity is in
	/// @param entity The owner of the brain
	@Override
	protected void doTick(ServerLevel level, BO entity) {
		final List<Projectile> projectiles = EntityRetrievalUtil.getEntities(entity, this.radius.applyAsFloat(entity), Projectile.class, projectile -> predicate().test(entity, projectile));

		if (!projectiles.isEmpty())
			projectiles.sort(Comparator.comparingDouble(entity::distanceToSqr));

		BrainUtil.setMemory(entity, SBLMemoryTypes.INCOMING_PROJECTILES.get(), projectiles);
	}
	//</editor-fold>
}
