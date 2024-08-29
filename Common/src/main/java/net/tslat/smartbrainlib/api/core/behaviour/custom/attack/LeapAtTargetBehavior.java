package net.tslat.smartbrainlib.api.core.behaviour.custom.attack;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.function.Function;

/**
 * Recreation of the {@link net.minecraft.world.entity.ai.goal.LeapAtTargetGoal} for the brain system
 * @param <E>
 */
public class LeapAtTargetBehavior<E extends Mob> extends AnimatableMeleeAttack<E> {
    protected Function<E, Float> jumpHeightSupplier = entity -> 0.3F;
    protected Function<E, Float> jumpDistanceSupplier = entity -> 8F;

    public LeapAtTargetBehavior(int delayTicks) {
        super(delayTicks);
    }

    /**
     * Set the jump strength.
     * @param supplier The jump strength value provider
     * @return this
     */
    public LeapAtTargetBehavior<E> jumpHeight(Function<E, Float> supplier) {
        this.jumpHeightSupplier = supplier;

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

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        this.target = BrainUtils.getTargetOfEntity(entity);

        float dist = this.jumpDistanceSupplier.apply(entity);
        return entity.getSensing().hasLineOfSight(this.target) && entity.onGround() && entity.position().distanceToSqr(this.target.position()) <= dist * dist;
    }

    @Override
    protected void start(E entity) {
        super.start(entity);
        Vec3 vec3 = entity.getDeltaMovement();
        Vec3 vec32 = new Vec3(this.target.getX() - entity.getX(), 0.0, this.target.getZ() - entity.getZ());
        if (vec32.lengthSqr() > 1.0E-7) {
            vec32 = vec32.normalize().scale(0.4).add(vec3.scale(0.2));
        }
        entity.setDeltaMovement(vec32.x, this.jumpHeightSupplier.apply(entity), vec32.z);
    }
}
