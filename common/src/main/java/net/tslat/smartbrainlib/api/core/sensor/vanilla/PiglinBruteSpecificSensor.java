package net.tslat.smartbrainlib.api.core.sensor.vanilla;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.registry.SBLSensors;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/// A replication of vanilla's [net.minecraft.world.entity.ai.sensing.PiglinBruteSpecificSensor]<br/>
/// Not really useful, but included for completeness' sake and legibility.
///
/// Keeps track of nearby [piglins][Piglin] and [nemesis][MemoryModuleType#NEAREST_VISIBLE_NEMESIS]
///
/// @param <BO> The brain owner entity
public class PiglinBruteSpecificSensor<BO extends LivingEntity> extends ExtendedSensor<BO> {
	protected static final List<MemoryModuleType<?>> MEMORIES = ObjectArrayList.of(MemoryModuleType.NEARBY_ADULT_PIGLINS);

	protected BiPredicate<BO, Mob> nemesisPredicate = (_, mob) -> mob instanceof WitherSkeleton || mob instanceof WitherBoss;
	protected BiPredicate<BO, AbstractPiglin> piglinPredicate = (_, piglin) -> piglin.isAdult();

	/// Set a custom predicate for determining valid "nemesis" entities
	public PiglinBruteSpecificSensor<BO> setNemesisPredicate(BiPredicate<BO, Mob> predicate) {
		this.nemesisPredicate = predicate;

		return this;
	}

	/// Set a custom predicate for determining valid [AbstractPiglin]s for the [MemoryModuleType#NEARBY_ADULT_PIGLINS] memory
	public PiglinBruteSpecificSensor<BO> setPiglinPredicate(BiPredicate<BO, AbstractPiglin> predicate) {
		this.piglinPredicate = predicate;

		return this;
	}

	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// Set the scan rate for this sensor
	public PiglinBruteSpecificSensor<BO> scanRate(int scanRate) {
		return scanRate(_ -> scanRate);
	}

	/// Set the scan rate provider for this sensor
	///
	/// The provider will be sampled every time the sensor does a scan
	@Override
	public PiglinBruteSpecificSensor<BO> scanRate(ToIntFunction<BO> function) {
		return (PiglinBruteSpecificSensor<BO>)super.scanRate(function);
	}

	/// Set a callback function for when the sensor completes a scan
	@Override
	public PiglinBruteSpecificSensor<BO> afterScanning(Consumer<BO> callback) {
		return (PiglinBruteSpecificSensor<BO>)super.afterScanning(callback);
	}

	/// Set a condition that must be met in order to perform a scan
	///
	/// Failing the predicate will skip that scan tick and will not try again until the next scan tick as defined by [#scanRate]
	@Override
	public PiglinBruteSpecificSensor<BO> onlyScanIf(Predicate<BO> predicate) {
		return (PiglinBruteSpecificSensor<BO>)super.onlyScanIf(predicate);
	}
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="<Internal Handling>">
	/// @return The [SensorType] of the sensor, used for reverse lookups.
	@Override
	public SensorType<? extends ExtendedSensor<?>> type() {
		return SBLSensors.PIGLIN_BRUTE_SPECIFIC.get();
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
		final NearestVisibleLivingEntities entities = BrainUtil.memoryOrDefault(brain, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, NearestVisibleLivingEntities.empty());
		final Mob nemesis = entities.findClosest(target -> target instanceof Mob mob && this.nemesisPredicate.test(entity, mob)).map(Mob.class::cast).orElse(null);
		final List<AbstractPiglin> nearbyPiglins = new ObjectArrayList<>();

		BrainUtil.setOrClearMemory(brain, MemoryModuleType.NEAREST_VISIBLE_NEMESIS, nemesis);
		BrainUtil.withMemory(brain, MemoryModuleType.NEAREST_LIVING_ENTITIES, nearby -> {
			for (LivingEntity target : nearby) {
				if (target instanceof AbstractPiglin piglin && this.piglinPredicate.test(entity, piglin))
					nearbyPiglins.add(piglin);
			}
		});
		BrainUtil.setMemory(brain, MemoryModuleType.NEARBY_ADULT_PIGLINS, nearbyPiglins);
	}
	//</editor-fold>
}
