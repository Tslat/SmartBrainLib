package net.tslat.smartbrainlib.registry;

import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.tslat.smartbrainlib.SmartBrainLib;

import java.util.List;
import java.util.Optional;

/**
 * Registry class for custom {@link net.minecraft.world.entity.ai.memory.MemoryModuleType Memory Types}
 */
public final class SBLMemoryTypes {
	public static final DeferredRegister<MemoryModuleType<?>> MEMORY_TYPES = DeferredRegister.create(ForgeRegistries.Keys.MEMORY_MODULE_TYPES, SmartBrainLib.MOD_ID);

	public static final RegistryObject<MemoryModuleType<List<Projectile>>> INCOMING_PROJECTILES = register("incoming_projectiles");

	private static <T> RegistryObject<MemoryModuleType<T>> register(String id) {
		return MEMORY_TYPES.register(id, () -> new MemoryModuleType<T>(Optional.empty()));
	}
}
