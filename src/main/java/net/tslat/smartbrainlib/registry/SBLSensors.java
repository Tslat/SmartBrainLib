package net.tslat.smartbrainlib.registry;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.tslat.smartbrainlib.SmartBrainLib;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.custom.GenericAttackTargetSensor;
import net.tslat.smartbrainlib.api.core.sensor.custom.IncomingProjectilesSensor;
import net.tslat.smartbrainlib.api.core.sensor.custom.NearbyBlocksSensor;
import net.tslat.smartbrainlib.api.core.sensor.custom.UnreachableTargetSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.AxolotlSpecificSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.FrogSpecificSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.HoglinSpecificSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.HurtBySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.InWaterSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.ItemTemptingSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyAdultSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyBabySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyGolemSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyHostileSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyLivingEntitySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyPlayersSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearestHomeSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearestItemSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.PiglinBruteSpecificSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.PiglinSpecificSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.SecondaryPoiSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.WardenSpecificSensor;
import net.tslat.smartbrainlib.mixin.SensorTypeInvoker;

/**
 * Registry class for {@link ExtendedSensor} implementations
 */
public final class SBLSensors {

	// Vanilla sensors
	public static final SensorType<NearestItemSensor<?>> NEAREST_ITEM = Registry.register(Registry.SENSOR_TYPE, new ResourceLocation(SmartBrainLib.MOD_ID, "nearest_item"), SensorTypeInvoker.createSensorType(NearestItemSensor::new));

	public static final SensorType<NearbyLivingEntitySensor<?>> NEARBY_LIVING_ENTITY = Registry.register(Registry.SENSOR_TYPE, new ResourceLocation(SmartBrainLib.MOD_ID, "nearby_living_entity"), SensorTypeInvoker.createSensorType(NearbyLivingEntitySensor::new));
	public static final SensorType<NearbyPlayersSensor<?>> NEARBY_PLAYERS = Registry.register(Registry.SENSOR_TYPE, new ResourceLocation(SmartBrainLib.MOD_ID, "nearby_players"), SensorTypeInvoker.createSensorType(NearbyPlayersSensor::new));
	public static final SensorType<NearestHomeSensor<?>> NEAREST_HOME = Registry.register(Registry.SENSOR_TYPE, new ResourceLocation(SmartBrainLib.MOD_ID, "nearest_home"), SensorTypeInvoker.createSensorType(NearestHomeSensor::new));
	public static final SensorType<HurtBySensor<?>> HURT_BY = Registry.register(Registry.SENSOR_TYPE, new ResourceLocation(SmartBrainLib.MOD_ID, "hurt_by"), SensorTypeInvoker.createSensorType(HurtBySensor::new));
	public static final SensorType<NearbyHostileSensor<?>> NEARBY_HOSTILE = Registry.register(Registry.SENSOR_TYPE, new ResourceLocation(SmartBrainLib.MOD_ID, "nearby_hostile"), SensorTypeInvoker.createSensorType(NearbyHostileSensor::new));
	public static final SensorType<NearbyBabySensor<?>> NEARBY_BABY = Registry.register(Registry.SENSOR_TYPE, new ResourceLocation(SmartBrainLib.MOD_ID, "nearby_baby"), SensorTypeInvoker.createSensorType(NearbyBabySensor::new));
	public static final SensorType<SecondaryPoiSensor<?>> SECONDARY_POI = Registry.register(Registry.SENSOR_TYPE, new ResourceLocation(SmartBrainLib.MOD_ID, "secondary_poi"), SensorTypeInvoker.createSensorType(SecondaryPoiSensor::new));
	public static final SensorType<NearbyGolemSensor<?>> NEARBY_GOLEM = Registry.register(Registry.SENSOR_TYPE, new ResourceLocation(SmartBrainLib.MOD_ID, "nearby_golem"), SensorTypeInvoker.createSensorType(NearbyGolemSensor::new));
	public static final SensorType<NearbyAdultSensor<?>> NEARBY_ADULT = Registry.register(Registry.SENSOR_TYPE, new ResourceLocation(SmartBrainLib.MOD_ID, "nearby_adult"), SensorTypeInvoker.createSensorType(NearbyAdultSensor::new));
	public static final SensorType<ItemTemptingSensor<?>> ITEM_TEMPTING = Registry.register(Registry.SENSOR_TYPE, new ResourceLocation(SmartBrainLib.MOD_ID, "item_tempting"), SensorTypeInvoker.createSensorType(ItemTemptingSensor::new));
	public static final SensorType<InWaterSensor<?>> IN_WATER = Registry.register(Registry.SENSOR_TYPE, new ResourceLocation(SmartBrainLib.MOD_ID, "in_water"), SensorTypeInvoker.createSensorType(InWaterSensor::new));

