package net.tslat.smartbrainlib.api.core.sensor.vanilla;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.PlayerSensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.player.Player;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.base.PredicateSensor;
import net.tslat.smartbrainlib.library.object.SquareRadius;
import net.tslat.smartbrainlib.registry.SBLSensors;
import net.tslat.smartbrainlib.util.BrainUtil;
import net.tslat.smartbrainlib.util.EntityRetrievalUtil;
import net.tslat.smartbrainlib.util.SensoryUtil;

import java.util.Comparator;
import java.util.List;
import java.util.function.*;

/// A sensor that looks for nearby players in the surrounding area, sorted by proximity to the brain owner
///
/// @see PlayerSensor
/// @param <BO> The brain owner entity
public class NearbyPlayersSensor<BO extends LivingEntity> extends PredicateSensor<BO, Player> {
	private static final List<MemoryModuleType<?>> MEMORIES = ObjectArrayList.of(MemoryModuleType.NEAREST_PLAYERS, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER);

	protected Function<BO, SquareRadius> radius = entity -> new SquareRadius(entity.getAttributeValue(Attributes.FOLLOW_RANGE));

	public NearbyPlayersSensor() {
		super((_, player) -> !player.isSpectator());
	}

	/// Set the radius for the sensor to scan
	///
	/// @param radius The coordinate radius, in blocks
	public NearbyPlayersSensor<BO> setRadius(double radius) {
		return setRadius(radius, radius);
	}

	/// Set the radius for the sensor to scan
	///
	/// @param xz The X/Z coordinate radius, in blocks
	/// @param y  The Y coordinate radius, in blocks
	public NearbyPlayersSensor<BO> setRadius(double xz, double y) {
		return setRadius(_ -> new SquareRadius(xz, y));
	}

	/// Set the radius for the sensor to scan.
	///
	/// @param radiusFunction The function to determine the radius for the current scan tick
	public NearbyPlayersSensor<BO> setRadius(Function<BO, SquareRadius> radiusFunction) {
		this.radius = radiusFunction;

		return this;
	}

	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// Set the predicate for the sensor. The subclass of this class determines its usage
	@Override
	public NearbyPlayersSensor<BO> setPredicate(BiPredicate<BO, Player> predicate) {
		return (NearbyPlayersSensor<BO>)super.setPredicate(predicate);
	}

	/// Set the scan rate for this sensor
	public NearbyPlayersSensor<BO> scanRate(int scanRate) {
		return (NearbyPlayersSensor<BO>)super.scanRate(scanRate);
	}

	/// Set the scan rate provider for this sensor
	///
	/// The provider will be sampled every time the sensor does a scan
	@Override
	public NearbyPlayersSensor<BO> scanRate(ToIntFunction<BO> function) {
		return (NearbyPlayersSensor<BO>)super.scanRate(function);
	}

	/// Set a callback function for when the sensor completes a scan
	@Override
	public NearbyPlayersSensor<BO> afterScanning(Consumer<BO> callback) {
		return (NearbyPlayersSensor<BO>)super.afterScanning(callback);
	}

	/// Set a condition that must be met in order to perform a scan
	///
	/// Failing the predicate will skip that scan tick and will not try again until the next scan tick as defined by [#scanRate]
	@Override
	public NearbyPlayersSensor<BO> onlyScanIf(Predicate<BO> predicate) {
		return (NearbyPlayersSensor<BO>)super.onlyScanIf(predicate);
	}
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="<Internal Handling>">
	/// @return The [SensorType] of the sensor, used for reverse lookups.
	@Override
	public SensorType<? extends ExtendedSensor<?>> type() {
		return SBLSensors.NEARBY_PLAYERS.get();
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
		final SquareRadius radius = this.radius.apply(entity);
		final List<Player> players = EntityRetrievalUtil.getPlayers(entity, radius.xzRadius(), radius.yRadius(), radius.xzRadius(), player -> predicate().test(entity, player));

		players.sort(Comparator.comparingDouble(entity::distanceToSqr));

		final List<Player> targetablePlayers = new ObjectArrayList<>(players);

		targetablePlayers.removeIf(pl -> !SensoryUtil.isEntityTargetable(entity, pl));

		final List<Player> attackablePlayers = new ObjectArrayList<>(targetablePlayers);

		attackablePlayers.removeIf(pl -> !SensoryUtil.isEntityAttackable(entity, pl));

		BrainUtil.setMemory(entity, MemoryModuleType.NEAREST_PLAYERS, players);
		BrainUtil.setOrClearMemory(entity, MemoryModuleType.NEAREST_VISIBLE_PLAYER, targetablePlayers.isEmpty() ? null : targetablePlayers.getFirst());
		BrainUtil.setOrClearMemory(entity, MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER, attackablePlayers.isEmpty() ? null : attackablePlayers.getFirst());
	}
	//</editor-fold>
}
