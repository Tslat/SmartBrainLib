package net.tslat.smartbrainlib.api.core.behaviour.custom.misc;

import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.Animal;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.object.MemoryTest;
import net.tslat.smartbrainlib.object.ToFloatBiFunction;
import net.tslat.smartbrainlib.util.BrainUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.ToIntBiFunction;

/**
 * Functional replacement for vanilla's {@link net.minecraft.world.entity.ai.behavior.AnimalMakeLove AnimalMakeLove}.
 * <p>Makes the entity find, move to, and breed with its target mate, producing offspring.</p>
 * Defaults:
 * <ul>
 *     <li>1x walk speed modifier when moving to its breeding partner</li>
 *     <li>Spend between 3 and 5.5 seconds to create the offspring</li>
 * </ul>
 */
public class BreedWithPartner<E extends Animal> extends ExtendedBehaviour<E> {
	private static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(4).hasMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).noMemory(MemoryModuleType.BREED_TARGET).usesMemories(MemoryModuleType.LOOK_TARGET, MemoryModuleType.WALK_TARGET);

	protected ToFloatBiFunction<E, Animal> speedMod = (entity, partner) -> 1f;
	protected ToIntBiFunction<E, Animal> closeEnoughDist = (entity, partner) -> 2;
	protected ToIntBiFunction<E, Animal> breedTime = (entity, partner) -> entity.getRandom().nextInt(60, 110);
	protected BiPredicate<E, Animal> partnerPredicate = (entity, partner) -> entity.getType() == partner.getType() && entity.canMate(partner);

	protected int childBreedTick = -1;
	protected Animal partner = null;

	public BreedWithPartner() {
		noTimeout();
	}

	/**
	 * Set the movespeed modifier for the entity when moving to their partner.
	 * @param speedModifier The movespeed modifier/multiplier
	 * @return this
	 */
	public BreedWithPartner<E> speedMod(final ToFloatBiFunction<E, Animal> speedModifier) {
		this.speedMod = speedModifier;

		return this;
	}

	/**
	 * Sets the amount (in blocks) that the animal can be considered 'close enough' to their partner that they can stop pathfinding
	 * @param closeEnoughDist The distance function
	 * @return this
	 */
	public BreedWithPartner<E> closeEnoughDist(final ToIntBiFunction<E, Animal> closeEnoughDist) {
		this.closeEnoughDist = closeEnoughDist;

		return this;
	}

	@Override
	protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
		return MEMORY_REQUIREMENTS;
	}

	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
		if (!entity.isInLove())
			return false;

		this.partner = findPartner(entity);

		return this.partner != null;
	}

	@Override
	protected boolean shouldKeepRunning(E entity) {
		return this.partner != null && this.partner.isAlive() && entity.tickCount <= this.childBreedTick && BehaviorUtils.entityIsVisible(entity.getBrain(), this.partner) && this.partnerPredicate.test(entity, this.partner);
	}

	@Override
	protected void start(E entity) {
		this.childBreedTick = entity.tickCount + this.breedTime.applyAsInt(entity, this.partner);

		BrainUtil.setMemory(entity, MemoryModuleType.BREED_TARGET, this.partner);
		BrainUtil.setMemory(this.partner, MemoryModuleType.BREED_TARGET, entity);
		BehaviorUtils.lockGazeAndWalkToEachOther(entity, this.partner, this.speedMod.applyAsFloat(entity, this.partner), this.closeEnoughDist.applyAsInt(entity, this.partner));
	}

	@Override
	protected void tick(E entity) {
		BehaviorUtils.lockGazeAndWalkToEachOther(entity, this.partner, this.speedMod.applyAsFloat(entity, this.partner), this.closeEnoughDist.applyAsInt(entity, this.partner));

		if (entity.closerThan(this.partner, 3) && entity.tickCount == this.childBreedTick) {
			entity.spawnChildFromBreeding((ServerLevel)entity.level(), this.partner);
			BrainUtil.clearMemory(entity, MemoryModuleType.BREED_TARGET);
			BrainUtil.clearMemory(this.partner, MemoryModuleType.BREED_TARGET);
		}
	}

	@Override
	protected void stop(E entity) {
		BrainUtil.clearMemories(entity, MemoryModuleType.BREED_TARGET, MemoryModuleType.LOOK_TARGET, MemoryModuleType.WALK_TARGET);

		if (this.partner != null)
			BrainUtil.clearMemories(this.partner, MemoryModuleType.BREED_TARGET, MemoryModuleType.LOOK_TARGET, MemoryModuleType.WALK_TARGET);

		this.childBreedTick = -1;
		this.partner = null;
	}

	@Nullable
	protected Animal findPartner(E entity) {
		return BrainUtil.getMemory(entity, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).findClosest(entity2 -> entity2 instanceof Animal partner && this.partnerPredicate.test(entity, partner)).map(Animal.class::cast).orElse(null);
	}
}
