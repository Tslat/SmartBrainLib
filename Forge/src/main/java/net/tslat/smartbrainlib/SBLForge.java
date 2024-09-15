package net.tslat.smartbrainlib;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.level.Level;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.example.SBLSkeleton;
import net.tslat.smartbrainlib.registry.SBLMemoryTypes;
import net.tslat.smartbrainlib.registry.SBLSensors;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public final class SBLForge implements SBLLoader {
	public static final DeferredRegister<MemoryModuleType<?>> MEMORY_TYPES = DeferredRegister.create(ForgeRegistries.Keys.MEMORY_MODULE_TYPES, SBLConstants.MOD_ID);
	public static final DeferredRegister<SensorType<?>> SENSORS = DeferredRegister.create(ForgeRegistries.Keys.SENSOR_TYPES, SBLConstants.MOD_ID);
	public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.Keys.ENTITY_TYPES, SBLConstants.MOD_ID);

	public static RegistryObject<EntityType<SBLSkeleton>> SBL_SKELETON;

	public void init() {
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

		MEMORY_TYPES.register(modEventBus);
		SENSORS.register(modEventBus);

		SBLMemoryTypes.init();
		SBLSensors.init();

		if (isDevEnv())
			registerEntities(modEventBus);
	}

	@Override
	public boolean isDevEnv() {
		return !FMLLoader.isProduction();
	}

	@Override
	public Pair<Collection<? extends Entity>, Function<Entity, ? extends Entity>> getPartEntities(Level level) {
		return Pair.of(level.getPartEntities(), entity -> ((PartEntity<?>)entity).getParent());
	}

	@Override
	@ApiStatus.Internal
	public <T> Supplier<MemoryModuleType<T>> registerMemoryType(String id) {
		return registerMemoryType(id, Optional.empty());
	}

	@Override
	@ApiStatus.Internal
	public <T> Supplier<MemoryModuleType<T>> registerMemoryType(String id, Optional<Codec<T>> codec) {
		return MEMORY_TYPES.register(id, () -> new MemoryModuleType<>(codec));
	}

	@Override
	@ApiStatus.Internal
	public <T extends ExtendedSensor<?>> Supplier<SensorType<T>> registerSensorType(String id, Supplier<T> sensor) {
		return SENSORS.register(id, () -> new SensorType<>(sensor));
	}

	private static void registerEntities(IEventBus modEventBus) {
		ENTITY_TYPES.register(modEventBus);
		modEventBus.addListener(EventPriority.NORMAL, false, EntityAttributeCreationEvent.class, ev -> {
			ev.put(SBL_SKELETON.get(), Skeleton.createAttributes().build());
		});

		SBL_SKELETON = ENTITY_TYPES.register("sbl_skeleton", () -> EntityType.Builder.of(SBLSkeleton::new, MobCategory.MONSTER).sized(0.6f, 1.99f).build("sbl_skeleton"));
	}
}
