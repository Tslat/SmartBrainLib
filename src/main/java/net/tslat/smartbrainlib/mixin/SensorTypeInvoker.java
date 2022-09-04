package net.tslat.smartbrainlib.mixin;

import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;

@Mixin(SensorType.class)
public interface SensorTypeInvoker {
	@Invoker("register")
	static <U extends Sensor<?>> SensorType<U> register(String id, Supplier<U> factory) {
		throw new AssertionError();
	}
}