	// Entity Specific
	public static final SensorType<FrogSpecificSensor<?>> FROG_SPECIFIC = Registry.register(Registry.SENSOR_TYPE, new ResourceLocation(SmartBrainLib.MOD_ID, "frog_specific"), SensorTypeInvoker.createSensorType(FrogSpecificSensor::new));
	public static final SensorType<AxolotlSpecificSensor<?>> AXOLOTL_SPECIFIC = Registry.register(Registry.SENSOR_TYPE, new ResourceLocation(SmartBrainLib.MOD_ID, "axolotl_specific"), SensorTypeInvoker.createSensorType(AxolotlSpecificSensor::new));
	public static final SensorType<PiglinSpecificSensor<?>> PIGLIN_SPECIFIC = Registry.register(Registry.SENSOR_TYPE, new ResourceLocation(SmartBrainLib.MOD_ID, "piglin_specific"), SensorTypeInvoker.createSensorType(PiglinSpecificSensor::new));
	public static final SensorType<PiglinBruteSpecificSensor<?>> PIGLIN_BRUTE_SPECIFIC = Registry.register(Registry.SENSOR_TYPE, new ResourceLocation(SmartBrainLib.MOD_ID, "piglin_brute_specific"), SensorTypeInvoker.createSensorType(PiglinBruteSpecificSensor::new));
	public static final SensorType<HoglinSpecificSensor<?>> HOGLIN_SPECIFIC = Registry.register(Registry.SENSOR_TYPE, new ResourceLocation(SmartBrainLib.MOD_ID, "hoglin_specific"), SensorTypeInvoker.createSensorType(HoglinSpecificSensor::new));
	public static final SensorType<WardenSpecificSensor<?>> WARDEN_SPECIFIC = Registry.register(Registry.SENSOR_TYPE, new ResourceLocation(SmartBrainLib.MOD_ID, "warden_speciifc"), SensorTypeInvoker.createSensorType(WardenSpecificSensor::new));

	// Custom
	public static final SensorType<IncomingProjectilesSensor<?>> INCOMING_PROJECTILES = Registry.register(Registry.SENSOR_TYPE, new ResourceLocation(SmartBrainLib.MOD_ID, "incoming_projectiles"), SensorTypeInvoker.createSensorType(IncomingProjectilesSensor::new));
	public static final SensorType<GenericAttackTargetSensor<?>> GENERIC_ATTACK_TARGET = Registry.register(Registry.SENSOR_TYPE, new ResourceLocation(SmartBrainLib.MOD_ID, "generic_attack_target"), SensorTypeInvoker.createSensorType(GenericAttackTargetSensor::new));
	public static final SensorType<UnreachableTargetSensor<?>> UNREACHABLE_TARGET = Registry.register(Registry.SENSOR_TYPE, new ResourceLocation(SmartBrainLib.MOD_ID, "unreachable_target"), SensorTypeInvoker.createSensorType(UnreachableTargetSensor::new));
	public static final SensorType<NearbyBlocksSensor<?>> NEARBY_BLOCKS = Registry.register(Registry.SENSOR_TYPE, new ResourceLocation(SmartBrainLib.MOD_ID, "nearby_blocks"), SensorTypeInvoker.createSensorType(NearbyBlocksSensor::new));
	
	public static void init(){}
}
