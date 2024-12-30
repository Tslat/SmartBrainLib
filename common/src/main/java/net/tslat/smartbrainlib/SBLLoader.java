package net.tslat.smartbrainlib;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.level.Level;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public interface SBLLoader {
	void init(Object eventBus);
	boolean isDevEnv();
	Pair<Collection<? extends Entity>, Function<Entity, ? extends Entity>> getPartEntities(Level level);

	@ApiStatus.Internal
	<T> Supplier<MemoryModuleType<T>> registerMemoryType(String id);
	@ApiStatus.Internal
	<T> Supplier<MemoryModuleType<T>> registerMemoryType(String id, Optional<Codec<T>> codec);
	@ApiStatus.Internal
	<T extends ExtendedSensor<?>> Supplier<SensorType<T>> registerSensorType(String id, Supplier<T> sensor);
}
