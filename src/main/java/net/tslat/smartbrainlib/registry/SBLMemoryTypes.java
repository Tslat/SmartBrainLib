package net.tslat.smartbrainlib.registry;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;

import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.tslat.smartbrainlib.SmartBrainLib;

/**
 * Registry class for custom {@link net.minecraft.world.entity.ai.memory.MemoryModuleType Memory Types}
 */
public final class SBLMemoryTypes {
	public static final DeferredRegister<MemoryModuleType<?>> MEMORY_TYPES = DeferredRegister.create(ForgeRegistries.MEMORY_MODULE_TYPES, SmartBrainLib.MOD_ID);

	public static final RegistryObject<MemoryModuleType<List<ProjectileEntity>>> INCOMING_PROJECTILES = register("incoming_projectiles");
	public static final RegistryObject<MemoryModuleType<Boolean>> TARGET_UNREACHABLE = register("target_unreachable");

	private static <T> RegistryObject<MemoryModuleType<T>> register(String id) {
		return register(id, null);
	}

	private static <T> RegistryObject<MemoryModuleType<T>> register(String id, @Nullable Codec<T> codec) {
		return MEMORY_TYPES.register(id, () -> new MemoryModuleType<T>(Optional.ofNullable(codec)));
	}
}
