package net.tslat.smartbrainlib.api.core.behaviour.custom.misc;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.mojang.datafixers.util.Pair;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.item.UseAction;
import net.minecraft.util.Hand;
import net.minecraft.world.server.ServerWorld;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;

/**
 * Makes the entity use (block) using a shield if it's currently in the entity's hands
 */
public class BlockWithShield<E extends LivingEntity> extends ExtendedBehaviour<E> {
	protected Hand hand = Hand.MAIN_HAND;

	protected Predicate<E> stopCondition = entity -> false;

	/**
	 * Sets the condition for when the entity should stop blocking. <br>
	 * @param predicate The predicate
	 * @return this
	 */
	public BlockWithShield<E> stopWhen(Predicate<E> predicate) {
		this.stopCondition = predicate;

		return this;
	}

	@Override
	protected List<Pair<MemoryModuleType<?>, MemoryModuleStatus>> getMemoryRequirements() {
		return new ArrayList<>();
	}

	@Override
	protected boolean checkExtraStartConditions(ServerWorld level, E entity) {
		if (entity.getMainHandItem().getUseAnimation() == UseAction.BLOCK) {
			this.hand = Hand.MAIN_HAND;

			return true;
		}
		else if (entity.getOffhandItem().getUseAnimation() == UseAction.BLOCK) {
			this.hand = Hand.OFF_HAND;

			return true;
		}

		return false;
	}

	@Override
	protected void start(E entity) {
		entity.startUsingItem(this.hand);
	}

	@Override
	protected boolean canStillUse(ServerWorld level, E entity, long gameTime) {
		if (!entity.isUsingItem())
			return false;

		if (entity.getUseItem().getUseAnimation() != UseAction.BLOCK)
			return false;

		return !this.stopCondition.test(entity);
	}

	@Override
	protected void stop(E entity) {
		if (entity.getUseItem().getUseAnimation() == UseAction.BLOCK)
			entity.stopUsingItem();
	}
}
