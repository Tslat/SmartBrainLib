package net.tslat.smartbrainlib.registry;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.block.state.BlockState;
import net.tslat.smartbrainlib.SBLConstants;
import net.tslat.smartbrainlib.object.backport.NearestVisibleLivingEntities;

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

	// Backported from modern MC
	public static final Supplier<MemoryModuleType<NearestVisibleLivingEntities>> NEAREST_VISIBLE_LIVING_ENTITIES = register("visible_mobs");
	public static final Supplier<MemoryModuleType<List<LivingEntity>>> NEAREST_LIVING_ENTITIES = register("mobs");
	public static final Supplier<MemoryModuleType<Player>> TEMPTING_PLAYER = register("tempting_player");
	public static final Supplier<MemoryModuleType<Player>> NEAREST_VISIBLE_ATTACKABLE_PLAYER = register("nearest_visible_targetable_player");
	public static final Supplier<MemoryModuleType<LivingEntity>> NEAREST_ATTACKABLE = register("nearest_attackable");

	private static <T> Supplier<MemoryModuleType<T>> register(String id) {
		return SBLConstants.SBL_LOADER.registerMemoryType(id);
	}
}
