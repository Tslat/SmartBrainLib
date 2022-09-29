package net.tslat.smartbrainlib.api.core.behaviour.custom.misc;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;

import com.mojang.datafixers.util.Pair;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;

/**
 * Equips the entity with an item in its {@link net.minecraft.world.InteractionHand hand}. <br>
 * Can be set to an {@link ItemStack#EMPTY empty ItemStack} to act as unequipping. <br>
 * Defaults:
 * <ul>
 *     <li>Equips to the main hand</li>
 *     <li>Deletes the item it was holding prior to equipping the new item</li>
 * </ul>
 */
public class HoldItem<E extends LivingEntity> extends ExtendedBehaviour<E> {
	protected Function<E, ItemStack> stackFunction = entity -> ItemStack.EMPTY;
	protected Function<E, Hand> handDecider = entity -> Hand.MAIN_HAND;
	protected BiPredicate<E, ItemStack> dropItemOnUnequip = (entity, stack) -> false;

	/**
	 * Sets the function to determine which hand to equip the item in.
	 * @param function The function
	 * @return this
	 */
	public HoldItem<E> toHand(Function<E, Hand> function) {
		this.handDecider = function;

		return this;
	}

	/**
	 * Sets the function to determine the item to equip.
	 * @param function The itemstack function
	 * @return this
	 */
	public HoldItem<E> withStack(Function<E, ItemStack> function) {
		this.stackFunction = function;

		return this;
	}

	/**
	 * Sets the behaviour to drop the previously equipped item when equipping the new item.
	 * @return this
	 */
	public HoldItem<E> dropItemOnUnequip() {
		return dropItemOnUnequip((entity, stack) -> true);
	}

	/**
	 * Sets the predicate to determine whether the entity should drop the previously equipped item when equipping the new item.
	 * @param dropPredicate The predicate
	 * @return this
	 */
	public HoldItem<E> dropItemOnUnequip(BiPredicate<E, ItemStack> dropPredicate) {
		this.dropItemOnUnequip = dropPredicate;

		return this;
	}

	@Override
	protected List<Pair<MemoryModuleType<?>, MemoryModuleStatus>> getMemoryRequirements() {
		return new ArrayList<>();
	}

	@Override
	protected void start(E entity) {
		Hand hand = this.handDecider.apply(entity);
		ItemStack previousStack = entity.getItemInHand(hand);

		if (this.dropItemOnUnequip.test(entity, previousStack))
			entity.spawnAtLocation(previousStack);

		entity.setItemInHand(hand, this.stackFunction.apply(entity));
	}
}
