package net.tslat.smartbrainlib.api.core.navigation;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.SwimNodeEvaluator;
import org.jetbrains.annotations.Nullable;

/// Extension of the vanilla [WaterBoundPathNavigation] navigator with some tweaks for smoother pathfinding:
///
///   - Smoothed unit rounding to better accommodate edge-cases
///   - Patched [Path] implementation to use proper rounding
///   - Extensible [#canBreach()] implementation for ease-of-use
///
///
/// Override [Mob#createNavigation(Level)] and return a new instance of this if your entity is a water-based swimming entity
public class SmoothWaterBoundPathNavigation extends WaterBoundPathNavigation implements ExtendedNavigator {
    public SmoothWaterBoundPathNavigation(Mob mob, Level level) {
        super(mob, level);
    }

    /// Determine whether the entity can breach the surface as part of its pathing
    ///
    /// Defaults to false for non-dolphins
    public boolean canBreach() {
        return this.mob.getType() == EntityTypes.DOLPHIN;
    }

    @Override
    public Mob getMob() {
        return this.mob;
    }

    @Nullable
    @Override
    public Path getPath() {
        return super.getPath();
    }

    /// Patch [Path#getEntityPosAtNode] to use a proper rounding check
    @Override
    protected PathFinder createPathFinder(int maxVisitedNodes) {
        this.nodeEvaluator = new SwimNodeEvaluator(this.allowBreaching = canBreach());
        this.nodeEvaluator.setCanPassDoors(true);

        return createSmoothPathFinder(this.nodeEvaluator, maxVisitedNodes);
    }
}
