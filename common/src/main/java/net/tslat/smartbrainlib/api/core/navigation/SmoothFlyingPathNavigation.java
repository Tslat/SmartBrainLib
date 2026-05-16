package net.tslat.smartbrainlib.api.core.navigation;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.FlyNodeEvaluator;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import org.jspecify.annotations.Nullable;

/// Extension of the vanilla [FlyingPathNavigation] with some tweaks for smoother pathfinding:
/// - Patched [Path] implementation to use proper rounding
///
/// Override [Mob#createNavigation(Level)] and return a new instance of this if your entity is a ground-based walking entity
public class SmoothFlyingPathNavigation extends FlyingPathNavigation implements ExtendedNavigator {
    public SmoothFlyingPathNavigation(Mob mob, Level level) {
        super(mob, level);
    }

    /// Helper overload getter for retrieving the entity from [PathNavigation#mob]
    @Override
    public Mob getMob() {
        return this.mob;
    }

    /// Helper overload getter for retrieving the path from [PathNavigation#path]
    @Override
    public @Nullable Path getPath() {
        return super.getPath();
    }

    /// Patch [Path#getEntityPosAtNode] to use a proper rounding check
    @Override
    protected PathFinder createPathFinder(int maxVisitedNodes) {
        this.nodeEvaluator = new FlyNodeEvaluator();
        this.nodeEvaluator.setCanPassDoors(true);

        return createSmoothPathFinder(this.nodeEvaluator, maxVisitedNodes);
    }
}
