package net.tslat.smartbrainlib.api.core.navigation;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.AmphibiousPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.AmphibiousNodeEvaluator;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import org.jetbrains.annotations.Nullable;

/**
 * Extension of the vanilla {@link AmphibiousPathNavigation} with some tweaks for smoother pathfinding:
 * <ul>
 *     <li>Patched {@link Path} implementation to use proper rounding</li>
 *     <li>Extensible {@link #prefersShallowSwimming()} implementation for ease-of-use</li>
 * </ul>
 * <p>
 * Override {@link Mob#createNavigation(Level)} and return a new instance of this if your entity is a ground-based walking entity
 */
public class SmoothAmphibiousPathNavigation extends AmphibiousPathNavigation implements ExtendedNavigator {
    public SmoothAmphibiousPathNavigation(Mob mob, Level level) {
        super(mob, level);
    }

    /**
     * Determine whether the navigator should prefer shallow swimming patterns
     * <p>
     * Adjusts path node penalty when determining paths
     */
    public boolean prefersShallowSwimming() {
        return false;
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
        this.nodeEvaluator = new AmphibiousNodeEvaluator(prefersShallowSwimming());
        this.nodeEvaluator.setCanPassDoors(true);

        return createSmoothPathFinder(this.nodeEvaluator, maxVisitedNodes);
    }
}
