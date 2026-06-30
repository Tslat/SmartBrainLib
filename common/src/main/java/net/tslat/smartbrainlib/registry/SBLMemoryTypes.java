package net.tslat.smartbrainlib.registry;

import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.tslat.smartbrainlib.SBLConstants;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/// Registry class for custom [Memory Types][MemoryModuleType]
public final class SBLMemoryTypes {
	public static final Supplier<MemoryModuleType<List<Projectile>>> INCOMING_PROJECTILES = register("incoming_projectiles");
	public static final Supplier<MemoryModuleType<Boolean>> TARGET_UNREACHABLE = register("target_unreachable");
	public static final Supplier<MemoryModuleType<Boolean>> SPECIAL_ATTACK_COOLDOWN = register("special_attack_cooldown");
	public static final Supplier<MemoryModuleType<List<BlockInWorld>>> NEARBY_BLOCKS = register("nearby_blocks");
	public static final Supplier<MemoryModuleType<List<ItemEntity>>> NEARBY_ITEMS = register("nearby_items");

	//<editor-fold defaultstate="collapsed" desc="<Boilerplate>">
	public static void init() {}

	/// Register a basic memory type with no serialization
	///
	/// `SmartBrainLib` doesn't serialize memories anyway
	private static <T> Supplier<MemoryModuleType<T>> register(String id) {
		return SBLConstants.PLATFORM.registerMemoryType(id, Optional.empty());
	}
	//</editor-fold>
}
