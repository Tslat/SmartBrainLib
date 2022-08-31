package net.tslat.smartbrainlib.registry;

import com.mojang.serialization.Codec;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.tslat.smartbrainlib.SmartBrainLib;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * Registry class for custom {@link net.minecraft.world.entity.ai.memory.MemoryModuleType Memory Types}
 */
public final class SBLMemoryTypes {
	public static final DeferredRegister<MemoryModuleType<?>> MEMORY_TYPES = DeferredRegister.create(ForgeRegistries.Keys.MEMORY_MODULE_TYPES, SmartBrainLib.MOD_ID);

	public static final RegistryObject<MemoryModuleType<List<Projectile>>> INCOMING_PROJECTILES = register("incoming_projectiles");
	public static final RegistryObject<MemoryModuleType<Boolean>> TARGET_UNREACHABLE = register("target_unreachable");
	public static final RegistryObject<MemoryModuleType<Boolean>> SPECIAL_ATTACK_COOLDOWN = register("special_attack_cooldown");

	private static <T> RegistryObject<MemoryModuleType<T>> register(String id) {
		return register(id, null);
	}

	private static <T> RegistryObject<MemoryModuleType<T>> register(String id, @Nullable Codec<T> codec) {
		return MEMORY_TYPES.register(id, () -> new MemoryModuleType<T>(Optional.ofNullable(codec)));
	}
}
