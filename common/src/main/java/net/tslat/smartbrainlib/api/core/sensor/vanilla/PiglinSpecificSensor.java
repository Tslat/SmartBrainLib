package net.tslat.smartbrainlib.api.core.sensor.vanilla;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.registry.SBLSensors;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/// A replication of vanilla's [net.minecraft.world.entity.ai.sensing.PiglinSpecificSensor]<br/>
/// Not really useful, but included for completeness' sake and legibility
///
/// Handles most of the [Piglin]'s memories at once
///
/// @param <BO> The brain owner entity
public class PiglinSpecificSensor<BO extends LivingEntity> extends ExtendedSensor<BO> {
	protected static final List<MemoryModuleType<?>> MEMORIES = ObjectArrayList.of(MemoryModuleType.NEAREST_VISIBLE_NEMESIS, MemoryModuleType.NEAREST_VISIBLE_HUNTABLE_HOGLIN,
																				   MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN, MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED,
																				   MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD, MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM,
																				   MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS, MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT,
																				   MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT, MemoryModuleType.NEAREST_REPELLENT,
																				   MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryModuleType.NEARBY_ADULT_PIGLINS);

	protected BiPredicate<BO, Mob> nemesisPredicate = (_, mob) -> mob instanceof WitherSkeleton || mob instanceof WitherBoss;
	protected BiPredicate<BO, Hoglin> babyHoglinPredicate = (_, hoglin) -> hoglin.isBaby();
	protected BiPredicate<BO, Hoglin> adultHoglinPredicate = (_, hoglin) -> hoglin.isAdult();
	protected BiPredicate<BO, Hoglin> huntableHoglinPredicate = (_, hoglin) -> hoglin.isAdult() && hoglin.canBeHunted();
	protected BiPredicate<BO, Piglin> babyPiglinPredicate = (_, piglin) -> piglin.isBaby();
	protected BiPredicate<BO, LivingEntity> adultPiglinPredicate = (_, target) -> target instanceof PiglinBrute || (target instanceof Piglin piglin && piglin.isAdult());
	protected BiPredicate<BO, LivingEntity> zombifiedPiglinPredicate = (_, target) -> target instanceof ZombifiedPiglin || target instanceof Zoglin;
	protected BiPredicate<BO, Player> playerNotWearingGoldPredicate = (entity, player) -> entity.canAttack(player) && !PiglinAi.isWearingSafeArmor(player);
	protected BiPredicate<BO, Player> playerHoldingWantedItemPredicate = (entity, player) -> !player.isSpectator() && PiglinAi.isPlayerHoldingLovedItem(player);
	protected BiPredicate<BO, BlockState> isRepellent = (_, state) -> state.is(BlockTags.PIGLIN_REPELLENTS) && (!state.is(Blocks.SOUL_CAMPFIRE) || CampfireBlock.isLitCampfire(state));

	/// Set a custom predicate for determining valid "nemesis" entities
	public PiglinSpecificSensor<BO> setNemesisPredicate(BiPredicate<BO, Mob> predicate) {
		this.nemesisPredicate = predicate;

		return this;
	}

	/// Set a custom predicate for determining valid [Hoglin]s for the [MemoryModuleType#NEAREST_VISIBLE_BABY_HOGLIN] memory
	public PiglinSpecificSensor<BO> setBabyHoglinPredicate(BiPredicate<BO, Hoglin> predicate) {
		this.babyHoglinPredicate = predicate;

		return this;
	}

	/// Set a custom predicate for determining valid [Hoglin]s for the [MemoryModuleType#VISIBLE_ADULT_HOGLIN_COUNT] memory
	public PiglinSpecificSensor<BO> setAdultHoglinPredicate(BiPredicate<BO, Hoglin> predicate) {
		this.adultHoglinPredicate = predicate;

		return this;
	}

