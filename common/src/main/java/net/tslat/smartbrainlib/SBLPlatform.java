package net.tslat.smartbrainlib;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.Level;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.ActivityBuilder;
import net.tslat.smartbrainlib.api.core.schedule.SmartBrainSchedule;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.internal.SmartBrain;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/// Loader-agnostic SPI for `SmartBrainLib` functionality
@SuppressWarnings("NullableProblems")
public interface SBLPlatform {
	/// @return true if the current runtime environment is in-development, false otherwise
	boolean isDevEnv();

	/// Create a new [SmartBrain] instance from the given values
	///
	/// This only exists because of Forge's BrainBuilder hook
	default <BO extends LivingEntity & SmartBrainOwner<BO>> SmartBrain<BO> makeBrain(Collection<MemoryModuleType<?>> memories, Collection<? extends ExtendedSensor<BO>> sensors,
	                                                                                 List<ActivityBuilder<BO>> activities, @Nullable SmartBrainSchedule<BO, ?> schedule,
	                                                                                 RandomSource random) {
		return new SmartBrain<>(memories, sensors, activities, schedule, random);
	}

	/// Return the multipart entities loaded in the provided level
	///
	/// By default, Minecraft only has multipart entities on the [EnderDragon], but `Forge` and `NeoForge` both offer extensible implementations that
	/// should be accounted for
	Pair<Collection<? extends Entity>, Function<Entity, ? extends Entity>> getPartEntities(Level level);

	/// Return the array of part entities belonging to this entity, if any
	Entity[] getPartEntities(Entity entity);

	/// Register a custom [MemoryModuleType] under the `SmartBrainLib` namespace
	///
	/// You should <u>**NOT**</u> be using this
	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @ApiStatus.Internal
	<T> Supplier<MemoryModuleType<T>> registerMemoryType(String id, Optional<Codec<T>> codec);

	/// Register a custom [SensorType] under the `SmartBrainLib` namespace
	///
	/// You should <u>**NOT**</u> be using this
	@ApiStatus.Internal
	<T extends ExtendedSensor<?>> Supplier<SensorType<T>> registerSensorType(String id, Supplier<T> sensor);
}