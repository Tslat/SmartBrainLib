package net.tslat.smartbrainlib.api.core.behaviour.custom.path;

import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.object.MemoryTest;
import net.tslat.smartbrainlib.object.SquareRadius;
import net.tslat.smartbrainlib.object.ToFloatBiFunction;
import net.tslat.smartbrainlib.util.BrainUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiPredicate;

/**
 * Set a random position to swim to. <br>
 * Defaults:
 * <ul>
 *     <li>1x movespeed modifier</li>
 *     <li>10-block lateral radius</li>
 *     <li>10-block vertical radius</li>
 * </ul>
 */
public class SetRandomSwimTarget<E extends PathfinderMob> extends ExtendedBehaviour<E> {
    private static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(1).noMemory(MemoryModuleType.WALK_TARGET);

    protected ToFloatBiFunction<E, Vec3> speedModifier = (entity, targetPos) -> 1f;
    protected SquareRadius radius = new SquareRadius(10, 7);
    protected BiPredicate<E, Vec3> positionPredicate = (entity, pos) -> true;

    /**
     * Set the radius in which to look for swim positions.
     * @param radius The coordinate radius, in blocks
     * @return this
     */
    public SetRandomSwimTarget<E> setRadius(double radius) {
        return setRadius(radius, radius);
    }

    /**
     * Set the radius in which to look for swim positions.
     * @param xz The X/Z coordinate radius, in blocks
     * @param y The Y coordinate radius, in blocks
     * @return this
     */
    public SetRandomSwimTarget<E> setRadius(double xz, double y) {
        this.radius = new SquareRadius(xz, y);

        return this;
    }

    /**
     * Set the movespeed modifier for the path when chosen.
     * @param modifier The movespeed modifier/multiplier
     * @return this
     */
    public SetRandomSwimTarget<E> speedModifier(float modifier) {
        return speedModifier((entity, targetPos) -> modifier);
    }

    /**
     * Set the movespeed modifier for the path when chosen.
     * @param function The movespeed modifier/multiplier function
     * @return this
     */
    public SetRandomSwimTarget<E> speedModifier(ToFloatBiFunction<E, Vec3> function) {
        this.speedModifier = function;

        return this;
    }

    /**
     * Sets a predicate to check whether the target movement position is valid or not
     * @param predicate The predicate
     * @return this
     */
    public SetRandomSwimTarget<E> swimTargetPredicate(BiPredicate<E, Vec3> predicate) {
        this.positionPredicate = predicate;

        return this;
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    @Override
    protected void start(E entity) {
        Vec3 targetPos = getTargetPos(entity);

        if (!this.positionPredicate.test(entity, targetPos))
            targetPos = null;

        if (targetPos == null) {
            BrainUtil.clearMemory(entity, MemoryModuleType.WALK_TARGET);
        }
        else {
            BrainUtil.setMemory(entity, MemoryModuleType.WALK_TARGET, new WalkTarget(targetPos, this.speedModifier.applyAsFloat(entity, targetPos), 0));
        }
    }

    @Nullable
    protected Vec3 getTargetPos(E entity) {
        return BehaviorUtils.getRandomSwimmablePos(entity, (int)this.radius.xzRadius(), (int)this.radius.yRadius());
    }
}