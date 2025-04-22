package net.tslat.smartbrainlib.api.core.behaviour.custom.move;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;

/**
 * A movement behaviour for automatically following the owner of a {@link TamableAnimal TameableAnimal}
 *
 * @param <E> The owner of the brain
 */
public class FollowOwner<E extends PathfinderMob & OwnableEntity> extends FollowEntity<E, LivingEntity> {
	public FollowOwner() {
		following(OwnableEntity::getOwner);
		teleportToTargetAfter(12);
	}
}