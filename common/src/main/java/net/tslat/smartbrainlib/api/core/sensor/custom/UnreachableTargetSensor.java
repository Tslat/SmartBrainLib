package net.tslat.smartbrainlib.api.core.sensor.custom;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.registry.SBLMemoryTypes;
import net.tslat.smartbrainlib.registry.SBLSensors;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.List;

/// A sensor to attempt to track whether the entity's target is currently obstructed either by blocks or a wall/tower of blocks
///
/// This is differentiated from [MemoryModuleType#CANT_REACH_WALK_TARGET_SINCE] in that it only stores state if the entity is actively blocked, and not just completing a previous path that may have been blocked
///
/// The contract of the memory (when this sensor is used) is as follows:
///
///   - If not present: _entity is not blocked_
///   - If false: _entity is blocked at a similar or lower y-coordinate (wall-blocked)_
///   - If true: _entity is blocked at a higher y-coordinate (target has towered up or is on cliff)_
///
/// @param <BO> The brain owner entity
public class UnreachableTargetSensor<BO extends LivingEntity> extends ExtendedSensor<BO> {
	protected static final List<MemoryModuleType<?>> MEMORIES = ObjectArrayList.of(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.ATTACK_TARGET, SBLMemoryTypes.TARGET_UNREACHABLE.get());

	protected long lastUnpathableTime = 0;

	//<editor-fold defaultstate="collapsed" desc="<Internal Handling>">
	/// @return The [SensorType] of the sensor, used for reverse lookups.
	@Override
	public SensorType<? extends ExtendedSensor<?>> type() {
		return SBLSensors.UNREACHABLE_TARGET.get();
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
		final Brain<?> brain = entity.getBrain();
		final LivingEntity target = BrainUtil.getTargetOfEntity(entity);

		if (target == null) {
			resetState(brain);

			return;
		}

		final Long unpathableTime = BrainUtil.getMemory(brain, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);

		if (unpathableTime == null) {
			resetState(brain);

			return;
		}

		if (this.lastUnpathableTime == 0) {
			this.lastUnpathableTime = unpathableTime;
		}
		else if (this.lastUnpathableTime == unpathableTime) {
			BrainUtil.clearMemory(brain, SBLMemoryTypes.TARGET_UNREACHABLE.get());
		}
		else if (this.lastUnpathableTime < unpathableTime) {
			this.lastUnpathableTime = unpathableTime;

			BrainUtil.setMemory(brain, SBLMemoryTypes.TARGET_UNREACHABLE.get(), target.getY() > entity.getEyeY());
		}
	}

	/// Reset this sensor's path obstruction timer and [SBLMemoryTypes#TARGET_UNREACHABLE] memory
	protected void resetState(Brain<?> brain) {
		if (this.lastUnpathableTime > 0)
			BrainUtil.clearMemory(brain, SBLMemoryTypes.TARGET_UNREACHABLE.get());

		this.lastUnpathableTime = 0;
	}
	//</editor-fold>
}
