package net.tslat.smartbrainlib.api.util;

import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableDouble;
import org.apache.commons.lang3.mutable.MutableObject;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * A helper class for retrieving entities from a given world.
 * This removes a lot of the overhead of vanilla's type-checking and redundant stream-collection.
 * Ultimately this leaves casting up to the end-user, and streamlines the actual retrieval functions to their most optimised form.
 */
@SuppressWarnings("unchecked")
public final class EntityRetrievalUtil {
	/**
	 * Get the nearest entity from an existing list of entities.
	 *
	 * @param origin The center-point of the distance comparison
	 * @param entities The existing list of entities
	 * @return The closest entity to the origin point, or null if the input list was empty
	 * @param <T> The entity type
	 */
	@Nullable
	public static <T extends Entity> T getNearest(Vec3 origin, List<T> entities) {
		if (entities.isEmpty())
			return null;

		double dist = Double.MAX_VALUE;
		T closest = null;

		for (T entity : entities) {
			double entityDist = entity.distanceToSqr(origin);

			if (entityDist < dist) {
				dist = entityDist;
				closest = entity;
			}
		}

		return closest;
	}

	/**
	 * Retrieve the nearest entity with a certain radius of a given origin point that meet a given criteria. <br>
	 * Note that the output is blind-cast to your intended output type for ease of use. Make sure you check {@code instanceof} in your predicate if you intend to use any subclass of Entity
	 *
	 * @param origin The entity to act as the central point of the search radius
	 * @param radius The radius on the axis to search
	 * @param predicate The predicate to filter entities by
	 * @return The closest entity found that meets the given criteria, or null if none found
	 * @param <T> The output entity subtype
	 */
	@Nullable
	public static <T extends Entity> T getNearestEntity(Entity origin, double radius, Predicate<? extends Entity> predicate) {
		return getNearestEntity(origin, radius, radius, radius, predicate);
	}

	/**
	 * Retrieve the nearest entity with a certain radius of a given origin point that meet a given criteria. <br>
	 * Note that the output is blind-cast to your intended output type for ease of use. Make sure you check {@code instanceof} in your predicate if you intend to use any subclass of Entity
	 *
	 * @param origin The entity to act as the central point of the search radius
	 * @param radiusX The radius on the x-axis to search
	 * @param radiusY The radius on the y-axis to search
	 * @param radiusZ The radius on the z-axis to search
	 * @param predicate The predicate to filter entities by
	 * @return The closest entity found that meets the given criteria, or null if none found
	 * @param <T> The output entity subtype
	 */
	@Nullable
	public static <T extends Entity> T getNearestEntity(Entity origin, double radiusX, double radiusY, double radiusZ, Predicate<? extends Entity> predicate) {
		return getNearestEntity(origin.level, new AABB(origin.getX() - radiusX, origin.getY() - radiusY, origin.getZ() - radiusZ, origin.getX() + radiusX, origin.getY() + radiusY, origin.getZ() + radiusZ), origin.position(), predicate);
	}

	/**
	 * Retrieve the nearest entity with a certain radius of a given origin point that meet a given criteria. <br>
	 * Note that the output is blind-cast to your intended output type for ease of use. Make sure you check {@code instanceof} in your predicate if you intend to use any subclass of Entity
	 *
	 * @param level The level to search in
	 * @param area The region to search for entities in
	 * @param origin The center-point of the search
	 * @param predicate The predicate to filter entities by
	 * @return The closest entity found that meets the given criteria, or null if none found
	 * @param <T> The output entity subtype
	 */
	@Nullable
	public static <T extends Entity> T getNearestEntity(Level level, AABB area, Vec3 origin, Predicate<? extends Entity> predicate) {
		final Predicate<Entity> typeSafePredicate = (Predicate<Entity>)predicate;
		final MutableDouble dist = new MutableDouble(Double.MAX_VALUE);
		final MutableObject<Entity> closest = new MutableObject<>(null);

		level.getEntities().get(area, entity -> {
			if (typeSafePredicate.test(entity)) {
				double entityDist = entity.distanceToSqr(origin);

				if (entityDist < dist.getValue()) {
					dist.setValue(entityDist);
					closest.setValue(entity);
				}
			}
		});

		return (T)closest.getValue();
	}

	/**
	 * Retrieve the nearest player with a certain radius of a given origin point that meet a given criteria.
	 *
	 * @param origin The entity to act as the central point of the search radius
	 * @param radius The radius on the axis to search
	 * @param predicate The predicate to filter players by
	 * @return The closest entity found that meets the given criteria, or null if none found
	 */
	@Nullable
	public static Player getNearestPlayer(Entity origin, double radius, Predicate<Player> predicate) {
		return getNearestPlayer(origin, radius, radius, radius, predicate);
	}

	/**
	 * Retrieve the nearest player with a certain radius of a given origin point that meet a given criteria.
	 *
	 * @param origin The entity to act as the central point of the search radius
	 * @param radiusX The radius on the x-axis to search
	 * @param radiusY The radius on the y-axis to search
	 * @param radiusZ The radius on the z-axis to search
	 * @param predicate The predicate to filter players by
	 * @return The closest entity found that meets the given criteria, or null if none found
	 */
	@Nullable
	public static Player getNearestPlayer(Entity origin, double radiusX, double radiusY, double radiusZ, Predicate<Player> predicate) {
		return getNearestPlayer(origin.level, new AABB(origin.getX() - radiusX, origin.getY() - radiusY, origin.getZ() - radiusZ, origin.getX() + radiusX, origin.getY() + radiusY, origin.getZ() + radiusZ), origin.position(), predicate);
	}

