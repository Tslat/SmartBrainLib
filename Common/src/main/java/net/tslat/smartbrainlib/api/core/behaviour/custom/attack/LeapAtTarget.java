package net.tslat.smartbrainlib.api.core.behaviour.custom.attack;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * SmartBrainLib equivalent of {@link net.minecraft.world.entity.ai.goal.LeapAtTargetGoal}
 * <p>
 * Launches the entity at the current attack target and attacks after a pre-determined delay
 * <p>
 * Defaults:
 * <ul>
 *     <li>0.3f vertical velocity</li>
 *     <li>20% of the entity's existing velocity added to the jump</li>
 *     <li>0.4f jump strength</li>
 *     <li>Only leaps if on the ground, has line of sight, and is within 8 blocks</li>
 * </ul>
 *
 * @see AnimatableMeleeAttack
 */
public class LeapAtTarget<E extends Mob> extends AnimatableMeleeAttack<E> {
    protected BiFunction<E, LivingEntity, Float> verticalJumpStrength = (entity, target) -> 0.3f;
    protected BiFunction<E, LivingEntity, Float> jumpStrength = (entity, target) -> 0.4f;
    protected BiFunction<E, LivingEntity, Float> moveSpeedContribution = (entity, target) -> 0.2f;
    protected BiFunction<E, LivingEntity, Float> leapRange = (entity, target) -> 8f;
    @Deprecated(forRemoval = true)
    protected BiPredicate<E, LivingEntity> leapPredicate = (entity, target) -> true;

    public LeapAtTarget(int delayTicks) {
        super(delayTicks);
    }

    /**
     * Add a predicate that determines whether the entity can leap or not
     *
     * @deprecated use {@link #startCondition(Predicate)} and/or {@link #leapRange(BiFunction)} instead
     * @param predicate The predicate
     * @return this
     */
    @Deprecated(forRemoval = true)
    public LeapAtTarget<E> leapIf(BiPredicate<E, LivingEntity> predicate) {
        this.leapPredicate = predicate;

        return this;
    }

    /**
     * Determines how far away the entity can be and still leap
     *
     * @param range The maximum range at which the entity can jump at target
     * @return this
     */
    public LeapAtTarget<E> leapRange(BiFunction<E, LivingEntity, Float> range) {
        this.leapRange = range;

        return this;
    }

    /**
     * Set the jump strength for the leap, scaled by the distance to be jumped
     *
     * @param function The jump strength function
     * @return this
     */
    public LeapAtTarget<E> jumpStrength(BiFunction<E, LivingEntity, Float> function) {
        this.jumpStrength = function;

        return this;
    }

    /**
     * Set the additional vertical velocity added when leaping.<br>
     * This value is not normally scaled by the distance being lept
     *
     * @param function The vertical jump strength function
     * @return this
     */
    public LeapAtTarget<E> verticalJumpStrength(BiFunction<E, LivingEntity, Float> function) {
        this.verticalJumpStrength = function;

        return this;
    }

    /**
     * Set the amount that the entity's existing velocity contributes to the jump's velocity.<br>
     * The returned value here acts as a percentage of the entity's existing velocity.<br>
     * {@code 0.2f = 20% of the existing velocity added to the jump}
     *
     * @param function The velocity contribution function
     * @return this
     */
    public LeapAtTarget<E> moveSpeedContribution(BiFunction<E, LivingEntity, Float> function) {
        this.moveSpeedContribution = function;

        return this;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        this.target = BrainUtils.getTargetOfEntity(entity);

        return entity.onGround() && entity.getSensing().hasLineOfSight(this.target) && entity.closerThan(this.target, this.leapRange.apply(entity, this.target));
    }

    @Override
    protected void start(E entity) {
        super.start(entity);

        Vec3 velocity = new Vec3(this.target.getX() - entity.getX(), 0, this.target.getZ() - entity.getZ());

        if (velocity.lengthSqr() > 1.0E-7)
            velocity = velocity.normalize().scale(this.jumpStrength.apply(entity, this.target)).add(entity.getDeltaMovement().scale(this.moveSpeedContribution.apply(entity, this.target)));

        entity.setDeltaMovement(velocity.x, this.verticalJumpStrength.apply(entity, this.target), velocity.z);
    }
}
