package net.tslat.smartbrainlib.api.core.sensor.vanilla;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.base.PredicateSensor;
import net.tslat.smartbrainlib.registry.SBLSensors;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/// A sensor that sets the memory state for the last damage source and attacker
///
/// @param <BO> The brain owner entity
public class HurtBySensor<BO extends LivingEntity> extends PredicateSensor<BO, DamageSource> {
	protected static final List<MemoryModuleType<?>> MEMORIES = ObjectArrayList.of(MemoryModuleType.HURT_BY, MemoryModuleType.HURT_BY_ENTITY);

	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// Set the predicate for the sensor. The subclass of this class determines its usage
	@Override
	public HurtBySensor<BO> setPredicate(BiPredicate<BO, DamageSource> predicate) {
		return (HurtBySensor<BO>)super.setPredicate(predicate);
	}

	/// Set the scan rate for this sensor
	public HurtBySensor<BO> scanRate(int scanRate) {
		return (HurtBySensor<BO>)super.scanRate(scanRate);
	}

	/// Set the scan rate provider for this sensor
	///
	/// The provider will be sampled every time the sensor does a scan
	@Override
	public HurtBySensor<BO> scanRate(ToIntFunction<BO> function) {
		return (HurtBySensor<BO>)super.scanRate(function);
	}

	/// Set a callback function for when the sensor completes a scan
	@Override
	public HurtBySensor<BO> afterScanning(Consumer<BO> callback) {
		return (HurtBySensor<BO>)super.afterScanning(callback);
	}

	/// Set a condition that must be met in order to perform a scan
	///
	/// Failing the predicate will skip that scan tick and will not try again until the next scan tick as defined by [#scanRate]
	@Override
	public HurtBySensor<BO> onlyScanIf(Predicate<BO> predicate) {
		return (HurtBySensor<BO>)super.onlyScanIf(predicate);
	}
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="<Internal Handling>">
	/// @return The [SensorType] of the sensor, used for reverse lookups
	@Override
	public SensorType<? extends ExtendedSensor<?>> type() {
		return SBLSensors.HURT_BY.get();
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
		final Brain<BO> brain = BrainUtil.getBrain(entity);
		final DamageSource damageSource = entity.getLastDamageSource();

		if (damageSource == null) {
			BrainUtil.clearMemory(brain, MemoryModuleType.HURT_BY);
			BrainUtil.clearMemory(brain, MemoryModuleType.HURT_BY_ENTITY);
		}
		else if (predicate().test(entity, damageSource)) {
			BrainUtil.setMemory(brain, MemoryModuleType.HURT_BY, damageSource);

			if (damageSource.getEntity() instanceof LivingEntity attacker && attacker.isAlive() && attacker.level() == entity.level())
				BrainUtil.setMemory(brain, MemoryModuleType.HURT_BY_ENTITY, attacker);
		}
		else {
			BrainUtil.withMemory(brain, MemoryModuleType.HURT_BY_ENTITY, attacker -> {
				if (!attacker.isAlive() || attacker.level() != entity.level())
					BrainUtil.clearMemory(brain, MemoryModuleType.HURT_BY_ENTITY);
			});
		}
	}
	//</editor-fold>
}
