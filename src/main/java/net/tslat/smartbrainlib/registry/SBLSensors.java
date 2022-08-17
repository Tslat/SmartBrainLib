package net.tslat.smartbrainlib.registry;

import net.minecraft.world.entity.ai.sensing.SensorType;
import net.tslat.smartbrainlib.core.sensor.custom.GenericAttackTargetSensor;
import net.tslat.smartbrainlib.core.sensor.custom.IncomingProjectilesSensor;
import net.tslat.smartbrainlib.core.sensor.vanilla.AxolotlSpecificSensor;
import net.tslat.smartbrainlib.core.sensor.vanilla.FrogSpecificSensor;
import net.tslat.smartbrainlib.core.sensor.vanilla.HoglinSpecificSensor;
import net.tslat.smartbrainlib.core.sensor.vanilla.HurtBySensor;
import net.tslat.smartbrainlib.core.sensor.vanilla.InWaterSensor;
import net.tslat.smartbrainlib.core.sensor.vanilla.ItemTemptingSensor;
import net.tslat.smartbrainlib.core.sensor.vanilla.NearbyAdultSensor;
import net.tslat.smartbrainlib.core.sensor.vanilla.NearbyBabySensor;
import net.tslat.smartbrainlib.core.sensor.vanilla.NearbyGolemSensor;
import net.tslat.smartbrainlib.core.sensor.vanilla.NearbyHostileSensor;
import net.tslat.smartbrainlib.core.sensor.vanilla.NearbyLivingEntitySensor;
import net.tslat.smartbrainlib.core.sensor.vanilla.NearbyPlayersSensor;
import net.tslat.smartbrainlib.core.sensor.vanilla.NearestHomeSensor;
import net.tslat.smartbrainlib.core.sensor.vanilla.NearestItemSensor;
import net.tslat.smartbrainlib.core.sensor.vanilla.PiglinBruteSpecificSensor;
import net.tslat.smartbrainlib.core.sensor.vanilla.PiglinSpecificSensor;
import net.tslat.smartbrainlib.core.sensor.vanilla.SecondaryPoiSensor;
import net.tslat.smartbrainlib.core.sensor.vanilla.WardenSpecificSensor;
import net.tslat.smartbrainlib.mixin.SensorTypeInvoker;

/**
 * Registry class for {@link net.tslat.smartbrainlib.core.sensor.ExtendedSensor} implementations
 */
public final class SBLSensors {

	// Vanilla sensors
	public static final SensorType<NearestItemSensor<?>> NEAREST_ITEM = SensorTypeInvoker.register("nearest_item", NearestItemSensor::new);
	public static final SensorType<NearbyLivingEntitySensor<?>> NEARBY_LIVING_ENTITY = SensorTypeInvoker.register("nearby_living_entity", NearbyLivingEntitySensor::new);
	public static final SensorType<NearbyPlayersSensor<?>> NEARBY_PLAYERS = SensorTypeInvoker.register("nearby_players", NearbyPlayersSensor::new);
	public static final SensorType<NearestHomeSensor<?>> NEAREST_HOME = SensorTypeInvoker.register("nearest_home", NearestHomeSensor::new);
	public static final SensorType<HurtBySensor<?>> HURT_BY = SensorTypeInvoker.register("hurt_by", HurtBySensor::new);
	public static final SensorType<NearbyHostileSensor<?>> NEARBY_HOSTILE = SensorTypeInvoker.register("nearby_hostile", NearbyHostileSensor::new);
	public static final SensorType<NearbyBabySensor<?>> NEARBY_BABY = SensorTypeInvoker.register("nearby_baby", NearbyBabySensor::new);
	public static final SensorType<SecondaryPoiSensor<?>> SECONDARY_POI = SensorTypeInvoker.register("secondary_poi", SecondaryPoiSensor::new);
	public static final SensorType<NearbyGolemSensor<?>> NEARBY_GOLEM = SensorTypeInvoker.register("nearby_golem", NearbyGolemSensor::new);
	public static final SensorType<NearbyAdultSensor<?>> NEARBY_ADULT = SensorTypeInvoker.register("nearby_adult", NearbyAdultSensor::new);
	public static final SensorType<ItemTemptingSensor<?>> ITEM_TEMPTING = SensorTypeInvoker.register("item_tempting", ItemTemptingSensor::new);
	public static final SensorType<InWaterSensor<?>> IN_WATER = SensorTypeInvoker.register("in_water", InWaterSensor::new);

	// Entity Specific
	public static final SensorType<FrogSpecificSensor<?>> FROG_SPECIFIC = SensorTypeInvoker.register("frog_specific", FrogSpecificSensor::new);
	public static final SensorType<AxolotlSpecificSensor<?>> AXOLOTL_SPECIFIC = SensorTypeInvoker.register("axolotl_specific", AxolotlSpecificSensor::new);
	public static final SensorType<PiglinSpecificSensor<?>> PIGLIN_SPECIFIC = SensorTypeInvoker.register("piglin_specific", PiglinSpecificSensor::new);
	public static final SensorType<PiglinBruteSpecificSensor<?>> PIGLIN_BRUTE_SPECIFIC = SensorTypeInvoker.register("piglin_brute_specific", PiglinBruteSpecificSensor::new);
	public static final SensorType<HoglinSpecificSensor<?>> HOGLIN_SPECIFIC = SensorTypeInvoker.register("hoglin_specific", HoglinSpecificSensor::new);
	public static final SensorType<WardenSpecificSensor<?>> WARDEN_SPECIFIC = SensorTypeInvoker.register("warden_speciifc", WardenSpecificSensor::new);

	// Custom
	public static final SensorType<IncomingProjectilesSensor<?>> INCOMING_PROJECTILES = SensorTypeInvoker.register("incoming_projectiles", IncomingProjectilesSensor::new);
	public static final SensorType<GenericAttackTargetSensor<?>> GENERIC_ATTACK_TARGET = SensorTypeInvoker.register("generic_attack_target", GenericAttackTargetSensor::new);

}
