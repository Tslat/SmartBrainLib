package net.tslat.smartbrainlib.api.core.behaviour.custom.misc;

import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.ItemStack;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * Equips the entity with an item in its {@link InteractionHand hand}. <br>
 * Can be set to an {@link ItemStack#EMPTY empty ItemStack} to act as unequipping. <br>
 * Defaults:
 * <ul>
 *     <li>Equips to the main hand</li>
 *     <li>Deletes the item it was holding prior to equipping the new item</li>
 * </ul>
 */
public class HoldItem<E extends LivingEntity> extends ExtendedBehaviour<E> {
	protected Function<E, ItemStack> stackFunction = entity -> ItemStack.EMPTY;
	protected Function<E, InteractionHand> handDecider = entity -> InteractionHand.MAIN_HAND;
	protected BiPredicate<E, ItemStack> dropItemOnUnequip = (entity, stack) -> false;

	/**
	 * Sets the function to determine which hand to equip the item in.
	 * @param function The function
	 * @return this
	 */
	public HoldItem<E> toHand(Function<E, InteractionHand> function) {
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
	protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
		return List.of();
	}

	@Override
	protected void start(E entity) {
		InteractionHand hand = this.handDecider.apply(entity);
		ItemStack previousStack = entity.getItemInHand(hand);

		if (this.dropItemOnUnequip.test(entity, previousStack))
			entity.spawnAtLocation((ServerLevel)entity.level(), previousStack);

		entity.setItemInHand(hand, this.stackFunction.apply(entity));
	}
}