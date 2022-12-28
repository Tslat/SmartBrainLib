package net.tslat.smartbrainlib.registry;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.block.state.BlockState;
import net.tslat.smartbrainlib.SBLConstants;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

/**
 * Registry class for custom {@link MemoryModuleType Memory Types}
 */
public final class SBLMemoryTypes {
	public static void init() {}

	public static final Supplier<MemoryModuleType<List<Projectile>>> INCOMING_PROJECTILES = register("incoming_projectiles");
	public static final Supplier<MemoryModuleType<Boolean>> TARGET_UNREACHABLE = register("target_unreachable");
	public static final Supplier<MemoryModuleType<Boolean>> SPECIAL_ATTACK_COOLDOWN = register("special_attack_cooldown");
	public static final Supplier<MemoryModuleType<List<Pair<BlockPos, BlockState>>>> NEARBY_BLOCKS = register("nearby_blocks");
	public static final Supplier<MemoryModuleType<Unit>> IS_IN_WATER = register("is_in_water");

	private static <T> Supplier<MemoryModuleType<T>> register(String id) {
		return register(id, null);
	}

	private static <T> Supplier<MemoryModuleType<T>> register(String id, @Nullable Codec<T> codec) {
		return SBLConstants.SBL_LOADER.registerMemoryType(id, codec);
	}
}
