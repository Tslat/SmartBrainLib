package net.tslat.smartbrainlib.api.core.navigation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.BulkSectionAccess;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;

/**
 * Extracted interface to act as a helper utility for cleaner navigator implementations
 * <p>
 * This expands on Vanilla's navigator functionality, fixing some issues, optimising it, and splitting it out to be properly extensible
 */
public interface ExtendedNavigator {
    /**
     * Minimum threshold representing a rounding error for the purposes of bounds collision
     */
    float EPSILON = 1.0E-8F;

    /**
     * Helper overload getter for retrieving the entity from {@link PathNavigation#mob}
     */
    Mob getMob();

    /**
     * Helper overload getter for retrieving the path from {@link PathNavigation#path}
     */
    Path getPath();

    /**
     * @return Whether the given path type can be pathed onto, or otherwise be considered a pathable surface
     */
    default boolean canPathOnto(BlockPathTypes pathType) {
        return switch (pathType) {
            case WATER, LAVA, OPEN -> false;
            default -> true;
        };
    }

    /**
     * @return Whether the given path type should be considered safe to path into, or is otherwise a pathable free space
     */
    default boolean canPathInto(BlockPathTypes pathType) {
        return switch (pathType) {
            case DAMAGE_FIRE, DANGER_FIRE, DAMAGE_OTHER -> true;
            default -> false;
        };
    }

    /**
     * Determine whether the entity should be considered close enough to the next node to be counted as having reached it
     *
     * @param distance The distance threshold which counts as 'close enough' to the next node
     * @return Whether the entity is within reach of the next node in the path
     * @see PathNavigation#getMaxDistanceToWaypoint()
     */
    default boolean isCloseToNextNode(float distance) {
        final Mob mob = getMob();
        final Path path = getPath();
        final Vec3 nextNodePos = getEntityPosAtNode(path.getNextNodeIndex());

        return Math.abs(mob.getX() - nextNodePos.x) < distance &&
                Math.abs(mob.getZ() - nextNodePos.z) < distance &&
                Math.abs(mob.getY() - nextNodePos.y) < 1;
    }

    /**
     * @return Whether the path the mob is following is about to cause a change in elevation (either up or down), accounting for potentially skippable nodes based on the entity's stride size
     */
    default boolean isAboutToTraverseVertically() {
        final Mob mob = getMob();
        final Path path = getPath();
        final int fromNode = path.getNextNodeIndex();
        final int fromNodeHeight = path.getNode(fromNode).y;
        final int toNode = Math.min(path.getNodeCount(), fromNode + Mth.ceil(mob.getBbWidth() * 0.5d) + 1);

        for (int i = fromNode + 1; i < toNode; i++) {
            if (path.getNode(i).y != fromNodeHeight)
                return true;
        }

        return false;
    }

    /**
     * Attempt to skip to the target node, bypassing the intermediate notes depending on bounds collision for the intervening distance
     * <p>
     * Typically, the target node should already have been checked for proximal relevance, as otherwise this could cause node skips to act strangely
     *
     * @param targetNode     The target node index to shortcut to
     * @param safeSurfacePos The baseline position of where the mob should traverse to (usually the nearest ground pos or surface of the fluid it's submerged in)
     * @return Whether the shortcut was successful or not
     */
    default boolean attemptShortcut(int targetNode, Vec3 safeSurfacePos) {
        final Mob mob = getMob();
        final Path path = getPath();
        final Vec3 position = mob.position();
        final Vec3 minBounds = safeSurfacePos.add(-mob.getBbWidth() * 0.5d, 0, -mob.getBbWidth() * 0.5d);
        final Vec3 maxBounds = minBounds.add(mob.getBbWidth(), mob.getBbHeight(), mob.getBbWidth());

        for (int nodeIndex = targetNode - 1; nodeIndex > path.getNextNodeIndex(); nodeIndex--) {
            final Vec3 nodeDelta = getEntityPosAtNode(nodeIndex).subtract(position);

            if (isCollisionFreeTraversal(nodeDelta, minBounds, maxBounds)) {
                path.setNextNodeIndex(nodeIndex);

                return true;
            }
        }

        return false;
    }

