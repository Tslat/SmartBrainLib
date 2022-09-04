package net.tslat.smartbrainlib.mixin;

import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;

@Mixin(SensorType.class)
public interface SensorTypeInvoker {
	@Invoker("<init>")
	static <U extends Sensor<?>>  SensorType createSensorType (Supplier<U> factory) {
		throw new UnsupportedOperationException();
	}
}