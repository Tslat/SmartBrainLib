package net.tslat.smartbrainlib;

import com.mojang.serialization.Codec;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.monster.SkeletonEntity;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.example.boilerplate.SBLExampleEntities;
import net.tslat.smartbrainlib.registry.SBLMemoryTypes;
import net.tslat.smartbrainlib.registry.SBLSensors;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Supplier;

public final class SBLForge implements SBLLoader {
	public static final DeferredRegister<MemoryModuleType<?>> MEMORY_TYPES = DeferredRegister.create(ForgeRegistries.MEMORY_MODULE_TYPES, SBLConstants.MOD_ID);
	public static final DeferredRegister<SensorType<?>> SENSORS = DeferredRegister.create(ForgeRegistries.SENSOR_TYPES, SBLConstants.MOD_ID);
	public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITIES, SBLConstants.MOD_ID);

	public void init() {
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

		MEMORY_TYPES.register(modEventBus);
		SENSORS.register(modEventBus);

		SBLMemoryTypes.init();
		SBLSensors.init();

		if (isDevEnv()) {
			ENTITY_TYPES.register(modEventBus);
			modEventBus.addListener(EventPriority.NORMAL, false, EntityAttributeCreationEvent.class, SBLForge::registerEntityStats);
			SBLExampleEntities.init();
		}
	}

	@Override
	public boolean isDevEnv() {
		return !FMLLoader.isProduction();
	}

	@Override
	public <T> Supplier<MemoryModuleType<T>> registerMemoryType(String id) {
		return registerMemoryType(id, null);
	}

	@Override
	public <T> Supplier<MemoryModuleType<T>> registerMemoryType(String id, @Nullable Codec<T> codec) {
		return MEMORY_TYPES.register(id, () -> new MemoryModuleType<T>(Optional.ofNullable(codec)));
	}

	@Override
	public <T extends ExtendedSensor<?>> Supplier<SensorType<T>> registerSensorType(String id, Supplier<T> sensor) {
		return SENSORS.register(id, () -> new SensorType<>(sensor));
	}

	@Override
	public <T extends LivingEntity> Supplier<EntityType<T>> registerEntityType(String id, Supplier<EntityType<T>> entityType) {
		return ENTITY_TYPES.register(id, entityType);
	}

	private static void registerEntityStats(final EntityAttributeCreationEvent ev) {
		ev.put(SBLExampleEntities.SBL_SKELETON.get(), SkeletonEntity.createAttributes().build());
	}
}
