package net.tslat.smartbrainlib.api.core.navigation;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.FlyNodeEvaluator;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import org.jetbrains.annotations.Nullable;

/**
 * Extension of the vanilla {@link FlyingPathNavigation} with some tweaks for smoother pathfinding:
 * <ul>
 *     <li>Patched {@link Path} implementation to use proper rounding</li>
 * </ul>
 * <p>
 * Override {@link Mob#createNavigation(Level)} and return a new instance of this if your entity is a ground-based walking entity
 */
public class SmoothFlyingPathNavigation extends FlyingPathNavigation implements ExtendedNavigator {
    public SmoothFlyingPathNavigation(Mob mob, Level level) {
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

    /**
     * Patch {@link Path#getEntityPosAtNode} to use a proper rounding check
     */
    @Override
    protected PathFinder createPathFinder(int maxVisitedNodes) {
        this.nodeEvaluator = new FlyNodeEvaluator();
        this.nodeEvaluator.setCanPassDoors(true);

        return createSmoothPathFinder(this.nodeEvaluator, maxVisitedNodes);
    }
}