    /**
     * Get the entity's predicted position at the time they reach the given node
     * <p>
     * Functionally replaces {@link Path#getEntityPosAtNode} to better handle the double rounding
     *
     * @param nodeIndex The index of the node to check
     * @return The approximate position of the entity for the given node
     */
    default Vec3 getEntityPosAtNode(int nodeIndex) {
        final Mob mob = getMob();
        final Path path = getPath();
        final double lateralOffset = Mth.floor(mob.getBbWidth() + 1d) / 2d;

        return Vec3.atLowerCornerOf(path.getNodePos(nodeIndex)).add(lateralOffset, 0, lateralOffset);
    }

    /**
     * Recursively sweep the edges of a given area, identifying collisions for colliding faces of a pseudo-bounds determined by ray-casts
     * projected from the bounds leading edge, then cross-checking interceptions for the relevant face.
     * <p>
     * This is a quick algorithm based on Andy Hall's <a href="https://github.com/fenomas/voxel-aabb-sweep/tree/d3ef85b19c10e4c9d2395c186f9661b052c50dc7">voxel-aabb-sweep</a>
     *
     * @param traversalVector The vector that represents the angle and length of traversal to cover
     * @param minBoundsPos    The negative-most position representing the minimum corner of the bounds
     * @param leadingEdgePos  The positive-most position representing the maximum corner of the bounds
     * @return Whether the given traversal is free from collisions
     */
    default boolean isCollisionFreeTraversal(Vec3 traversalVector, Vec3 minBoundsPos, Vec3 leadingEdgePos) {
        final float traversalDistance = (float)traversalVector.length();

        if (traversalDistance < EPSILON)
            return true;

        final VoxelRayDetails ray = new VoxelRayDetails();

        for (Direction.Axis axis : Direction.Axis.values()) {
            final int index = axis.ordinal();
            final float axisLength = lengthForAxis(traversalVector, axis);
            final boolean isPositive = axisLength >= 0;
            final float maxPos = lengthForAxis(isPositive ? leadingEdgePos : minBoundsPos, axis);

            ray.absStep[index] = isPositive ? 1 : -1;
            ray.minPos[index] = lengthForAxis(isPositive ? minBoundsPos : leadingEdgePos, axis);
            ray.leadingEdgeBound[index] = Mth.floor(maxPos - ray.absStep[index] * EPSILON);
            ray.trailingEdgeBound[index] = Mth.floor(ray.minPos[index] + ray.absStep[index] * EPSILON);
            ray.axisLengthNormalised[index] = axisLength / traversalDistance;
            ray.axisSteps[index] = Mth.abs(traversalDistance / axisLength);
            final float dist = isPositive ? (ray.leadingEdgeBound[index] + 1 - maxPos) : (maxPos - ray.leadingEdgeBound[index]);
            ray.rayTargetLength[index] = ray.axisSteps[index] < Float.POSITIVE_INFINITY ? ray.axisSteps[index] * dist : Float.POSITIVE_INFINITY;
        }

        return collidesWhileTraversing(ray, traversalDistance);
    }