	/// Set a custom predicate for determining valid [Hoglin]s for the [MemoryModuleType#NEAREST_VISIBLE_HUNTABLE_HOGLIN] memory
	public PiglinSpecificSensor<BO> setHuntableHoglinPredicate(BiPredicate<BO, Hoglin> predicate) {
		this.huntableHoglinPredicate = predicate;

		return this;
	}

	/// Set a custom predicate for determining valid [Piglin]s for the [MemoryModuleType#NEAREST_VISIBLE_BABY_HOGLIN] memory
	public PiglinSpecificSensor<BO> setBabyPiglinPredicate(BiPredicate<BO, Piglin> predicate) {
		this.babyPiglinPredicate = predicate;

		return this;
	}

	/// Set a custom predicate for determining valid [Piglin]s for the [MemoryModuleType#NEARBY_ADULT_PIGLINS] memory
	public PiglinSpecificSensor<BO> setAdultPiglinPredicate(BiPredicate<BO, LivingEntity> predicate) {
		this.adultPiglinPredicate = predicate;

		return this;
	}

	/// Set a custom predicate for determining valid [LivingEntity]s for the [MemoryModuleType#NEAREST_VISIBLE_ZOMBIFIED] memory
	public PiglinSpecificSensor<BO> setZombifiedPiglinPredicate(BiPredicate<BO, LivingEntity> predicate) {
		this.zombifiedPiglinPredicate = predicate;

		return this;
	}

	/// Set a custom predicate for determining valid [Player]s for the [MemoryModuleType#NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD] memory
	public PiglinSpecificSensor<BO> setPlayerNotWearingGoldPredicate(BiPredicate<BO, Player> predicate) {
		this.playerNotWearingGoldPredicate = predicate;

		return this;
	}

	/// Set a custom predicate for determining valid [Player]s for the [MemoryModuleType#NEAREST_PLAYER_HOLDING_WANTED_ITEM] memory
	public PiglinSpecificSensor<BO> setPlayerHoldingWantedItemPredicate(BiPredicate<BO, Player> predicate) {
		this.playerHoldingWantedItemPredicate = predicate;

		return this;
	}

	/// Set a custom predicate for valid repellent blocks
	public PiglinSpecificSensor<BO> validRepellents(BiPredicate<BO, BlockState> predicate) {
		this.isRepellent = predicate;

		return this;
	}

	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// Set the scan rate for this sensor
	public PiglinSpecificSensor<BO> scanRate(int scanRate) {
		return scanRate(_ -> scanRate);
	}

	/// Set the scan rate provider for this sensor
	///
	/// The provider will be sampled every time the sensor does a scan
	@Override
	public PiglinSpecificSensor<BO> scanRate(ToIntFunction<BO> function) {
		return (PiglinSpecificSensor<BO>)super.scanRate(function);
	}

	/// Set a callback function for when the sensor completes a scan
	@Override
	public PiglinSpecificSensor<BO> afterScanning(Consumer<BO> callback) {
		return (PiglinSpecificSensor<BO>)super.afterScanning(callback);
	}

	/// Set a condition that must be met in order to perform a scan
	///
	/// Failing the predicate will skip that scan tick and will not try again until the next scan tick as defined by [#scanRate]
	@Override
	public PiglinSpecificSensor<BO> onlyScanIf(Predicate<BO> predicate) {
		return (PiglinSpecificSensor<BO>)super.onlyScanIf(predicate);
	}
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="<Internal Handling>">
	/// @return The [SensorType] of the sensor, used for reverse lookups.
	@Override
	public SensorType<? extends ExtendedSensor<?>> type() {
		return SBLSensors.PIGLIN_SPECIFIC.get();
	}

	/// The list of memory types this sensor saves to. This should contain any memory the sensor sets a value for in the brain<br/>
	/// Bonus points if it's a statically cached list
	///
	/// @return The list of memory types saves by this sensor
	@Override
	public List<MemoryModuleType<?>> memoriesUsed() {
		return MEMORIES;
	}

