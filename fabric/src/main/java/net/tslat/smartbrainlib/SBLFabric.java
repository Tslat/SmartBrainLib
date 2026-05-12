package net.tslat.smartbrainlib;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.Level;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.internal.SmartBrain;
import net.tslat.smartbrainlib.api.internal.SmartBrainProvider;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/// `Fabric` platform specific SPI implementation for [SBLPlatform]
@SuppressWarnings("NullableProblems")
public final class SBLFabric implements SBLPlatform {
	/// @return true if the current runtime environment is in-development, false otherwise
	@Override
	public boolean isDevEnv() {
		return FabricLoader.getInstance().isDevelopmentEnvironment();
	}

	/// Return the multipart entities loaded in the provided level
	///
	/// By default, Minecraft only has multipart entities on the [EnderDragon], but `Forge` and `NeoForge` both offer extensible implementations that
	/// should be accounted for
	@Override
	public Pair<Collection<? extends Entity>, Function<Entity, ? extends Entity>> getPartEntities(Level level) {
		return Pair.of(List.of(), Function.identity());
	}

	/// Return the array of part entities belonging to this entity, if any
	@Override
	public Entity[] getPartEntities(Entity entity) {
		return entity instanceof EnderDragon dragon ? dragon.getSubEntities() : new Entity[0];
	}

	/// Register a custom [MemoryModuleType] under the `SmartBrainLib` namespace
	///
	/// You should <u>**NOT**</u> be using this
	@ApiStatus.Internal
	@Override
	public <T> Supplier<MemoryModuleType<T>> registerMemoryType(String id, Optional<Codec<T>> codec) {
		final MemoryModuleType<T> memoryType = Registry.register(BuiltInRegistries.MEMORY_MODULE_TYPE, Identifier.fromNamespaceAndPath(SBLConstants.MOD_ID, id), new MemoryModuleType<>(codec));

		return () -> memoryType;
	}

	/// Register a custom [SensorType] under the `SmartBrainLib` namespace
	///
	/// You should <u>**NOT**</u> be using this
	@ApiStatus.Internal
	@Override
	public <T extends ExtendedSensor<?>> Supplier<SensorType<T>> registerSensorType(String id, Supplier<T> sensor) {
		final SensorType<T> sensorType = Registry.register(BuiltInRegistries.SENSOR_TYPE, Identifier.fromNamespaceAndPath(SBLConstants.MOD_ID, id), new SensorType<>(sensor));

		return () -> sensorType;
	}
}
