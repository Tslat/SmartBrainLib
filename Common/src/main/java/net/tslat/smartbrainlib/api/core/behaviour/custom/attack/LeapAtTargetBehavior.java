package net.tslat.smartbrainlib.api.core.behaviour.custom.attack;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Recreation of the {@link net.minecraft.world.entity.ai.goal.LeapAtTargetGoal} for the brain system
 * @param <E>
 */
public class LeapAtTargetBehavior<E extends Mob> extends AnimatableMeleeAttack<E> {
    protected Function<E, Float> vertJumpStrengthSupplier = entity -> 0.3F;
    protected Function<E, Float> jumpDistanceSupplier = entity -> 8F;
    protected Function<E, Float> jumpStrengthSupplier = entity -> 0.4F;
    protected BiFunction<E, Vec3, Float> speedModifier = (entity, vec3) -> 0.2F;

    public LeapAtTargetBehavior(int delayTicks) {
        super(delayTicks);
    }

    /**
     * Sets the jump strength.
     * @param supplier The jump strength value provider
     * @return this
     */
    public LeapAtTargetBehavior<E> jumpStrength(Function<E, Float> supplier) {
        this.jumpStrengthSupplier = supplier;

        return this;
    }

    /**
     * Sets the vertical jump strength.
     * @param supplier The vertical jump strength value provider
     * @return this
     */
    public LeapAtTargetBehavior<E> verticalJumpStrength(Function<E, Float> supplier) {
        this.vertJumpStrengthSupplier = supplier;

        return this;
    }

    /**
     * Set the distance in blocks that the entity should be able to leap at targets.
     * @param supplier The distance value provider
     * @return this
     */
    public LeapAtTargetBehavior<E> jumpDistance(Function<E, Float> supplier) {
        this.jumpDistanceSupplier = supplier;

        return this;
    }

    /**
     * Set the jumpspeed modifier for when the entity is leaping
     * @param modifier The jumpspeed modifier/multiplier
     * @return this
     */
    public LeapAtTargetBehavior<E> speedModifier(float modifier) {
        return speedModifier((entity, targetPos) -> modifier);
    }

    /**
     * Set the jumpspeed modifier for when the entity is leaping
     * @param function The jumpspeed modifier/multiplier function
     * @return this
     */
    public LeapAtTargetBehavior<E> speedModifier(BiFunction<E, Vec3, Float> function) {
        this.speedModifier = function;

        return this;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        this.target = BrainUtils.getTargetOfEntity(entity);

        float dist = this.jumpDistanceSupplier.apply(entity);
        return entity.getSensing().hasLineOfSight(this.target) && entity.onGround() && entity.position().distanceToSqr(this.target.position()) <= dist * dist;
    }

    @Override
    protected void start(E entity) {
        super.start(entity);
        Vec3 speed = entity.getDeltaMovement();
        Vec3 targetVec = new Vec3(this.target.getX() - entity.getX(), 0.0, this.target.getZ() - entity.getZ());
        if (targetVec.lengthSqr() > 1.0E-7)
            targetVec = targetVec.normalize().scale(this.jumpStrengthSupplier.apply(entity)).add(speed.scale(this.speedModifier.apply(entity, this.target.position())));

        entity.setDeltaMovement(targetVec.x, this.vertJumpStrengthSupplier.apply(entity), targetVec.z);
    }
}
