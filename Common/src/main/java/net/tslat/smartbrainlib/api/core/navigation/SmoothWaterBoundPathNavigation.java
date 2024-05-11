package net.tslat.smartbrainlib.api.core.navigation;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.SwimNodeEvaluator;
import org.jetbrains.annotations.Nullable;

/**
 * Extension of the vanilla {@link WaterBoundPathNavigation} navigator with some tweaks for smoother pathfinding:
 * <ul>
 *     <li>Smoothed unit rounding to better accommodate edge-cases</li>
 *     <li>Patched {@link Path} implementation to use proper rounding</li>
 *     <li>Extensible constructor pattern to allow non-dolphin breaching mobs</li>
 * </ul>
 * <p>
 * Override {@link Mob#createNavigation(Level)} and return a new instance of this if your entity is a water-based swimming entity
 * @see ExtendedNavigator#canPathOnto
 * @see ExtendedNavigator#canPathInto
 */
public class SmoothWaterBoundPathNavigation extends WaterBoundPathNavigation implements ExtendedNavigator {
    public SmoothWaterBoundPathNavigation(Mob mob, Level level) {
        super(mob, level);

        this.allowBreaching = this.mob.getType() == EntityType.DOLPHIN;
    }

    /**
     * Set whether the entity can breach the surface as part of its pathing
     *
     * @return this
     */
    public SmoothWaterBoundPathNavigation setCanBreach(boolean canBreach) {
        this.allowBreaching = canBreach;

        return this;
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
        this.nodeEvaluator = new SwimNodeEvaluator(this.allowBreaching);
        this.nodeEvaluator.setCanPassDoors(true);

        return createSmoothPathFinder(this.nodeEvaluator, maxVisitedNodes);
    }
}
