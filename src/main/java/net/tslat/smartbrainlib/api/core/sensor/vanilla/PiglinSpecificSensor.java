package net.tslat.smartbrainlib.api.core.sensor.vanilla;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.monster.HoglinEntity;
import net.minecraft.entity.monster.WitherSkeletonEntity;
import net.minecraft.entity.monster.piglin.AbstractPiglinEntity;
import net.minecraft.entity.monster.piglin.PiglinBruteEntity;
import net.minecraft.entity.monster.piglin.PiglinEntity;
import net.minecraft.entity.monster.piglin.PiglinTasks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.util.BrainUtils;
import net.tslat.smartbrainlib.registry.SBLMemoryTypes;
import net.tslat.smartbrainlib.registry.SBLSensors;

import java.util.List;

/**
 * A replication of vanilla's {@link net.minecraft.world.entity.ai.sensing.PiglinSpecificSensor}. Not really useful, but included for completeness' sake and legibility. <br>
 * Handles most of Piglin's memories at once.
 * @param <E> The entity
 */
public class PiglinSpecificSensor<E extends LivingEntity> extends ExtendedSensor<E> {
	private static final List<MemoryModuleType<?>> MEMORIES = ObjectArrayList.wrap(new MemoryModuleType[] {MemoryModuleType.NEAREST_VISIBLE_NEMESIS, MemoryModuleType.NEAREST_VISIBLE_HUNTABLE_HOGLIN, MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN, MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD, MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM, MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS, MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT, MemoryModuleType.NEAREST_REPELLENT, MemoryModuleType.LIVING_ENTITIES, MemoryModuleType.NEARBY_ADULT_PIGLINS});

	@Override
	public List<MemoryModuleType<?>> memoriesUsed() {
		return MEMORIES;
	}

	@Override
	public SensorType<? extends ExtendedSensor<?>> type() {
		return SBLSensors.PIGLIN_SPECIFIC.get();
	}

	@Override
	protected void doTick(ServerWorld level, E entity) {
		Brain<?> brain = entity.getBrain();
		List<AbstractPiglinEntity> adultPiglins = new ObjectArrayList<>();

		BrainUtils.withMemory(brain, SBLMemoryTypes.NEAREST_VISIBLE_LIVING_ENTITIES.get(), entities -> {
			MobEntity nemesis = null;
			HoglinEntity nearestHuntableHoglin = null;
			HoglinEntity nearestBabyHoglin = null;
			LivingEntity nearestZombified = null;
			PlayerEntity nearestPlayerWithoutGold = null;
			PlayerEntity nearestPlayerWithWantedItem = null;
			List<AbstractPiglinEntity> visibleAdultPiglins = new ObjectArrayList<>();
			int adultHoglinCount = 0;

			for (LivingEntity target : entities.findAllMatchingEntries(obj -> true)) {
				if (target instanceof HoglinEntity) {
					HoglinEntity hoglin = (HoglinEntity)target;
					if (hoglin.isBaby() && nearestBabyHoglin == null) {
						nearestBabyHoglin = hoglin;
					}
					else if (hoglin.isAdult()) {
						adultHoglinCount++;

						if (nearestHuntableHoglin == null && hoglin.canBeHunted())
							nearestHuntableHoglin = hoglin;
					}
				}
				else if (target instanceof PiglinBruteEntity) {
					visibleAdultPiglins.add((PiglinBruteEntity)target);
				}
				else if (target instanceof PiglinEntity) {
					if (((PiglinEntity)target).isAdult())
						visibleAdultPiglins.add((PiglinEntity)target);
				}
				else if (target instanceof PlayerEntity) {
					if (nearestPlayerWithoutGold == null && !PiglinTasks.isWearingGold(target) && entity.canAttack(target))
						nearestPlayerWithoutGold = (PlayerEntity) target;

					if (nearestPlayerWithWantedItem == null && !target.isSpectator() && PiglinTasks.isPlayerHoldingLovedItem(target))
						nearestPlayerWithWantedItem = (PlayerEntity) target;
				}
				else if (nemesis != null || !(target instanceof WitherSkeletonEntity) && !(target instanceof WitherEntity)) {
					if (nearestZombified == null && PiglinTasks	.isZombified(target.getType()))
						nearestZombified = target;
				}
				else {
					nemesis = (MobEntity)target;
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
			BrainUtils.setMemory(brain, MemoryModuleType.NEAREST_REPELLENT, BlockPos.findClosestMatch(entity.blockPosition(), 8, 4, pos -> {
				BlockState state = level.getBlockState(pos);
				boolean isRepellent = state.is(BlockTags.PIGLIN_REPELLENTS);

				return isRepellent && state.is(Blocks.SOUL_CAMPFIRE) ? CampfireBlock.isLitCampfire(state) : isRepellent;
			}).orElse(null));
		});

		BrainUtils.withMemory(brain, MemoryModuleType.LIVING_ENTITIES, entities -> {
			for (LivingEntity target : entities) {
				if (target instanceof AbstractPiglinEntity && ((AbstractPiglinEntity)target).isAdult())
					adultPiglins.add((AbstractPiglinEntity) target);
			}
		});

		BrainUtils.setMemory(brain, MemoryModuleType.NEARBY_ADULT_PIGLINS, adultPiglins);
	}
}
