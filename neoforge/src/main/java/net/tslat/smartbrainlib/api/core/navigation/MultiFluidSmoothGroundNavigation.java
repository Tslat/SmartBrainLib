package net.tslat.smartbrainlib.api.core.navigation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.tslat.smartbrainlib.api.core.navigation.nodeevaluator.MultiFluidWalkNodeEvaluator;

/**
 * An extension of {@link SmoothGroundNavigation} to allow for fluid-agnostic pathfinding based on (Neo)Forge's fluid API overhaul
 * <p>
 * This allows for entities to pathfind in fluids other than water as necessary
 *
 * @see MultiFluidNavigationElement
 */
public class MultiFluidSmoothGroundNavigation extends SmoothGroundNavigation implements MultiFluidNavigationElement {
    public MultiFluidSmoothGroundNavigation(Mob mob, Level level) {
        super(mob, level);
    }

    /**
     * Patch {@link Path#getEntityPosAtNode} to use a proper rounding check
     */
    @Override
    protected PathFinder createPathFinder(int maxVisitedNodes) {
        this.nodeEvaluator = new MultiFluidWalkNodeEvaluator();
        this.nodeEvaluator.setCanPassDoors(true);

        return createSmoothPathFinder(this.nodeEvaluator, maxVisitedNodes);
    }

    /**
     * Whether the navigator should consider the entity's current state valid for navigating through a path
     * <p>
     * Note that this does not specifically apply to any given path (and the entity's path may even be null at times when this is called)
     */
    @Override
    protected boolean canUpdatePath() {
        return this.mob.onGround() || this.mob.isInFluidType((fluidType, height) -> canSwimInFluid(this.mob, fluidType, height), true) || this.mob.isPassenger();
    }

    /**
     * Helper override to allow end-users to modify the fluids an entity can swim in, extensibly patching in (Neo)Forge's fluid API
     * <p>
     * Don't use this method to adjust which fluids are 'swimmable', use {@link MultiFluidNavigationElement#canSwimInFluid}
     *
     * @return The nearest safe surface height for the entity
     */
    @Override
    public int getSurfaceY() {
        if (this.mob.isInFluidType((fluidType, height) -> canSwimInFluid(this.mob, fluidType, height), true) && canFloat()) {
            final int basePos = this.mob.getBlockY();
            BlockPos.MutableBlockPos pos = BlockPos.containing(this.mob.getX(), basePos, this.mob.getZ()).mutable();
            BlockState state = this.level.getBlockState(pos);
            FluidState fluidState = state.getFluidState();

            while (canSwimInFluid(this.mob, fluidState.getFluidType(), fluidState.getHeight(this.level, pos))) {
                state = this.level.getBlockState(pos.move(Direction.UP));
                fluidState = state.getFluidState();

                if (pos.getY() - basePos > 16)
                    return basePos;
            }

            return pos.getY();
        }

        return Mth.floor(this.mob.getY() + 0.5);
    }
}