	/**
	 * Retrieve the nearest player with a certain radius of a given origin point that meet a given criteria.
	 *
	 * @param level The level to search in
	 * @param area The region to search for players in
	 * @param origin The center-point of the search
	 * @param predicate The predicate to filter players by
	 * @return The closest entity found that meets the given criteria, or null if none found
	 */
	@Nullable
	public static Player getNearestPlayer(Level level, AABB area, Vec3 origin, Predicate<Player> predicate) {
		double dist = Double.MAX_VALUE;
		Player closest = null;

		for (Player player : level.players()) {
			if (area.contains(player.position()) && predicate.test(player)) {
				double playerDist = player.distanceToSqr(origin);

				if (playerDist < dist) {
					dist = playerDist;
					closest = player;
				}
			}
		}

		return closest;
	}

	/**
	 * Get all players within a given region.
	 *
	 * @param level The level in which to search
	 * @param area The region in which to find players
	 * @return A list of players that are within the given region
	 */
	public static List<Player> getPlayers(Level level, AABB area) {
		return getPlayers(level, area, pl -> true);
	}

	/**
	 * Get all players within a given region that meet a given criteria.
	 *
	 * @param origin The entity to act as the central point of the search radius
	 * @param radius The radius on the axis to search
	 * @param predicate The criteria to meet for a player to be included in the returned list
	 * @return A list of players that are within the given region that meet the criteria in the predicate
	 */
	public static List<Player> getPlayers(Entity origin, double radius, Predicate<Player> predicate) {
		return getPlayers(origin, radius, radius, radius, predicate);
	}

	/**
	 * Get all players within a given region that meet a given criteria.
	 *
	 * @param origin The entity to act as the central point of the search radius
	 * @param radiusX The radius on the x-axis to search
	 * @param radiusY The radius on the y-axis to search
	 * @param radiusZ The radius on the z-axis to search
	 * @param predicate The criteria to meet for a player to be included in the returned list
	 * @return A list of players that are within the given region that meet the criteria in the predicate
	 */
	public static List<Player> getPlayers(Entity origin, double radiusX, double radiusY, double radiusZ, Predicate<Player> predicate) {
		return getPlayers(origin.level, new AABB(origin.getX() - radiusX, origin.getY() - radiusY, origin.getZ() - radiusZ, origin.getX() + radiusX, origin.getY() + radiusY, origin.getZ() + radiusZ), predicate);
	}

	/**
	 * Get all players within a given region that meet a given criteria.
	 *
	 * @param level The level in which to search
	 * @param area The region in which to find players
	 * @param predicate The criteria to meet for a player to be included in the returned list
	 * @return A list of players that are within the given region that meet the criteria in the predicate
	 */
	public static List<Player> getPlayers(Level level, AABB area, Predicate<Player> predicate) {
		List<Player> players = new ObjectArrayList<>();

		for (Player player : level.players()) {
			if (area.contains(player.position()) && predicate.test(player))
				players.add(player);
		}

		return players;
	}

	/**
	 * Retrieve all nearby entities from the given area that meet the given criteria. <br>
	 * Note that the output is blind-cast to your intended output type for ease of use. Make sure you check {@code instanceof} in your predicate if you intend to use any subclass of Entity
	 *
	 * @param origin The entity to act as the central point of the search radius
	 * @param radius The radius on the axis to search
	 * @param predicate The predicate to filter entities by
	 * @return A list of entities found in the provided region that meet the criteria of the predicate, or an empty list if none match
	 * @param <T> The output entity subtype
	 */
	public static <T> List<T> getEntities(Entity origin, double radius, Predicate<? extends Entity> predicate) {
		return getEntities(origin, radius, radius, radius, predicate);
	}

	/**
	 * Retrieve all nearby entities from the given area that meet the given criteria. <br>
	 * Note that the output is blind-cast to your intended output type for ease of use. Make sure you check {@code instanceof} in your predicate if you intend to use any subclass of Entity
	 *
	 * @param origin The entity to act as the central point of the search radius
	 * @param radiusX The radius on the x-axis to search
	 * @param radiusY The radius on the y-axis to search
	 * @param radiusZ The radius on the z-axis to search
	 * @param predicate The predicate to filter entities by
	 * @return A list of entities found in the provided region that meet the criteria of the predicate, or an empty list if none match
	 * @param <T> The output entity subtype
	 */
	public static <T> List<T> getEntities(Entity origin, double radiusX, double radiusY, double radiusZ, Predicate<? extends Entity> predicate) {
		return getEntities(origin.level, new AABB(origin.getX() - radiusX, origin.getY() - radiusY, origin.getZ() - radiusZ, origin.getX() + radiusX, origin.getY() + radiusY, origin.getZ() + radiusZ), predicate.and(entity -> entity != origin));
	}

	/**
	 * Retrieve all nearby entities from the given area that meet the given criteria. <br>
	 * Note that the output is blind-cast to your intended output type for ease of use. Make sure you check {@code instanceof} in your predicate if you intend to use any subclass of Entity
	 *
	 * @param level The level to search in
	 * @param area The region to search for entities in
	 * @param predicate The predicate to filter entities by
	 * @return A list of entities found in the provided region that meet the criteria of the predicate, or an empty list if none match
	 * @param <T> The output entity subtype
	 */
	public static <T> List<T> getEntities(Level level, AABB area, Predicate<? extends Entity> predicate) {
		Predicate<Entity> typeSafePredicate = (Predicate<Entity>)predicate;
		List<T> entities = new ObjectArrayList<>();

		level.getEntities().get(area, entity -> {
			if (typeSafePredicate.test(entity))
				entities.add((T)entity);
		});

		return entities;
	}
}
