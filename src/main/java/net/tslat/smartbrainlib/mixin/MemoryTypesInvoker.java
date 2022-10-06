package net.tslat.smartbrainlib.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.entity.ai.memory.MemoryModuleType;

@Mixin(MemoryModuleType.class)
public interface MemoryTypesInvoker {
	@Invoker
	static <U> MemoryModuleType<U> invokeRegister(String id) {
		throw new AssertionError();
	}
}