package net.tslat.smartbrainlib.api.core.behaviour.custom.misc;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;
import net.tslat.smartbrainlib.library.object.MemoryTest;
import org.jetbrains.annotations.ApiStatus;

import java.util.Set;
import java.util.function.*;

/// Equips the entity with an item in its [hand][InteractionHand], or alternatively an [empty stack][ItemStack#EMPTY] to unequip
public class HoldItem<BO extends LivingEntity> extends ExtendedBehaviour<BO> {
	protected Function<BO, ItemStack> stackFunction = _ -> ItemStack.EMPTY;
	protected Function<BO, InteractionHand> handDecider = _ -> InteractionHand.MAIN_HAND;
	protected BiPredicate<BO, ItemStack> dropItemOnUnequip = (_, _) -> false;
	protected boolean forceReplacement = false;

	/// Set which hand to hold the item in
	@ApiStatus.NonExtendable
	public HoldItem<BO> inHand(InteractionHand hand) {
		return inHand(_ -> hand);
	}

	/// Set a function to determine which hand to hold the item in
	@ApiStatus.NonExtendable
	public HoldItem<BO> inHand(Function<BO, InteractionHand> function) {
		this.handDecider = function;
		
		return this;
	}
	
	/// Set the item to equip in the entity's hand
	///
	/// Passing [Items#AIR] will effectively 'unequip' the hand instead
	@ApiStatus.NonExtendable
	public HoldItem<BO> withItem(ItemLike item) {
		return withStack(_ -> new ItemStack(item.asItem()));
	}
	
	/// Set a function to provide the ItemStack to set in the entity's hand
	///
	/// Providing an [empty][ItemStack#EMPTY] stack will effectively 'unequip' the hand instead
	@ApiStatus.NonExtendable
	public HoldItem<BO> withStack(Function<BO, ItemStack> function) {
		this.stackFunction = function;

		return this;
	}

	/// Set the behaviour to drop the previously equipped item when equipping the new item
	@ApiStatus.NonExtendable
	public HoldItem<BO> dropItemOnUnequip() {
		return dropItemOnUnequip((_, _) -> true);
	}

	/// Set a predicate to determine whether the entity should drop the previously equipped item when equipping the new item
	@ApiStatus.NonExtendable
	public HoldItem<BO> dropItemOnUnequip(BiPredicate<BO, ItemStack> dropPredicate) {
		this.dropItemOnUnequip = dropPredicate;

		return this;
	}

	/// Set a predicate to determine
	@ApiStatus.NonExtendable
	public HoldItem<BO> dropEvenIfIdentical() {
		this.forceReplacement = true;

		return this;
	}
	
	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// Set a callback for when the behaviour successfully begins
	///
	/// This is called immediately prior to [ExtendedBehaviour#start(LivingEntity)]
	@ApiStatus.NonExtendable
	@Override
	public HoldItem<BO> whenStarting(Consumer<BO> callback) {
		return (HoldItem<BO>)super.whenStarting(callback);
	}
	
	/// Set a callback for when the behaviour stops
	///
	/// This is called immediately prior to [ExtendedBehaviour#stop(LivingEntity)]
	///
	/// Note that the behaviour stopping does not necessarily mean it was successful
	@ApiStatus.NonExtendable
	@Override
	public HoldItem<BO> whenStopping(Consumer<BO> callback) {
		return (HoldItem<BO>)super.whenStopping(callback);
	}
	
	/// Set the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public HoldItem<BO> runFor(int ticks) {
		return (HoldItem<BO>)super.runFor(ticks);
	}
	
	/// Set the range of ticks the behaviour should try to run for, once started<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is run
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public HoldItem<BO> runFor(int minTicks, int maxTicks) {
		return (HoldItem<BO>)super.runFor(minTicks, maxTicks);
	}
	
	/// Set a function to determine the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public HoldItem<BO> runFor(ToIntFunction<BO> timeProvider) {
		return (HoldItem<BO>)super.runFor(timeProvider);
	}
	
	/// Disable the tick-based timeout for this behaviour and instead rely exclusively on other conditions
	/// such as [#getMemoryRequirements()] failing or [#stopIf(Predicate)]
	@ApiStatus.NonExtendable
	@Override
	public HoldItem<BO> noTimeout() {
		return (HoldItem<BO>)super.noTimeout();
	}
	
	/// Set the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public HoldItem<BO> cooldownFor(int ticks) {
		return (HoldItem<BO>)super.cooldownFor(ticks);
	}
	
	/// Set the range of ticks that this behaviour should be prevented from starting again after it has finished<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is stopped
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public HoldItem<BO> cooldownFor(int minTicks, int maxTicks) {
		return (HoldItem<BO>)super.cooldownFor(minTicks, maxTicks);
	}
	
	/// Set a function to determine the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public HoldItem<BO> cooldownFor(ToIntFunction<BO> timeProvider) {
		return (HoldItem<BO>)super.cooldownFor(timeProvider);
	}
	
	/// Set an additional condition for the behaviour to be able to start
	///
	/// Prevents this behaviour starting unless this predicate returns true
	@ApiStatus.NonExtendable
	@Override
	public HoldItem<BO> startCondition(Predicate<BO> predicate) {
		return (HoldItem<BO>)super.startCondition(predicate);
	}
	
	/// Set a condition under which the behaviour should automatically stop<br/>
	/// Has no effect on one-shot behaviours that don't tick or have a runtime
	///
	/// Stops the behaviour immediately if the predicate returns true, ready to run again
	@ApiStatus.NonExtendable
	@Override
	public HoldItem<BO> stopIf(Predicate<BO> predicate) {
		return (HoldItem<BO>)super.stopIf(predicate);
	}
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="<Internal Handling>">
	/// The set of memory requirements this task has before starting<br/>
	/// This outlines the approximate state the brain should be in to allow this behaviour to run
	///
	/// Ideally, this would be a statically cached set
	///
	/// @return The [Set] of [Memories][MemoryModuleType] and their associated required [status][MemoryStatus]
	/// @see MemoryTest
	@Override
	public Set<MemoryCondition<?, ?>> getMemoryRequirements() {
		return Set.of();
	}
	
	/// Run the core functionality this behaviour has when starting<br/>
	/// This method is called once per behaviour run
	///
	/// By this stage any memory requirements set in [#getMemoryRequirements()] are true, so any memories paired with [MemoryStatus#VALUE_PRESENT] are safe to retrieve
	///
	/// If you are not making a custom behaviour and are instead just using an existing behaviour; you would use [#whenStarting(Consumer)] instead of overriding this method
	@Override
	protected void start(BO entity) {
		final InteractionHand hand = this.handDecider.apply(entity);
		final ItemStack previousStack = entity.getItemInHand(hand);
		final ItemStack newStack = this.stackFunction.apply(entity);
		
		if (!this.forceReplacement && ItemStack.isSameItemSameComponents(previousStack, newStack))
			return;
		
		if (this.dropItemOnUnequip.test(entity, previousStack))
			entity.spawnAtLocation((ServerLevel)entity.level(), previousStack.copy());

		entity.setItemInHand(hand, newStack.copy());
	}
	//</editor-fold>
}