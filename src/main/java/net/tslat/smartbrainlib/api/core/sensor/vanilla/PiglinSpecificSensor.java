package net.tslat.smartbrainlib.api.core.sensor.vanilla;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.util.BrainUtils;
import net.tslat.smartbrainlib.registry.SBLSensors;

import java.util.List;

/**
 * A replication of vanilla's
 * {@link net.minecraft.world.entity.ai.sensing.PiglinSpecificSensor}. Not
 * really useful, but included for completeness' sake and legibility. <br>
 * Handles most of Piglin's memories at once.
 * 
 * @param <E> The entity
 */
public class PiglinSpecificSensor<E extends LivingEntity> extends ExtendedSensor<E> {
	private static final List<MemoryModuleType<?>> MEMORIES = ObjectArrayList.of(MemoryModuleType.NEAREST_VISIBLE_NEMESIS, MemoryModuleType.NEAREST_VISIBLE_HUNTABLE_HOGLIN, MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN, MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD, MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM, MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS, MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT, MemoryModuleType.NEAREST_REPELLENT, MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryModuleType.NEARBY_ADULT_PIGLINS);

	@Override
	public List<MemoryModuleType<?>> memoriesUsed() {
		return MEMORIES;
	}

	@Override
	public SensorType<? extends ExtendedSensor<?>> type() {
		return SBLSensors.PIGLIN_SPECIFIC;
	}

	@Override
	protected void doTick(ServerLevel level, E entity) {
		Brain<?> brain = entity.getBrain();
		List<AbstractPiglin> adultPiglins = new ObjectArrayList<>();

		BrainUtils.withMemory(brain, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, entities -> {
			Mob nemesis = null;
			Hoglin nearestHuntableHoglin = null;
			Hoglin nearestBabyHoglin = null;
			LivingEntity nearestZombified = null;
			Player nearestPlayerWithoutGold = null;
			Player nearestPlayerWithWantedItem = null;
			List<AbstractPiglin> visibleAdultPiglins = new ObjectArrayList<>();
			int adultHoglinCount = 0;

			for (LivingEntity target : entities.findAll(obj -> true)) {
				if (target instanceof Hoglin hoglin) {
					if (hoglin.isBaby() && nearestBabyHoglin == null) {
						nearestBabyHoglin = hoglin;
					}
					else if (hoglin.isAdult()) {
						adultHoglinCount++;

						if (nearestHuntableHoglin == null && hoglin.canBeHunted())
							nearestHuntableHoglin = hoglin;
					}
				}
				else if (target instanceof PiglinBrute brute) {
					visibleAdultPiglins.add(brute);
				}
				else if (target instanceof Piglin piglin) {
					if (piglin.isAdult())
						visibleAdultPiglins.add(piglin);
				}
				else if (target instanceof Player player) {
					if (nearestPlayerWithoutGold == null && !PiglinAi.isWearingGold(player) && entity.canAttack(player))
						nearestPlayerWithoutGold = player;

					if (nearestPlayerWithWantedItem == null && !player.isSpectator() && PiglinAi.isPlayerHoldingLovedItem(player))
						nearestPlayerWithWantedItem = player;
				}
				else if (nemesis != null || !(target instanceof WitherSkeleton) && !(target instanceof WitherBoss)) {
					if (nearestZombified == null && PiglinAi.isZombified(target.getType()))
						nearestZombified = target;
				}
				else {
					nemesis = (Mob) target;
				}
			}

			BrainUtils.setMemory(brain, MemoryModuleType.NEAREST_VISIBLE_NEMESIS, nemesis);
			BrainUtils.setMemory(brain, MemoryModuleType.NEAREST_VISIBLE_HUNTABLE_HOGLIN, nearestHuntableHoglin);
			BrainUtils.setMemory(brain, MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN, nearestBabyHoglin);
			BrainUtils.setMemory(brain, MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, nearestZombified);
			BrainUtils.setMemory(brain, MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD, nearestPlayerWithoutGold);
			BrainUtils.setMemory(brain, MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM, nearestPlayerWithWantedItem);
			BrainUtils.setMemory(brain, MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS, visibleAdultPiglins);
			BrainUtils.setMemory(brain, MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, visibleAdultPiglins.size());
			BrainUtils.setMemory(brain, MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT, adultHoglinCount);
			BrainUtils.setMemory(brain, MemoryModuleType.NEAREST_REPELLENT,
					BlockPos.findClosestMatch(entity.blockPosition(), 8, 4, pos -> {
						BlockState state = level.getBlockState(pos);
						boolean isRepellent = state.is(BlockTags.PIGLIN_REPELLENTS);

						return isRepellent && state.is(Blocks.SOUL_CAMPFIRE) ? CampfireBlock.isLitCampfire(state) : isRepellent;
					}).orElse(null));
		});

		BrainUtils.withMemory(brain, MemoryModuleType.NEAREST_LIVING_ENTITIES, entities -> {
			for (LivingEntity target : entities) {
				if (target instanceof AbstractPiglin piglin && piglin.isAdult())
					adultPiglins.add(piglin);
			}
		});
		BrainUtils.setMemory(brain, MemoryModuleType.NEARBY_ADULT_PIGLINS, adultPiglins);
	}
}
