package net.tslat.smartbrainlib.api.core.navigation;

import net.minecraft.world.entity.Mob;
import net.neoforged.neoforge.fluids.FluidType;

/**
 * Extracted interface to act as a helper utility for extensible implementations of (Neo)Forge's fluid API overhaul into pathfinding
 */
public interface MultiFluidNavigationElement {
    /**
     * Determine whether a given fluidType is one that the given entity can actively navigate through
     * <p>
     * Note that the provided entity may not necessarily be in the given fluid at the time of this call
     *
     * @param mob The entity to check the pathing capabilities for
     * @param fluidType The FluidType to check
     */
    default boolean canSwimInFluid(Mob mob, FluidType fluidType) {
        return canSwimInFluid(mob, fluidType, 1);
    }

    /**
     * Determine whether a given fluidType is one that the given entity can actively navigate through
     * <p>
     * Note that the provided entity may not necessarily be in the given fluid at the time of this call
     *
     * @param mob The entity to check the pathing capabilities for
     * @param fluidType The FluidType to check
     * @param fluidHeight The depth of the given fluid in the block. I.E. the percentage of the block the fluid is deep
     */
    default boolean canSwimInFluid(Mob mob, FluidType fluidType, double fluidHeight) {
        return fluidType.canSwim(mob);
    }
}
