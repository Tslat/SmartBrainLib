package net.tslat.smartbrainlib;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.entity.PartEntity;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.example.SBLSkeleton;
import net.tslat.smartbrainlib.registry.SBLMemoryTypes;
import net.tslat.smartbrainlib.registry.SBLSensors;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public final class SBLNeoForge implements SBLLoader {
	public static final DeferredRegister<MemoryModuleType<?>> MEMORY_TYPES = DeferredRegister.create(Registries.MEMORY_MODULE_TYPE, SBLConstants.MOD_ID);
	public static final DeferredRegister<SensorType<?>> SENSORS = DeferredRegister.create(Registries.SENSOR_TYPE, SBLConstants.MOD_ID);
	public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, SBLConstants.MOD_ID);

	public static DeferredHolder<EntityType<?>, EntityType<SBLSkeleton>> SBL_SKELETON;

	public void init(Object eventBus) {
		final IEventBus modEventBus = (IEventBus)eventBus;

		MEMORY_TYPES.register(modEventBus);
		SENSORS.register(modEventBus);

		SBLMemoryTypes.init();
		SBLSensors.init();

		if (isDevEnv())
			registerEntities(modEventBus);
	}

	@Override
	public boolean isDevEnv() {
		return !FMLLoader.getCurrent().isProduction();
	}

	@Override
	public Pair<Collection<? extends Entity>, Function<Entity, ? extends Entity>> getPartEntities(Level level) {
		return Pair.of(level.dragonParts(), entity -> ((PartEntity<?>)entity).getParent());
	}

	@Override
	public <T> Supplier<MemoryModuleType<T>> registerMemoryType(String id) {
		return registerMemoryType(id, Optional.empty());
	}

	@Override
	public <T> Supplier<MemoryModuleType<T>> registerMemoryType(String id, Optional<Codec<T>> codec) {
		return MEMORY_TYPES.register(id, () -> new MemoryModuleType<>(codec));
	}

	@Override
	public <T extends ExtendedSensor<?>> Supplier<SensorType<T>> registerSensorType(String id, Supplier<T> sensor) {
		return SENSORS.register(id, () -> new SensorType<>(sensor));
	}

	private static void registerEntities(IEventBus modEventBus) {
		ENTITY_TYPES.register(modEventBus);
		modEventBus.addListener(EventPriority.NORMAL, false, EntityAttributeCreationEvent.class, ev -> {
			ev.put(SBL_SKELETON.get(), Skeleton.createAttributes().build());
		});

		SBL_SKELETON = ENTITY_TYPES.register("sbl_skeleton", () -> EntityType.Builder.of(SBLSkeleton::new, MobCategory.MONSTER).sized(0.6f, 1.99f).build(ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(SBLConstants.MOD_ID, "sbl_skeleton"))));
	}
}
