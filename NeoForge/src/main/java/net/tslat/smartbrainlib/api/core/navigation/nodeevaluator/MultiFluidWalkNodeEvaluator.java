package net.tslat.smartbrainlib.api.core.navigation.nodeevaluator;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.AABB;
import net.tslat.smartbrainlib.api.core.navigation.MultiFluidNavigationElement;

/**
 * An extension of {@link WalkNodeEvaluator} to allow for fluid-agnostic pathfinding based on (Neo)Forge's fluid API overhaul
 * <p>
 * This allows for entities to pathfind in fluids other than water as necessary
 *
 * @see MultiFluidNavigationElement
 */
public class MultiFluidWalkNodeEvaluator extends WalkNodeEvaluator implements MultiFluidNavigationElement {
    /**
     * Determine and create a path node for the current starting position based on the surrounding environment
     */
    @Override
    public Node getStart() {
        int groundY = this.mob.getBlockY();
        final BlockPos.MutableBlockPos testPos = new BlockPos.MutableBlockPos(this.mob.getX(), groundY, this.mob.getZ());
        BlockState groundState = this.currentContext.getBlockState(testPos);

        if (!this.mob.canStandOnFluid(groundState.getFluidState())) {
            if (canFloat() && this.mob.isInFluidType((fluidType, height) -> canSwimInFluid(this.mob, fluidType, height))) {
                while (true) {
                    if (groundState.getFluidState().isEmpty() || !canSwimInFluid(this.mob, groundState.getFluidState().getFluidType())) {
                        groundY--;

                        break;
                    }

                    groundState = this.currentContext.getBlockState(testPos.setY(++groundY));
                }
            }
            else if (this.mob.onGround()) {
                groundY = Mth.floor(this.mob.getY() + 0.5d);
            }
            else {
                testPos.setY(Mth.floor(this.mob.getY() + 1));

                while (testPos.getY() > this.currentContext.level().getMinBuildHeight()) {
                    groundY = testPos.getY();
                    testPos.setY(testPos.getY() - 1);
                    groundState = this.currentContext.getBlockState(testPos);

                    if (!groundState.isAir() && !groundState.isPathfindable(PathComputationType.LAND))
                        break;
                }
            }
        }
        else {
            while (this.mob.canStandOnFluid(groundState.getFluidState())) {
                groundState = this.currentContext.getBlockState(testPos.setY(++groundY));
            }

            groundY--;
        }

        if (!canStartAt(testPos.setY(groundY))) {
            AABB entityBounds = this.mob.getBoundingBox();

            if (canStartAt(testPos.set(entityBounds.minX, groundY, entityBounds.minZ))
                    || canStartAt(testPos.set(entityBounds.minX, groundY, entityBounds.maxZ))
                    || canStartAt(testPos.set(entityBounds.maxX, groundY, entityBounds.minZ))
                    || canStartAt(testPos.set(entityBounds.maxX, groundY, entityBounds.maxZ))) {
                return getStartNode(testPos);
            }
        }

        return getStartNode(testPos.set(this.mob.getX(), groundY, this.mob.getZ()));
    }

    /**
     * Get the position the mob would stand on if it were standing at the given position
     */
    @Override
    protected double getFloorLevel(BlockPos pos) {
        final BlockGetter blockGetter = this.currentContext.level();
        FluidState fluidState = blockGetter.getFluidState(pos);

        return (canFloat() || isAmphibious()) && canSwimInFluid(this.mob, fluidState.getFluidType(), fluidState.getHeight(blockGetter, pos))
                ? pos.getY() + 0.5d
                : getFloorLevel(blockGetter, pos);
    }
}
