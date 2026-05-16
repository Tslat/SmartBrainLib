package net.tslat.smartbrainlib.registry;

import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.tslat.smartbrainlib.SBLConstants;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.custom.*;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.*;

import java.util.function.Supplier;

/// Registry class for builtin [ExtendedSensor] implementations
///
/// Hosts 3 pseudo-categories of [SensorType]:
/// <ol>
/// 	<li>Vanilla Sensors - [ExtendedSensor] implementations of generic vanilla [Sensor] types, often with extended functionality</li>
/// 	<li>Entity-Specific Sensors - [ExtendedSensor] implementations of vanilla entity-specific [Sensor] types. Mostly just for completeness</li>
/// 	<li>Custom Sensors - New [ExtendedSensor] implementations made by `SmartBrainLib` for developer or internal usage</li>
/// </ol>
public final class SBLSensors {
	/// Vanilla sensors
	public static final Supplier<SensorType<HurtBySensor<?>>> HURT_BY = register("hurt_by", HurtBySensor::new);
	public static final Supplier<SensorType<InWaterSensor<?>>> IN_WATER = register("in_water", InWaterSensor::new);
	public static final Supplier<SensorType<ItemTemptingSensor<?>>> ITEM_TEMPTING = register("item_tempting", ItemTemptingSensor::new);
	public static final Supplier<SensorType<NearbyAdultSensor<?>>> NEARBY_ADULT = register("nearby_adult", NearbyAdultSensor::new);
	public static final Supplier<SensorType<NearbyBabySensor<?>>> NEARBY_BABY = register("nearby_baby", NearbyBabySensor::new);
	public static final Supplier<SensorType<NearbyGolemSensor<?>>> NEARBY_GOLEM = register("nearby_golem", NearbyGolemSensor::new);
	public static final Supplier<SensorType<NearbyHostileSensor<?>>> NEARBY_HOSTILE = register("nearby_hostile", NearbyHostileSensor::new);
	public static final Supplier<SensorType<NearbyLivingEntitySensor<?>>> NEARBY_LIVING_ENTITY = register("nearby_living_entity", NearbyLivingEntitySensor::new);
	public static final Supplier<SensorType<NearbyPlayersSensor<?>>> NEARBY_PLAYERS = register("nearby_players", NearbyPlayersSensor::new);
	public static final Supplier<SensorType<NearestBabyBedSensor<?>>> NEAREST_HOME = register("nearest_home", NearestBabyBedSensor::new);
	public static final Supplier<SensorType<NearestItemSensor<?>>> NEAREST_ITEM = register("nearest_item", NearestItemSensor::new);
	public static final Supplier<SensorType<SecondaryPoiSensor<?>>> SECONDARY_POI = register("secondary_poi", SecondaryPoiSensor::new);

	/// Entity Specific
	public static final Supplier<SensorType<AxolotlSpecificSensor<?>>> AXOLOTL_SPECIFIC = register("axolotl_specific", AxolotlSpecificSensor::new);
	public static final Supplier<SensorType<FrogSpecificSensor<?>>> FROG_SPECIFIC = register("frog_specific", FrogSpecificSensor::new);
	public static final Supplier<SensorType<HoglinSpecificSensor<?>>> HOGLIN_SPECIFIC = register("hoglin_specific", HoglinSpecificSensor::new);
	public static final Supplier<SensorType<PiglinBruteSpecificSensor<?>>> PIGLIN_BRUTE_SPECIFIC = register("piglin_brute_specific", PiglinBruteSpecificSensor::new);
	public static final Supplier<SensorType<PiglinSpecificSensor<?>>> PIGLIN_SPECIFIC = register("piglin_specific", PiglinSpecificSensor::new);
	public static final Supplier<SensorType<WardenSpecificSensor<?>>> WARDEN_SPECIFIC = register("warden_specific", WardenSpecificSensor::new);

	/// Custom
	public static final Supplier<SensorType<GenericAttackTargetSensor<?>>> GENERIC_ATTACK_TARGET = register("generic_attack_target", GenericAttackTargetSensor::new);
	public static final Supplier<SensorType<IncomingProjectilesSensor<?>>> INCOMING_PROJECTILES = register("incoming_projectiles", IncomingProjectilesSensor::new);
	public static final Supplier<SensorType<NearbyBlocksSensor<?>>> NEARBY_BLOCKS = register("nearby_blocks", NearbyBlocksSensor::new);
	public static final Supplier<SensorType<NearbyItemsSensor<?>>> NEARBY_ITEMS = register("nearby_items", NearbyItemsSensor::new);
	public static final Supplier<SensorType<UnreachableTargetSensor<?>>> UNREACHABLE_TARGET = register("unreachable_target", UnreachableTargetSensor::new);

	//<editor-fold defaultstate="collapsed" desc="<Boilerplate>">
	public static void init() {}

	/// Register a custom [SensorType]
	private static <T extends ExtendedSensor<?>> Supplier<SensorType<T>> register(String id, Supplier<T> sensor) {
		return SBLConstants.PLATFORM.registerSensorType(id, sensor);
	}
	//</editor-fold>
}
