package net.tslat.smartbrainlib.registry;

import net.minecraft.world.entity.ai.sensing.SensorType;
import net.tslat.smartbrainlib.SBLConstants;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.custom.GenericAttackTargetSensor;
import net.tslat.smartbrainlib.api.core.sensor.custom.IncomingProjectilesSensor;
import net.tslat.smartbrainlib.api.core.sensor.custom.NearbyBlocksSensor;
import net.tslat.smartbrainlib.api.core.sensor.custom.UnreachableTargetSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.*;

import java.util.function.Supplier;

/**
 * Registry class for {@link ExtendedSensor} implementations
 */
public final class SBLSensors {
	public static void init() {}

	// Vanilla sensors
	public static final Supplier<SensorType<NearestItemSensor<?>>> NEAREST_ITEM = register("nearest_item", NearestItemSensor::new);
	public static final Supplier<SensorType<NearbyLivingEntitySensor<?>>> NEARBY_LIVING_ENTITY = register("nearby_living_entity", NearbyLivingEntitySensor::new);
	public static final Supplier<SensorType<NearbyPlayersSensor<?>>> NEARBY_PLAYERS = register("nearby_players", NearbyPlayersSensor::new);
	public static final Supplier<SensorType<NearestHomeSensor<?>>> NEAREST_HOME = register("nearest_home", NearestHomeSensor::new);
	public static final Supplier<SensorType<HurtBySensor<?>>> HURT_BY = register("hurt_by", HurtBySensor::new);
	public static final Supplier<SensorType<NearbyHostileSensor<?>>> NEARBY_HOSTILE = register("nearby_hostile", NearbyHostileSensor::new);
	public static final Supplier<SensorType<NearbyBabySensor<?>>> NEARBY_BABY = register("nearby_baby", NearbyBabySensor::new);
	public static final Supplier<SensorType<SecondaryPoiSensor<?>>> SECONDARY_POI = register("secondary_poi", SecondaryPoiSensor::new);
	public static final Supplier<SensorType<NearbyGolemSensor<?>>> NEARBY_GOLEM = register("nearby_golem", NearbyGolemSensor::new);
	public static final Supplier<SensorType<NearbyAdultSensor<?>>> NEARBY_ADULT = register("nearby_adult", NearbyAdultSensor::new);
	public static final Supplier<SensorType<ItemTemptingSensor<?>>> ITEM_TEMPTING = register("item_tempting", ItemTemptingSensor::new);
	public static final Supplier<SensorType<InWaterSensor<?>>> IN_WATER = register("in_water", InWaterSensor::new);

	// Entity Specific
	public static final Supplier<SensorType<FrogSpecificSensor<?>>> FROG_SPECIFIC = register("frog_specific", FrogSpecificSensor::new);
	public static final Supplier<SensorType<AxolotlSpecificSensor<?>>> AXOLOTL_SPECIFIC = register("axolotl_specific", AxolotlSpecificSensor::new);
	public static final Supplier<SensorType<PiglinSpecificSensor<?>>> PIGLIN_SPECIFIC = register("piglin_specific", PiglinSpecificSensor::new);
	public static final Supplier<SensorType<PiglinBruteSpecificSensor<?>>> PIGLIN_BRUTE_SPECIFIC = register("piglin_brute_specific", PiglinBruteSpecificSensor::new);
	public static final Supplier<SensorType<HoglinSpecificSensor<?>>> HOGLIN_SPECIFIC = register("hoglin_specific", HoglinSpecificSensor::new);
	public static final Supplier<SensorType<WardenSpecificSensor<?>>> WARDEN_SPECIFIC = register("warden_specific", WardenSpecificSensor::new);

	// Custom
	public static final Supplier<SensorType<IncomingProjectilesSensor<?>>> INCOMING_PROJECTILES = register("incoming_projectiles", IncomingProjectilesSensor::new);
	public static final Supplier<SensorType<GenericAttackTargetSensor<?>>> GENERIC_ATTACK_TARGET = register("generic_attack_target", GenericAttackTargetSensor::new);
	public static final Supplier<SensorType<UnreachableTargetSensor<?>>> UNREACHABLE_TARGET = register("unreachable_target", UnreachableTargetSensor::new);
	public static final Supplier<SensorType<NearbyBlocksSensor<?>>> NEARBY_BLOCKS = register("nearby_blocks", NearbyBlocksSensor::new);

	private static <T extends ExtendedSensor<?>> Supplier<SensorType<T>> register(String id, Supplier<T> sensor) {
		return SBLConstants.SBL_LOADER.registerSensorType(id, sensor);
	}
}