    /**
     * @param ray The details container for the ray traversal
     * @param traversalDistance The direct length of the traversal vector
     * @return Whether the given bounds would collide for the given trajectory
     */
    default boolean collidesWhileTraversing(VoxelRayDetails ray, float traversalDistance) {
        final Mob mob = getMob();
        final Level level = mob.level();

        try (BulkSectionAccess sectionAccess = new BulkSectionAccess(level)) {
            final NodeEvaluator nodeEvaluator = mob.getNavigation().getNodeEvaluator();
            final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            float target = 0;

            do {
                final Direction.Axis longestEdge = ray.rayTargetLength[0] < ray.rayTargetLength[1] ?
                        ray.rayTargetLength[0] < ray.rayTargetLength[2] ? Direction.Axis.X : Direction.Axis.Z :
                        ray.rayTargetLength[1] < ray.rayTargetLength[2] ? Direction.Axis.Y : Direction.Axis.Z;
                final int index = longestEdge.ordinal();
                final float rayDelta = ray.rayTargetLength[index] - target;
                target = ray.rayTargetLength[index];
                ray.leadingEdgeBound[index] += ray.absStep[index];
                ray.rayTargetLength[index] += ray.axisSteps[index];

                for (Direction.Axis axis : Direction.Axis.values()) {
                    final int index2 = axis.ordinal();
                    ray.minPos[index2] += rayDelta * ray.axisLengthNormalised[index2];
                    ray.trailingEdgeBound[index2] = Mth.floor(ray.minPos[index2] + ray.absStep[index2] * EPSILON);
                }

                final int xStep = ray.absStep[0];
                final int yStep = ray.absStep[1];
                final int zStep = ray.absStep[2];
                final int xBound = longestEdge == Direction.Axis.X ? ray.leadingEdgeBound[0] : ray.trailingEdgeBound[0];
                final int yBound = longestEdge == Direction.Axis.Y ? ray.leadingEdgeBound[1] : ray.trailingEdgeBound[1];
                final int zBound = longestEdge == Direction.Axis.Z ? ray.leadingEdgeBound[2] : ray.trailingEdgeBound[2];
                final int xStepBound = ray.leadingEdgeBound[0] + xStep;
                final int yStepBound = ray.leadingEdgeBound[1] + yStep;
                final int zStepBound = ray.leadingEdgeBound[2] + zStep;

                for (int x = xBound; x != xStepBound; x += xStep) {
                    for (int z = zBound; z != zStepBound; z += zStep) {
                        for (int y = yBound; y != yStepBound; y += yStep) {
                            if (!sectionAccess.getBlockState(pos.set(x, y, z)).isPathfindable(level, pos, PathComputationType.LAND))
                                return false;
                        }

                        if (!canPathOnto(nodeEvaluator.getBlockPathType(level, x, yBound - 1, z)))
                            return false;

                        final BlockPathTypes insidePathType = nodeEvaluator.getBlockPathType(level, x, yBound, z);
                        final float pathMalus = mob.getPathfindingMalus(insidePathType);

                        if (pathMalus < 0 || pathMalus >= 8)
                            return false;

                        if (canPathInto(insidePathType))
                            return false;
                    }
                }
            } while (target <= traversalDistance);
        }

        return true;
    }

    /**
     * Container object for voxel ray traversal details
     * <p>
     * Each array represent [x, y, z] vector coordinates
     *
     * @param minPos               The minimum-pos coordinate for the given axis
     * @param leadingEdgeBound     The maximum-pos axis-aligned coordinate for the given axis
     * @param trailingEdgeBound    The mimimum-pos axis-aligned coordinate for the given axis
     * @param absStep              -1 or 1 value representing which direction to step for each axis
     * @param axisSteps            How many lengths of the given axis required to traverse the full distance
     * @param rayTargetLength      How long the ray should be to account for traversal
     * @param axisLengthNormalised Fraction of the full distance the given length of this axis represents
     */
    record VoxelRayDetails(float[] minPos, int[] leadingEdgeBound, int[] trailingEdgeBound, int[] absStep, float[] axisSteps, float[] rayTargetLength, float[] axisLengthNormalised) {
        public VoxelRayDetails() {
            this(new float[3], new int[3], new int[3], new int[3], new float[3], new float[3], new float[3]);
        }
    }

    /**
     * @return The vector length for the given axis
     */
    default float lengthForAxis(Vec3 vector, Direction.Axis axis) {
        return (float)axis.choose(vector.x, vector.y, vector.z);
    }
}