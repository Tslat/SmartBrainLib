package net.tslat.smartbrainlib.api.core.behaviour.custom.path;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.HoverRandomPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.function.ToIntFunction;

/**
 * Extension of {@link SetRandomHoverTarget}, with a configurable weight to allow for more 'flying'-like movement
 * <p>Additionally expands the vertical path search radius to 10, over the default of 7</p>
 */
public class SetRandomFlyingTarget<E extends PathfinderMob> extends SetRandomHoverTarget<E> {
    protected ToIntFunction<E> verticalWeight = entity -> -2;

    public SetRandomFlyingTarget() {
        setRadius(10, 10);
    }

    /**
     * Sets the function that determines a vertical position offset for target positions.<br>
     * Flight patterns will tend towards this direction, with bigger values pulling more strongly
     * @param function The function
     * @return this
     */
    public SetRandomFlyingTarget<E> verticalWeight(ToIntFunction<E> function) {
        this.verticalWeight = function;

        return this;
    }

    @Nullable
    @Override
    protected Vec3 getTargetPos(E entity) {
        Vec3 entityFacing = entity.getViewVector(0);
        Vec3 hoverPos = HoverRandomPos.getPos(entity, (int)(Math.ceil(this.radius.xzRadius())), (int)Math.ceil(this.radius.yRadius()), entityFacing.x, entityFacing.z, Mth.HALF_PI, 3, 1);

        if (hoverPos != null)
            return hoverPos;

        return AirAndWaterRandomPos.getPos(entity, (int)(Math.ceil(this.radius.xzRadius())), (int)Math.ceil(this.radius.yRadius()), this.verticalWeight.applyAsInt(entity), entityFacing.x, entityFacing.z, Mth.HALF_PI);
    }
}