package net.tslat.smartbrainlib;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.monster.SkeletonEntity;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.tslat.smartbrainlib.example.boilerplate.SBLExampleEntities;
import net.tslat.smartbrainlib.example.boilerplate.SBLExampleEntityRenderers;
import net.tslat.smartbrainlib.registry.SBLMemoryTypes;
import net.tslat.smartbrainlib.registry.SBLSensors;

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

	public static void clientSetup(final FMLClientSetupEvent ev) {
		if (SBLConstants.SBL_LOADER.isDevEnv())
			SBLExampleEntityRenderers.registerEntityRenderers();
	}

								   @Override
	public boolean isDevEnv() {
		return !FMLLoader.isProduction();
	}

	@Override
	public Supplier<MemoryModuleType<Object>> registerMemoryType(String id) {
		return MEMORY_TYPES.register(id, () -> new MemoryModuleType<>(Optional.empty()));
	}

	@Override
	public Supplier<SensorType> registerSensorType(String id, Supplier sensor) {
		return SENSORS.register(id, () -> new SensorType<>(sensor));
	}

	@Override
	public Supplier<EntityType> registerEntityType(String id, Supplier entityType) {
		return ENTITY_TYPES.register(id, entityType);
	}

	private static void registerEntityStats(final EntityAttributeCreationEvent ev) {
		ev.put((EntityType)((Supplier)SBLExampleEntities.SBL_SKELETON).get(), SkeletonEntity.createAttributes().build());
	}
}
