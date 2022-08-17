package net.tslat.smartbrainlib.registry;

import java.util.List;

import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.projectile.Projectile;
import net.tslat.smartbrainlib.mixin.MemoryTypesInvoker;

/**
 * Registry class for custom
 * {@link net.minecraft.world.entity.ai.memory.MemoryModuleType Memory Types}
 */
public final class SBLMemoryTypes {

	public static final MemoryModuleType<List<Projectile>> INCOMING_PROJECTILES = MemoryTypesInvoker
			.register("incoming_projectiles");

}
