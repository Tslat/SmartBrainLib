package net.tslat.smartbrainlib;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.Level;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.entity.PartEntity;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/// `NeoForge` platform specific SPI implementation for [SBLPlatform]
@SuppressWarnings("NullableProblems")
public final class SBLNeoForge implements SBLPlatform {
	/// @return true if the current runtime environment is in-development, false otherwise
	@Override
	public boolean isDevEnv() {
		return !FMLLoader.getCurrent().isProduction();
	}

	/// Return the multipart entities loaded in the provided level
	///
	/// By default, Minecraft only has multipart entities on the [EnderDragon], but `Forge` and `NeoForge` both offer extensible implementations that
	/// should be accounted for
	@Override
	public Pair<Collection<? extends Entity>, Function<Entity, ? extends Entity>> getPartEntities(Level level) {
		return Pair.of(level.dragonParts(), entity -> ((PartEntity<?>)entity).getParent());
	}

	/// Return the array of part entities belonging to this entity, if any
	@Override
	public Entity[] getPartEntities(Entity entity) {
		final PartEntity<?>[] parts = entity.getParts();

        //noinspection ConstantValue
        return parts == null ? new Entity[0] : parts;
	}

	/// Register a custom [MemoryModuleType] under the `SmartBrainLib` namespace
	///
	/// You should <u>**NOT**</u> be using this
	@Override
	public <T> Supplier<MemoryModuleType<T>> registerMemoryType(String id, Optional<Codec<T>> codec) {
		return SmartBrainLib.MEMORY_TYPES.register(id, () -> new MemoryModuleType<>(codec));
	}

	/// Register a custom [SensorType] under the `SmartBrainLib` namespace
	///
	/// You should <u>**NOT**</u> be using this
	@Override
	public <T extends ExtendedSensor<?>> Supplier<SensorType<T>> registerSensorType(String id, Supplier<T> sensor) {
		return SmartBrainLib.SENSORS.register(id, () -> new SensorType<>(sensor));
	}
}
