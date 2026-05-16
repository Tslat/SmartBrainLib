package net.tslat.smartbrainlib.api.core.navigation;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import org.jspecify.annotations.Nullable;

/// Extension of the vanilla [WallClimberNavigation] with some tweaks for smoother pathfinding:
/// - Patched [Path] implementation to use proper rounding
/// - Accessible [GroundPathNavigation#getSurfaceY()] override for extensibility
///
/// Override [Mob#createNavigation(Level)] and return a new instance of this if your entity is a ground-based walking entity
public class SmoothWallClimberNavigation extends WallClimberNavigation implements ExtendedNavigator {
    public SmoothWallClimberNavigation(Mob mob, Level level) {
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
        this.nodeEvaluator = new WalkNodeEvaluator();
        this.nodeEvaluator.setCanPassDoors(true);

        return createSmoothPathFinder(this.nodeEvaluator, maxVisitedNodes);
    }

    /// Helper override to allow end-users to modify the fluids an entity can swim in
    ///
    /// If using this to modify swimmable fluids, ensure you also override [PathNavigation#canUpdatePath()]
    ///
    /// @return The nearest safe surface height for the entity
    @Override
    public int getSurfaceY() {
        return super.getSurfaceY();
    }
}
