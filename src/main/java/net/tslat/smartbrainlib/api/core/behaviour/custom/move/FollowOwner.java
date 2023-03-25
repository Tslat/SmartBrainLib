package net.tslat.smartbrainlib.api.core.behaviour.custom.move;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.TameableEntity;

/**
 * A movement behaviour for automatically following the owner of a {@link net.minecraft.world.entity.TamableAnimal TameableAnimal}.<br>
 * @param <E> The owner of the brain
 * @param <T> The minimum common class of the entity expected to be following
 */
public class FollowOwner<E extends TameableEntity> extends FollowEntity<E, LivingEntity> {
	protected LivingEntity owner = null;

	public FollowOwner() {
		following(this::getOwner);
		teleportToTargetAfter(12);
	}

	protected LivingEntity getOwner(E entity) {
		if (this.owner == null)
			this.owner = entity.getOwner();

		if (this.owner != null && !this.owner.isAddedToWorld())
			this.owner = null;

		return this.owner;
	}
}
