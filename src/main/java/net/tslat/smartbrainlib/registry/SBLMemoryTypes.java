package net.tslat.smartbrainlib.registry;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.Unit;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.tslat.smartbrainlib.SmartBrainLib;
import net.tslat.smartbrainlib.object.NearestVisibleLivingEntities;

/**
 * Registry class for custom {@link net.minecraft.world.entity.ai.memory.MemoryModuleType Memory Types}
 */
public final class SBLMemoryTypes {
	public static final DeferredRegister<MemoryModuleType<?>> MEMORY_TYPES = DeferredRegister.create(ForgeRegistries.MEMORY_MODULE_TYPES, SmartBrainLib.MOD_ID);

	public static final RegistryObject<MemoryModuleType<List<ProjectileEntity>>> INCOMING_PROJECTILES = register("incoming_projectiles");
	public static final RegistryObject<MemoryModuleType<Boolean>> TARGET_UNREACHABLE = register("target_unreachable");
	
	//Not present in 1.16 vanilla
	public static final RegistryObject<MemoryModuleType<LivingEntity>> NEAREST_ATTACKABLE = register("nearest_attackable");
	public static final RegistryObject<MemoryModuleType<NearestVisibleLivingEntities>> NEAREST_VISIBLE_LIVING_ENTITIES = register("nearest_visible_living_entities");
	public static final RegistryObject<MemoryModuleType<Unit>> IS_IN_WATER = register("is_in_water");
	public static final RegistryObject<MemoryModuleType<PlayerEntity>> TEMPTING_PLAYER = register("tempting_player");

	private static <T> RegistryObject<MemoryModuleType<T>> register(String id) {
		return register(id, null);
	}

	private static <T> RegistryObject<MemoryModuleType<T>> register(String id, @Nullable Codec<T> codec) {
		return MEMORY_TYPES.register(id, () -> new MemoryModuleType<T>(Optional.ofNullable(codec)));
	}
}
