package net.tslat.smartbrainlib.api.core.navigation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidType;

/**
 * An extension of {@link SmoothGroundNavigation} to allow for fluid-agnostic pathfinding based on (Neo)Forge's fluid API overhaul
 * <p>
 * This allows for entities to pathfind in fluids other than water as necessary
 */
public class MultiFluidSmoothGroundNavigation extends SmoothGroundNavigation {
    public MultiFluidSmoothGroundNavigation(Mob mob, Level level) {
        super(mob, level);
    }

    /**
     * Whether the navigator should consider the entity's current state valid for navigating through a path
     * <p>
     * Note that this does not specifically apply to any given path (and the entity's path may even be null at times when this is called)
     */
    @Override
    protected boolean canUpdatePath() {
        return this.mob.onGround() || this.mob.isInFluidType((fluidType, height) -> canSwimInFluid(fluidType), true) || this.mob.isPassenger();
    }

    /**
     * Helper override to allow end-users to modify the fluids an entity can swim in, extensibly patching in (Neo)Forge's fluid API
     * <p>
     * Don't use this method to adjust which fluids are 'swimmable', use {@link #canSwimInFluid(FluidType)}
     *
     * @return The nearest safe surface height for the entity
     */
    @Override
    public int getSurfaceY() {
        if (this.mob.isInFluidType((fluidType, height) -> canSwimInFluid(fluidType), true) && canFloat()) {
            final int basePos = this.mob.getBlockY();
            BlockPos.MutableBlockPos pos = BlockPos.containing(this.mob.getX(), basePos, this.mob.getZ()).mutable();
            BlockState state = this.level.getBlockState(pos);

            while (canSwimInFluid(state.getFluidState().getFluidType())) {
                state = this.level.getBlockState(pos.move(Direction.UP));

                if (pos.getY() - basePos > 16)
                    return basePos;
            }

            return pos.getY();
        }

        return Mth.floor(this.mob.getY() + 0.5);
    }

    /**
     * Determine whether a given fluidType is one that the the entity this navigator represents can actively path through
     */
    protected boolean canSwimInFluid(FluidType fluidType) {
        return fluidType.canSwim(this.mob);
    }
}
