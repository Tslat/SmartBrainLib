package net.tslat.smartbrainlib.api.core.behaviour.custom.move;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;

/**
 * A movement behaviour for automatically following the owner of a {@link TamableAnimal TameableAnimal}.<br>
 * @param <E> The owner of the brain
 * @param <T> The minimum common class of the entity expected to be following
 */
public class FollowOwner<E extends TamableAnimal> extends FollowEntity<E, LivingEntity> {
	protected LivingEntity owner = null;

	public FollowOwner() {
		following(this::getOwner);
		teleportToTargetAfter(12);
	}

	protected LivingEntity getOwner(E entity) {
		if (this.owner == null)
			this.owner = entity.getOwner();

		if (this.owner != null && this.owner.isRemoved())
			this.owner = null;

		return this.owner;
	}
}
