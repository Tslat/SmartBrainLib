package net.tslat.smartbrainlib.api.core.navigation;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import org.jetbrains.annotations.Nullable;

/// Extension of the vanilla [WallClimberNavigation] with some tweaks for smoother pathfinding:
///
///   - Patched [Path] implementation to use proper rounding
///   - Accessible [GroundPathNavigation#getSurfaceY()] override for extensibility
///
///
/// Override [Mob#createNavigation(Level)] and return a new instance of this if your entity is a ground-based walking entity
public class SmoothWallClimberNavigation extends WallClimberNavigation implements ExtendedNavigator {
    public SmoothWallClimberNavigation(Mob mob, Level level) {
        super(mob, level);
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
        this.nodeEvaluator = new WalkNodeEvaluator();
        this.nodeEvaluator.setCanPassDoors(true);

        return createSmoothPathFinder(this.nodeEvaluator, maxVisitedNodes);
    }

    /// Helper override to allow end-users to modify the fluids an entity can swim in
    ///
    /// If using this to modify swimmable fluids, ensure you also override [PathNavigation#canUpdatePath()] as well
    ///
    /// @return The nearest safe surface height for the entity
    @Override
    public int getSurfaceY() {
        return super.getSurfaceY();
    }
}
