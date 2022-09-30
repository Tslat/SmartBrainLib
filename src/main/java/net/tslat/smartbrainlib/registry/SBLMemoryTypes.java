package net.tslat.smartbrainlib.registry;

import java.util.List;

import com.mojang.datafixers.util.Pair;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.block.state.BlockState;
import net.tslat.smartbrainlib.SmartBrainLib;
import net.tslat.smartbrainlib.mixin.MemoryTypesInvoker;

/**
 * Registry class for custom
 * {@link net.minecraft.world.entity.ai.memory.MemoryModuleType Memory Types}
 */
public final class SBLMemoryTypes {

	public static final MemoryModuleType<List<Projectile>> INCOMING_PROJECTILES = MemoryTypesInvoker.invokeRegister(SmartBrainLib.MOD_ID + ":incoming_projectiles");
	public static final MemoryModuleType<Boolean> TARGET_UNREACHABLE = MemoryTypesInvoker.invokeRegister(SmartBrainLib.MOD_ID + ":target_unreachable");
	public static final MemoryModuleType<Boolean> SPECIAL_ATTACK_COOLDOWN = MemoryTypesInvoker.invokeRegister(SmartBrainLib.MOD_ID + ":special_attack_cooldown");
	public static final MemoryModuleType<List<Pair<BlockPos, BlockState>>> NEARBY_BLOCKS = MemoryTypesInvoker.invokeRegister(SmartBrainLib.MOD_ID + ":nearby_blocks");

	public static void init(){}
}
