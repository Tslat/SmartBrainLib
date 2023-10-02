package net.tslat.smartbrainlib.api.core.behaviour.custom.path;

import net.minecraft.world.entity.PathfinderMob;

/**
 * Extension of {@link SetRandomHoverTarget}, with a configurable weight to allow for more 'flying'-like movement
 */
public class SetRandomFlyingTarget<E extends PathfinderMob> extends SetRandomHoverTarget<E> {
}
