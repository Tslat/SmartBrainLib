package net.tslat.smartbrainlib;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.monster.Skeleton;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.example.SBLSkeleton;
import net.tslat.smartbrainlib.example.boilerplate.SBLExampleEntities;
import net.tslat.smartbrainlib.mixin.MemoryTypesInvoker;
import net.tslat.smartbrainlib.mixin.SensorTypeInvoker;
import net.tslat.smartbrainlib.registry.SBLMemoryTypes;
import net.tslat.smartbrainlib.registry.SBLSensors;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.loader.api.QuiltLoader;

import java.util.function.Supplier;

public final class SBLQuilt implements SBLLoader {
	public static EntityType<SBLSkeleton> SBL_SKELETON;

	public void init() {
		SBLMemoryTypes.init();
		SBLSensors.init();

		if (isDevEnv()) {
			SBL_SKELETON = Registry.register(BuiltInRegistries.ENTITY_TYPE, new ResourceLocation(SBLConstants.MOD_ID, "sbl_skeleton"), FabricEntityTypeBuilder.create(MobCategory.MONSTER, SBLSkeleton::new).dimensions(EntityDimensions.scalable(0.6f, 1.99f)).build());
			registerEntityStats();
		}
	}

	@Override
	public boolean isDevEnv() {
		return QuiltLoader.isDevelopmentEnvironment();
	}

	@Override
	public <T> Supplier<MemoryModuleType<T>> registerMemoryType(String id) {
		return registerMemoryType(id, null);
	}

	@Override
	public <T> Supplier<MemoryModuleType<T>> registerMemoryType(String id, @Nullable Codec<T> codec) {
		MemoryModuleType<T> memoryType = MemoryTypesInvoker.invokeRegister(SBLConstants.MOD_ID + ":" + id);

		return () -> memoryType;
	}

	@Override
	public <T extends ExtendedSensor<?>> Supplier<SensorType<T>> registerSensorType(String id, Supplier<T> sensor) {
		SensorType<T> sensorType = Registry.register(BuiltInRegistries.SENSOR_TYPE, new ResourceLocation(SBLConstants.MOD_ID, id), SensorTypeInvoker.createSensorType(sensor));

		return () -> sensorType;
	}

	@Override
	public <T extends LivingEntity> Supplier<EntityType<T>> registerEntityType(String id, Supplier<EntityType<T>> entityType) {
		Registry.register(BuiltInRegistries.ENTITY_TYPE, new ResourceLocation(SBLConstants.MOD_ID, id), entityType.get());

		return entityType;
	}

	private static void registerEntityStats() {
		FabricDefaultAttributeRegistry.register(SBL_SKELETON, Skeleton.createAttributes());
	}
}
