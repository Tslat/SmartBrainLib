package net.tslat.smartbrainlib.registry;

import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.tslat.smartbrainlib.SmartBrainLib;
import net.tslat.smartbrainlib.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.core.sensor.custom.GenericAttackTargetSensor;
import net.tslat.smartbrainlib.core.sensor.custom.IncomingProjectilesSensor;
import net.tslat.smartbrainlib.core.sensor.custom.UnreachableTargetSensor;
import net.tslat.smartbrainlib.core.sensor.vanilla.*;

import java.util.function.Supplier;

/**
 * Registry class for {@link net.tslat.smartbrainlib.core.sensor.ExtendedSensor} implementations
 */
public final class SBLSensors {
	public static final DeferredRegister<SensorType<?>> SENSORS = DeferredRegister.create(ForgeRegistries.Keys.SENSOR_TYPES, SmartBrainLib.MOD_ID);

	// Vanilla sensors
	public static final RegistryObject<SensorType<NearestItemSensor<?>>> NEAREST_ITEM = register("nearest_item", NearestItemSensor::new);
	public static final RegistryObject<SensorType<NearbyLivingEntitySensor<?>>> NEARBY_LIVING_ENTITY = register("nearby_living_entity", NearbyLivingEntitySensor::new);
	public static final RegistryObject<SensorType<NearbyPlayersSensor<?>>> NEARBY_PLAYERS = register("nearby_players", NearbyPlayersSensor::new);
	public static final RegistryObject<SensorType<NearestHomeSensor<?>>> NEAREST_HOME = register("nearest_home", NearestHomeSensor::new);
	public static final RegistryObject<SensorType<HurtBySensor<?>>> HURT_BY = register("hurt_by", HurtBySensor::new);
	public static final RegistryObject<SensorType<NearbyHostileSensor<?>>> NEARBY_HOSTILE = register("nearby_hostile", NearbyHostileSensor::new);
	public static final RegistryObject<SensorType<NearbyBabySensor<?>>> NEARBY_BABY = register("nearby_baby", NearbyBabySensor::new);
	public static final RegistryObject<SensorType<SecondaryPoiSensor<?>>> SECONDARY_POI = register("secondary_poi", SecondaryPoiSensor::new);
	public static final RegistryObject<SensorType<NearbyGolemSensor<?>>> NEARBY_GOLEM = register("nearby_golem", NearbyGolemSensor::new);
	public static final RegistryObject<SensorType<NearbyAdultSensor<?>>> NEARBY_ADULT = register("nearby_adult", NearbyAdultSensor::new);
	public static final RegistryObject<SensorType<ItemTemptingSensor<?>>> ITEM_TEMPTING = register("item_tempting", ItemTemptingSensor::new);
	public static final RegistryObject<SensorType<InWaterSensor<?>>> IN_WATER = register("in_water", InWaterSensor::new);

	// Entity Specific
	public static final RegistryObject<SensorType<FrogSpecificSensor<?>>> FROG_SPECIFIC = register("frog_specific", FrogSpecificSensor::new);
	public static final RegistryObject<SensorType<AxolotlSpecificSensor<?>>> AXOLOTL_SPECIFIC = register("axolotl_specific", AxolotlSpecificSensor::new);
	public static final RegistryObject<SensorType<PiglinSpecificSensor<?>>> PIGLIN_SPECIFIC = register("piglin_specific", PiglinSpecificSensor::new);
	public static final RegistryObject<SensorType<PiglinBruteSpecificSensor<?>>> PIGLIN_BRUTE_SPECIFIC = register("piglin_brute_specific", PiglinBruteSpecificSensor::new);
	public static final RegistryObject<SensorType<HoglinSpecificSensor<?>>> HOGLIN_SPECIFIC = register("hoglin_specific", HoglinSpecificSensor::new);
	public static final RegistryObject<SensorType<WardenSpecificSensor<?>>> WARDEN_SPECIFIC = register("warden_speciifc", WardenSpecificSensor::new);

	// Custom
	public static final RegistryObject<SensorType<IncomingProjectilesSensor<?>>> INCOMING_PROJECTILES = register("incoming_projectiles", IncomingProjectilesSensor::new);
	public static final RegistryObject<SensorType<GenericAttackTargetSensor<?>>> GENERIC_ATTACK_TARGET = register("generic_attack_target", GenericAttackTargetSensor::new);
	public static final RegistryObject<SensorType<UnreachableTargetSensor<?>>> UNREACHABLE_TARGET = register("unreachable_target", UnreachableTargetSensor::new);

	private static <T extends ExtendedSensor<?>> RegistryObject<SensorType<T>> register(String id, Supplier<T> sensor) {
		return SENSORS.register(id, () -> new SensorType<>(sensor));
	}
}
