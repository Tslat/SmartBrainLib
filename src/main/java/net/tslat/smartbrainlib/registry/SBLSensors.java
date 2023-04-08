package net.tslat.smartbrainlib.registry;

import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.tslat.smartbrainlib.SmartBrainLib;
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
	public static final DeferredRegister<SensorType<?>> SENSORS = DeferredRegister.create(ForgeRegistries.SENSOR_TYPES, SmartBrainLib.MOD_ID);

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
	public static final RegistryObject<SensorType<PiglinSpecificSensor<?>>> PIGLIN_SPECIFIC = register("piglin_specific", PiglinSpecificSensor::new);
	public static final RegistryObject<SensorType<PiglinBruteSpecificSensor<?>>> PIGLIN_BRUTE_SPECIFIC = register("piglin_brute_specific", PiglinBruteSpecificSensor::new);
	public static final RegistryObject<SensorType<HoglinSpecificSensor<?>>> HOGLIN_SPECIFIC = register("hoglin_specific", HoglinSpecificSensor::new);

	// Custom
	public static final RegistryObject<SensorType<IncomingProjectilesSensor<?>>> INCOMING_PROJECTILES = register("incoming_projectiles", IncomingProjectilesSensor::new);
	public static final RegistryObject<SensorType<GenericAttackTargetSensor<?>>> GENERIC_ATTACK_TARGET = register("generic_attack_target", GenericAttackTargetSensor::new);
	public static final RegistryObject<SensorType<UnreachableTargetSensor<?>>> UNREACHABLE_TARGET = register("unreachable_target", UnreachableTargetSensor::new);
	public static final RegistryObject<SensorType<NearbyBlocksSensor<?>>> NEARBY_BLOCKS = register("nearby_blocks", NearbyBlocksSensor::new);

	private static <T extends ExtendedSensor<?>> RegistryObject<SensorType<T>> register(String id, Supplier<T> sensor) {
		return SENSORS.register(id, () -> new SensorType<>(sensor));
	}
}