	/// Handle the Sensor's actual function here. Be wary of the performance implications of computation-heavy checks here
	///
	/// @param level The level the entity is in
	/// @param entity The owner of the brain
	@Override
	protected void doTick(ServerLevel level, BO entity) {
		final Brain<BO> brain = BrainUtil.getBrain(entity);
		final NearestVisibleLivingEntities entities = BrainUtil.memoryOrDefault(brain, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, NearestVisibleLivingEntities.empty());
		final List<AbstractPiglin> adultPiglins = new ObjectArrayList<>();
		final List<AbstractPiglin> visibleAdultPiglins = new ObjectArrayList<>();
		Mob nemesis = null;
		Hoglin nearestHuntableHoglin = null;
		Hoglin nearestBabyHoglin = null;
		Piglin nearestBabyPiglin = null;
		LivingEntity nearestZombified = null;
		Player nearestPlayerWithoutGold = null;
		Player nearestPlayerWithWantedItem = null;
		int adultHoglinCount = 0;

		for (LivingEntity target : entities.nearbyEntities()) {
			switch (target) {
				case Hoglin hoglin -> {
					if (nearestBabyHoglin == null && this.babyHoglinPredicate.test(entity, hoglin)) {
						nearestBabyHoglin = hoglin;
					}
					else if (this.adultHoglinPredicate.test(entity, hoglin)) {
						adultHoglinCount++;

						if (nearestHuntableHoglin == null && this.huntableHoglinPredicate.test(entity, hoglin))
							nearestHuntableHoglin = hoglin;
					}
				}
				case PiglinBrute piglinBrute -> {
					if (this.adultPiglinPredicate.test(entity, piglinBrute))
						visibleAdultPiglins.add(piglinBrute);
				}
				case Piglin piglin -> {
					if (nearestBabyPiglin == null && this.babyPiglinPredicate.test(entity, piglin)) {
						nearestBabyPiglin = piglin;
					}
					else if (this.adultPiglinPredicate.test(entity, piglin)) {
						visibleAdultPiglins.add(piglin);
					}
				}
				case Player player -> {
					if (nearestPlayerWithoutGold == null && this.playerNotWearingGoldPredicate.test(entity, player))
						nearestPlayerWithoutGold = player;

					if (nearestPlayerWithWantedItem == null && this.playerHoldingWantedItemPredicate.test(entity, player))
						nearestPlayerWithWantedItem = player;
				}
				default -> {
					if (nemesis == null && target instanceof Mob mob && this.nemesisPredicate.test(entity, mob)) {
						nemesis = mob;
					}
					else if (nearestZombified == null && this.zombifiedPiglinPredicate.test(entity, target)) {
						nearestZombified = target;
					}
				}
			}
		}

		BrainUtil.setOrClearMemory(brain, MemoryModuleType.NEAREST_REPELLENT, BlockPos.findClosestMatch(entity.blockPosition(), 8, 4, pos ->
				this.isRepellent.test(entity, level.getBlockState(pos))).orElse(null));
		BrainUtil.setOrClearMemory(brain, MemoryModuleType.NEAREST_VISIBLE_NEMESIS, nemesis);
		BrainUtil.setOrClearMemory(brain, MemoryModuleType.NEAREST_VISIBLE_HUNTABLE_HOGLIN, nearestHuntableHoglin);
		BrainUtil.setOrClearMemory(brain, MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN, nearestBabyHoglin);
		BrainUtil.setOrClearMemory(brain, MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, nearestZombified);
		BrainUtil.setOrClearMemory(brain, MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD, nearestPlayerWithoutGold);
		BrainUtil.setOrClearMemory(brain, MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM, nearestPlayerWithWantedItem);
		BrainUtil.setMemory(brain, MemoryModuleType.NEARBY_ADULT_PIGLINS, adultPiglins);
		BrainUtil.setMemory(brain, MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS, visibleAdultPiglins);
		BrainUtil.setMemory(brain, MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, visibleAdultPiglins.size());
		BrainUtil.setMemory(brain, MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT, adultHoglinCount);
	}
	//</editor-fold>
}
