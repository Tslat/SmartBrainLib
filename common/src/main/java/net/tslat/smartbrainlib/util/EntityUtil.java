package net.tslat.smartbrainlib.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

/// Generic helper class for methods related to [Entity] functions
public final class EntityUtil {
	/// Perform a surface-level check on whether an entity is alive and attackable
	public static boolean isAliveAndAttackable(LivingEntity target) {
		return target.isAlive() && (!(target instanceof Player pl) || !pl.getAbilities().invulnerable);
	}
	
	/// @return Whether the two entities would normally be considered allies at the current time
	public static boolean areAllies(LivingEntity entity, LivingEntity ally) {
		if (!entity.getClass().isAssignableFrom(ally.getClass()))
			return false;
		
		if (BrainUtil.getTargetOfEntity(entity) == ally || BrainUtil.getTargetOfEntity(ally) == entity)
			return false;
		
		if (getPetOwner(entity) != getPetOwner(ally))
			return false;
		
		return BrainUtil.getLastAttacker(entity) != ally & BrainUtil.getLastAttacker(ally) != entity;
	}
	
	/// @return The owner entity of the provided potential pet, or null if not ownable or otherwise owned
	public static @Nullable LivingEntity getPetOwner(LivingEntity entity) {
		return entity instanceof OwnableEntity pet ? pet.getOwner() : null;
	}
}
