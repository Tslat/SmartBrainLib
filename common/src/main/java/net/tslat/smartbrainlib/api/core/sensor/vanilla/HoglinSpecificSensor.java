package net.tslat.smartbrainlib.api.core.sensor.vanilla;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.level.block.state.BlockState;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.registry.SBLSensors;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/// A replication of vanilla's [net.minecraft.world.entity.ai.sensing.HoglinSpecificSensor]<br/>
/// Not really useful, but included for completeness' sake and legibility
///
/// Handles most of the [Hoglin]'s memories at once
///
/// @param <BO> The brain owner entity
public class HoglinSpecificSensor<BO extends LivingEntity> extends ExtendedSensor<BO> {
	protected static final List<MemoryModuleType<?>> MEMORIES = ObjectArrayList.of(
			MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
			MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLIN,
			MemoryModuleType.NEAREST_VISIBLE_ADULT_HOGLINS,
			MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT,
			MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT,
			MemoryModuleType.NEAREST_REPELLENT);
	protected BiPredicate<BO, LivingEntity> validTargetCondition = (_, target) -> !target.isBaby();
	protected BiPredicate<BO, BlockState> isRepellent = (_, state) -> state.is(BlockTags.HOGLIN_REPELLENTS);

	/// Set a targeting condition for valid targets
	public HoglinSpecificSensor<BO> onlyTargetIf(BiPredicate<BO, LivingEntity> predicate) {
		this.validTargetCondition = predicate;

		return this;
	}

	/// Set a custom predicate for valid repellent blocks
	public HoglinSpecificSensor<BO> validRepellents(BiPredicate<BO, BlockState> predicate) {
		this.isRepellent = predicate;

		return this;
	}

	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// Set the scan rate for this sensor
	public HoglinSpecificSensor<BO> scanRate(int scanRate) {
		return scanRate(_ -> scanRate);
	}

	/// Set the scan rate provider for this sensor
	///
	/// The provider will be sampled every time the sensor does a scan
	public HoglinSpecificSensor<BO> scanRate(ToIntFunction<BO> function) {
		return (HoglinSpecificSensor<BO>)super.scanRate(function);
	}

	/// Set a callback function for when the sensor completes a scan
	public HoglinSpecificSensor<BO> afterScanning(Consumer<BO> callback) {
		return (HoglinSpecificSensor<BO>)super.afterScanning(callback);
	}

	/// Set a condition that must be met in order to perform a scan
	///
	/// Failing the predicate will skip that scan tick and will not try again until the next scan tick as defined by [#scanRate]
	public HoglinSpecificSensor<BO> onlyScanIf(Predicate<BO> predicate) {
		return (HoglinSpecificSensor<BO>)super.onlyScanIf(predicate);
	}
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="<Internal Handling>">
	/// The list of memory types this sensor saves to. This should contain any memory the sensor sets a value for in the brain<br/>
	/// Bonus points if it's a statically cached list
	///
	/// @return The list of memory types saves by this sensor
	@Override
	public List<MemoryModuleType<?>> memoriesUsed() {
		return MEMORIES;
	}

	/// @return The [SensorType] of the sensor, used for reverse lookups
	@Override
	public SensorType<? extends ExtendedSensor<?>> type() {
		return SBLSensors.HOGLIN_SPECIFIC.get();
	}

	/// Handle the Sensor's actual function here. Be wary of the performance implications of computation-heavy checks here.
	///
	/// @param level The level the entity is in
	/// @param entity The owner of the brain
	@Override
	protected void doTick(ServerLevel level, BO entity) {
		final Brain<BO> brain = BrainUtil.getBrain(entity);
		final NearestVisibleLivingEntities entities = BrainUtil.memoryOrDefault(brain, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, NearestVisibleLivingEntities.empty());
		final List<Hoglin> hoglins = new ObjectArrayList<>();
		Piglin nearestPiglin = null;
		int piglinCount = 0;

		for (LivingEntity target : entities.findAll(mob -> this.validTargetCondition.test(entity, mob))) {
			if (target instanceof Piglin piglin) {
				piglinCount++;

				if (nearestPiglin == null)
					nearestPiglin = piglin;
			}
			else if (target instanceof Hoglin hoglin) {
				hoglins.add(hoglin);
			}
		}

		BrainUtil.setOrClearMemory(brain, MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLIN, nearestPiglin);
		BrainUtil.setMemory(brain, MemoryModuleType.NEAREST_VISIBLE_ADULT_HOGLINS, hoglins);
		BrainUtil.setMemory(brain, MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, piglinCount);
		BrainUtil.setMemory(brain, MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT, hoglins.size());
		BrainUtil.setOrClearMemory(brain, MemoryModuleType.NEAREST_REPELLENT, BlockPos.findClosestMatch(entity.blockPosition(), 8, 4, pos ->
				this.isRepellent.test(entity, level.getBlockState(pos))).orElse(null));
	}
	//</editor-fold>
}
