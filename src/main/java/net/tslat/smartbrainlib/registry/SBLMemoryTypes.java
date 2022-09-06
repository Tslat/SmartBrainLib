package net.tslat.smartbrainlib.registry;

import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.projectile.Projectile;
import net.tslat.smartbrainlib.SmartBrainLib;
import net.tslat.smartbrainlib.mixin.MemoryTypesInvoker;

import java.util.List;

/**
 * Registry class for custom
 * {@link net.minecraft.world.entity.ai.memory.MemoryModuleType Memory Types}
 */
public final class SBLMemoryTypes {

	public static final MemoryModuleType<List<Projectile>> INCOMING_PROJECTILES = MemoryTypesInvoker.invokeRegister(SmartBrainLib.MOD_ID + ":incoming_projectiles");
	public static final MemoryModuleType<Boolean> TARGET_UNREACHABLE = MemoryTypesInvoker.invokeRegister(SmartBrainLib.MOD_ID + ":target_unreachable");
	public static final MemoryModuleType<Boolean> SPECIAL_ATTACK_COOLDOWN = MemoryTypesInvoker.invokeRegister(SmartBrainLib.MOD_ID + ":special_attack_cooldown");

	public static void init(){}
}
