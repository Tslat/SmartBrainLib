package net.tslat.smartbrainlib.util;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.SBLConstants;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;

/// A helper class for retrieving entities from a given world
///
/// This removes a lot of the overhead of vanilla's type-checking, casting, and redundant stream-collection
///
/// Code borrowed from `TslatModdingExtensions`
public final class EntityRetrievalUtil {
	/// Get the [Entity] from the provided list that is closest to the origin point
	///
	/// @param origin   	The center-point of the distance comparison
	/// @param entities 	The existing list of entities
	/// @return         	The closest entity to the origin point, or null if the input list was empty
	/// @param <T>      	The lowest-common entity type
	public static <T extends Entity> @Nullable T getNearest(Vec3 origin, List<T> entities) {
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

	/// Get the closest [Entity] to an origin entity, only including entities within a radius of that entity
	///
	/// **<u>NOTE:</u>** The origin entity will be automatically excluded from the search
	///
	/// @param origin   	The entity to center the search on
	/// @param radius 		The radius to search within from the center
	/// @return         	An optional containing the closest entity to the origin entity, or an empty optional if none found
	public static Optional<Entity> getNearestEntity(Entity origin, double radius) {
		return getNearestEntity(origin, radius, radius, radius);
	}

	/// Get the closest [Entity] to an origin entity, only including entities within a radius of that entity
	///
	/// **<u>NOTE:</u>** The origin entity will be automatically excluded from the search
	///
	/// @param origin   	The entity to center the search on
	/// @param radiusX 		The radius to search within from the center, on the X axis
	/// @param radiusY 		The radius to search within from the center, on the Y axis
	/// @param radiusZ 		The radius to search within from the center, on the Z axis
	/// @return         	An optional containing the closest entity to the origin entity, or an empty optional if none found
	public static Optional<Entity> getNearestEntity(Entity origin, double radiusX, double radiusY, double radiusZ) {
		return getNearestEntity(origin.level(), origin.getBoundingBox().inflate(radiusX, radiusY, radiusZ), origin.position(), entity -> entity != origin);
	}

	/// Get the closest [Entity] to an origin point, only including entities within a radius of that point
	///
	/// @param level   		The level in which to search for entities
	/// @param origin   	The center location to search around
	/// @param radius 		The radius to search within from the center
	/// @return         	An optional containing the closest entity to the origin point, or an empty optional if none found
	public static Optional<Entity> getNearestEntity(Level level, Vec3 origin, double radius) {
		return getNearestEntity(level, origin, radius, radius, radius);
	}

	/// Get the closest [Entity] to an origin point, only including entities within a radius of that point
	///
	/// @param level   		The level in which to search for entities
	/// @param origin   	The center location to search around
	/// @param radiusX 		The radius to search within from the center, on the X axis
	/// @param radiusY 		The radius to search within from the center, on the Y axis
	/// @param radiusZ 		The radius to search within from the center, on the Z axis
	/// @return         	An optional containing the closest entity to the origin point, or an empty optional if none found
	public static Optional<Entity> getNearestEntity(Level level, Vec3 origin, double radiusX, double radiusY, double radiusZ) {
		return getNearestEntity(level, AABB.ofSize(origin, radiusX, radiusY, radiusZ), origin);
	}

	/// Get the closest [Entity] to an origin point, only including entities within a specified area
	///
	/// @param level   		The level in which to search for entities
	/// @param bounds   	The region in which to search
	/// @param origin   	The center location to search around
	/// @return         	An optional containing the closest entity to the origin point, or an empty optional if none found
	public static Optional<Entity> getNearestEntity(Level level, AABB bounds, Vec3 origin) {
		return getNearestEntity(level, bounds, origin, Entity.class);
	}

	/// Get the closest [Entity] to an origin entity, only including entities within a radius of that entity, filtering by entity class.<br/>
	/// This is significantly more efficient than non-filtering search methods, due to the way entities are stored
	///
	/// **<u>NOTE:</u>** The origin entity will be automatically excluded from the search and will not be passed to the predicate
	///
	/// @param origin   	The entity to center the search on
	/// @param radius 		The radius to search within from the center
	/// @param minimumClass The minimum entity class type to search for
	/// @param <T> 			The class in which all checked entities should be or extend. More specific types are more efficient
	/// @return         	An optional containing the closest entity to the origin entity, or an empty optional if none found
	public static <T extends Entity> Optional<T> getNearestEntity(Entity origin, double radius, Class<T> minimumClass) {
		return getNearestEntity(origin, radius, radius, radius, minimumClass);
	}

	/// Get the closest [Entity] to an origin entity, only including entities within a radius of that entity, filtering by entity class.<br/>
	/// This is significantly more efficient than non-filtering search methods, due to the way entities are stored
	///
	/// **<u>NOTE:</u>** The origin entity will be automatically excluded from the search
	///
	/// @param origin   	The entity to center the search on
	/// @param radiusX 		The radius to search within from the center, on the X axis
	/// @param radiusY 		The radius to search within from the center, on the Y axis
	/// @param radiusZ 		The radius to search within from the center, on the Z axis
	/// @param minimumClass The minimum entity class type to search for
	/// @param <T> 			The class in which all checked entities should be or extend. More specific types are more efficient
	/// @return         	An optional containing the closest entity to the origin entity, or an empty optional if none found
	public static <T extends Entity> Optional<T> getNearestEntity(Entity origin, double radiusX, double radiusY, double radiusZ, Class<T> minimumClass) {
		return getNearestEntity(origin.level(), origin.getBoundingBox().inflate(radiusX, radiusY, radiusZ), origin.position(), minimumClass, entity -> entity != origin);
	}

	/// Get the closest [Entity] to an origin point, only including entities within a radius of that point, filtering by entity class.<br/>
	/// This is significantly more efficient than non-filtering search methods, due to the way entities are stored
	///
	/// @param level   		The level in which to search for entities
	/// @param origin   	The center location to search around
	/// @param radius 		The radius to search within from the center
	/// @param minimumClass The minimum entity class type to search for
	/// @param <T> 			The class in which all checked entities should be or extend. More specific types are more efficient
	/// @return         	An optional containing the closest entity to the origin point, or an empty optional if none found
	public static <T extends Entity> Optional<T> getNearestEntity(Level level, Vec3 origin, double radius, Class<T> minimumClass) {
		return getNearestEntity(level, origin, radius, radius, radius, minimumClass);
	}

	/// Get the closest [Entity] to an origin point, only including entities within a radius of that point, filtering by entity class.<br/>
	/// This is significantly more efficient than non-filtering search methods, due to the way entities are stored
	///
	/// @param level   		The level in which to search for entities
	/// @param origin   	The center location to search around
	/// @param radiusX 		The radius to search within from the center, on the X axis
	/// @param radiusY 		The radius to search within from the center, on the Y axis
	/// @param radiusZ 		The radius to search within from the center, on the Z axis
	/// @param minimumClass The minimum entity class type to search for
	/// @param <T> 			The class in which all checked entities should be or extend. More specific types are more efficient
	/// @return         	An optional containing the closest entity to the origin point, or an empty optional if none found
	public static <T extends Entity> Optional<T> getNearestEntity(Level level, Vec3 origin, double radiusX, double radiusY, double radiusZ, Class<T> minimumClass) {
		return getNearestEntity(level, AABB.ofSize(origin, radiusX, radiusY, radiusZ), origin, minimumClass);
	}

	/// Get the closest [Entity] to an origin point, only including entities within a specified area, filtering by entity class.<br/>
	/// This is significantly more efficient than non-filtering search methods, due to the way entities are stored
	///
	/// @param level   		The level in which to search for entities
	/// @param bounds   	The region in which to search
	/// @param origin   	The center location to search around
	/// @param minimumClass The minimum entity class type to search for
	/// @param <T> 			The class in which all checked entities should be or extend. More specific types are more efficient
	/// @return         	An optional containing the closest entity to the origin point, or an empty optional if none found
	public static <T extends Entity> Optional<T> getNearestEntity(Level level, AABB bounds, Vec3 origin, Class<T> minimumClass) {
		return getNearestEntity(level, bounds, origin, minimumClass, _ -> true);
	}

	/// Get the closest [Entity] to an origin entity, only including entities within a radius of that entity, filtering by a [Predicate]
	///
	/// **<u>NOTE:</u>** The origin entity will be automatically excluded from the search and will not be passed to the predicate
	///
	/// @param origin   	The entity to center the search on
	/// @param radius 		The radius to search within from the center
	/// @param predicate	A predicate to apply to determine eligible entities to return
	/// @return         	An optional containing the closest entity to the origin entity, or an empty optional if none found
	public static Optional<Entity> getNearestEntity(Entity origin, double radius, Predicate<Entity> predicate) {
		return getNearestEntity(origin, radius, radius, radius, predicate);
	}

	/// Get the closest [Entity] to an origin entity, only including entities within a radius of that entity, filtering by a [Predicate]
	///
	/// **<u>NOTE:</u>** The origin entity will be automatically excluded from the search and will not be passed to the predicate
	///
	/// @param origin   	The entity to center the search on
	/// @param radiusX 		The radius to search within from the center, on the X axis
	/// @param radiusY 		The radius to search within from the center, on the Y axis
	/// @param radiusZ 		The radius to search within from the center, on the Z axis
	/// @param predicate	A predicate to apply to determine eligible entities to return
	/// @return         	An optional containing the closest entity to the origin entity, or an empty optional if none found
	public static Optional<Entity> getNearestEntity(Entity origin, double radiusX, double radiusY, double radiusZ, Predicate<Entity> predicate) {
		return getNearestEntity(origin.level(), origin.getBoundingBox().inflate(radiusX, radiusY, radiusZ), origin.position(),
								((Predicate<Entity>)entity -> entity != origin).and(predicate));
	}

	/// Get the closest [Entity] to an origin point, only including entities within a radius of that point, filtering by a [Predicate]
	///
	/// **<u>NOTE:</u>** The origin entity will be automatically excluded from the search and will not be passed to the predicate
	///
	/// @param level   		The level in which to search for entities
	/// @param origin   	The center location to search around
	/// @param radius 		The radius to search within from the center
	/// @param predicate	A predicate to apply to determine eligible entities to return
	/// @return         	An optional containing the closest entity to the origin point, or an empty optional if none found
	public static Optional<Entity> getNearestEntity(Level level, Vec3 origin, double radius, Predicate<Entity> predicate) {
		return getNearestEntity(level, origin, radius, radius, radius, predicate);
	}

	/// Get the closest [Entity] to an origin point, only including entities within a radius of that point, filtering by a [Predicate]
	///
	/// @param level   		The level in which to search for entities
	/// @param origin   	The center location to search around
	/// @param radiusX 		The radius to search within from the center, on the X axis
	/// @param radiusY 		The radius to search within from the center, on the Y axis
	/// @param radiusZ 		The radius to search within from the center, on the Z axis
	/// @param predicate	A predicate to apply to determine eligible entities to return
	/// @return         	An optional containing the closest entity to the origin point, or an empty optional if none found
	public static Optional<Entity> getNearestEntity(Level level, Vec3 origin, double radiusX, double radiusY, double radiusZ, Predicate<Entity> predicate) {
		return getNearestEntity(level, AABB.ofSize(origin, radiusX, radiusY, radiusZ), origin, predicate);
	}

	/// Get the closest [Entity] to an origin point, only including entities within a specified area, filtering by a [Predicate]
	///
	/// @param level   		The level in which to search for entities
	/// @param bounds   	The region in which to search
	/// @param origin   	The center location to search around
	/// @param predicate	A predicate to apply to determine eligible entities to return
	/// @return         	An optional containing the closest entity to the origin point, or an empty optional if none found
	public static Optional<Entity> getNearestEntity(Level level, AABB bounds, Vec3 origin, Predicate<Entity> predicate) {
		return getNearestEntity(level, bounds, origin, Entity.class, predicate);
	}

	/// Get the closest [Entity] to an origin entity, only including entities within a radius of that entity, filtering by entity class and a [Predicate].<br/>
	/// This is significantly more efficient than non-filtering search methods, due to the way entities are stored
	///
	/// **<u>NOTE:</u>** The origin entity will be automatically excluded from the search and will not be passed to the predicate
	///
	/// @param origin   	The entity to center the search on
	/// @param radius 		The radius to search within from the center
	/// @param minimumClass The minimum entity class type to search for
	/// @param predicate	A predicate to apply to determine eligible entities to return
	/// @param <T> 			The class in which all checked entities should be or extend. More specific types are more efficient
	/// @return         	An optional containing the closest entity to the origin entity, or an empty optional if none found
	public static <T extends Entity> Optional<T> getNearestEntity(Entity origin, double radius, Class<T> minimumClass, Predicate<? super T> predicate) {
		return getNearestEntity(origin, radius, radius, radius, minimumClass, predicate);
	}

	/// Get the closest [Entity] to an origin entity, only including entities within a radius of that entity, filtering by entity class and a [Predicate].<br/>
	/// This is significantly more efficient than non-filtering search methods, due to the way entities are stored
	///
	/// **<u>NOTE:</u>** The origin entity will be automatically excluded from the search and will not be passed to the predicate
	///
	/// @param origin   	The entity to center the search on
	/// @param radiusX 		The radius to search within from the center, on the X axis
	/// @param radiusY 		The radius to search within from the center, on the Y axis
	/// @param radiusZ 		The radius to search within from the center, on the Z axis
	/// @param minimumClass The minimum entity class type to search for
	/// @param predicate	A predicate to apply to determine eligible entities to return
	/// @param <T> 			The class in which all checked entities should be or extend. More specific types are more efficient
	/// @return         	An optional containing the closest entity to the origin entity, or an empty optional if none found
	public static <T extends Entity> Optional<T> getNearestEntity(Entity origin, double radiusX, double radiusY, double radiusZ, Class<T> minimumClass, Predicate<? super T> predicate) {
		return getNearestEntity(origin.level(), origin.getBoundingBox().inflate(radiusX, radiusY, radiusZ), origin.position(), minimumClass,
								((Predicate<T>)entity -> entity != origin).and(predicate));
	}

	/// Get the closest [Entity] to an origin point, only including entities within a radius of that point, filtering by entity class and a [Predicate].<br/>
	/// This is significantly more efficient than non-filtering search methods, due to the way entities are stored
	///
	/// @param level   		The level in which to search for entities
	/// @param origin   	The center location to search around
	/// @param radius 		The radius to search within from the center
	/// @param minimumClass The minimum entity class type to search for
	/// @param predicate	A predicate to apply to determine eligible entities to return
	/// @param <T> 			The class in which all checked entities should be or extend. More specific types are more efficient
	/// @return         	An optional containing the closest entity to the origin point, or an empty optional if none found
	public static <T extends Entity> Optional<T> getNearestEntity(Level level, Vec3 origin, double radius, Class<T> minimumClass, Predicate<? super T> predicate) {
		return getNearestEntity(level, AABB.ofSize(origin, radius, radius, radius), origin, minimumClass, predicate);
	}

	/// Get the closest [Entity] to an origin point, only including entities within a radius of that point, filtering by entity class and a [Predicate].<br/>
	/// This is significantly more efficient than non-filtering search methods, due to the way entities are stored
	///
	/// @param level   		The level in which to search for entities
	/// @param origin   	The center location to search around
	/// @param radiusX 		The radius to search within from the center, on the X axis
	/// @param radiusY 		The radius to search within from the center, on the Y axis
	/// @param radiusZ 		The radius to search within from the center, on the Z axis
	/// @param minimumClass The minimum entity class type to search for
	/// @param predicate	A predicate to apply to determine eligible entities to return
	/// @param <T> 			The class in which all checked entities should be or extend. More specific types are more efficient
	/// @return         	An optional containing the closest entity to the origin point, or an empty optional if none found
	public static <T extends Entity> Optional<T> getNearestEntity(Level level, Vec3 origin, double radiusX, double radiusY, double radiusZ, Class<T> minimumClass, Predicate<? super T> predicate) {
		return getNearestEntity(level, AABB.ofSize(origin, radiusX, radiusY, radiusZ), origin, minimumClass, predicate);
	}

	/// Get the closest [Entity] to an origin point, only including entities within a specified area, filtering by a [Predicate]
	///
	/// @param level   		The level in which to search for entities
	/// @param bounds   	The region in which to search
	/// @param origin   	The center location to search around
	/// @param minimumClass The minimum entity class type to search for
	/// @param predicate	A predicate to apply to determine eligible entities to return
	/// @param <T> 			The class in which all checked entities should be or extend. More specific types are more efficient
	/// @return         	An optional containing the closest entity to the origin point, or an empty optional if none found
	public static <T extends Entity> Optional<T> getNearestEntity(Level level, AABB bounds, Vec3 origin, Class<T> minimumClass, Predicate<? super T> predicate) {
		final MutableDouble dist = new MutableDouble(Double.MAX_VALUE);
		final MutableObject<@Nullable T> closest = new MutableObject<>(null);
		final EntityTypeTest<Entity, T> typeTest = makeEntityTypeTest(minimumClass);

		level.getEntities().get(typeTest, bounds, entity -> {
			if (isEntityInBounds(entity, bounds) && predicate.test(entity)) {
				double entityDist = entity.distanceToSqr(origin);

				if (entityDist < dist.doubleValue()) {
					dist.setValue(entityDist);
					closest.setValue(entity);
				}
			}

			return AbortableIterationConsumer.Continuation.CONTINUE;
		});

		return Optional.ofNullable(closest.get());
	}

	/// Get the closest [Player] to a given entity, only including players within a radius.<br/>
	/// This is significantly more efficient than non-player methods when looking for players,
	/// as it only searches the list of players rather than the pool of entities
	///
	/// **<u>NOTE:</u>** The origin entity will be automatically excluded from the search if it is a player
	///
	/// @param origin   	The entity to center the search on
	/// @param radius 		The radius to search within from the center
	/// @return         	An optional containing the closest player to the origin entity, or an empty optional if none found
	public static Optional<Player> getNearestPlayer(Entity origin, double radius) {
		return getNearestPlayer(origin, radius, radius, radius);
	}

	/// Get the closest [Player] to a given entity, only including players within a radius.<br/>
	/// This is significantly more efficient than non-player methods when looking for players,
	/// as it only searches the list of players rather than the pool of entities
	///
	/// **<u>NOTE:</u>** The origin entity will be automatically excluded from the search if it is a player
	///
	/// @param origin   	The entity to center the search on
	/// @param radiusX 		The radius to search within from the center, on the X axis
	/// @param radiusY 		The radius to search within from the center, on the Y axis
	/// @param radiusZ 		The radius to search within from the center, on the Z axis
	/// @return         	An optional containing the closest player to the origin entity, or an empty optional if none found
	public static Optional<Player> getNearestPlayer(Entity origin, double radiusX, double radiusY, double radiusZ) {
		return getNearestPlayer(origin.level(), origin.getBoundingBox().inflate(radiusX, radiusY, radiusZ), origin.position(), entity -> entity != origin);
	}

	/// Get the closest [Player] to a given entity, only including players within a radius.<br/>
	/// This is significantly more efficient than non-player methods when looking for players,
	/// as it only searches the list of players rather than the pool of entities
	///
	/// @param level   		The level in which to search for players
	/// @param origin   	The center location to search around
	/// @param radius 		The radius to search within from the center
	/// @return         	An optional containing the closest player to the origin point, or an empty optional if none found
	public static Optional<Player> getNearestPlayer(Level level, Vec3 origin, double radius) {
		return getNearestPlayer(level, origin, radius, radius, radius);
	}

	/// Get the closest [Player] to a given entity, only including players within a radius.<br/>
	/// This is significantly more efficient than non-player methods when looking for players,
	/// as it only searches the list of players rather than the pool of entities
	///
	/// @param level   		The level in which to search for players
	/// @param origin   	The center location to search around
	/// @param radiusX 		The radius to search within from the center, on the X axis
	/// @param radiusY 		The radius to search within from the center, on the Y axis
	/// @param radiusZ 		The radius to search within from the center, on the Z axis
	/// @return         	An optional containing the closest player to the origin point, or an empty optional if none found
	public static Optional<Player> getNearestPlayer(Level level, Vec3 origin, double radiusX, double radiusY, double radiusZ) {
		return getNearestPlayer(level, AABB.ofSize(origin, radiusX, radiusY, radiusZ), origin);
	}

	/// Get the closest [Player] to an origin point, only including players within a specified area.<br/>
	/// This is significantly more efficient than non-player methods when looking for players,
	/// as it only searches the list of players rather than the pool of entities
	///
	/// @param level   		The level in which to search for players
	/// @param bounds   	The region in which to search
	/// @param origin   	The center location to search around
	/// @return         	An optional containing the closest player to the origin point, or an empty optional if none found
	public static Optional<Player> getNearestPlayer(Level level, AABB bounds, Vec3 origin) {
		return getNearestPlayer(level, bounds, origin, _ -> true);
	}

	/// Get the closest [Player] to a given entity, only including players within a radius, filtering by a [Predicate].<br/>
	/// This is significantly more efficient than non-player methods when looking for players,
	/// as it only searches the list of players rather than the pool of entities
	///
	/// **<u>NOTE:</u>** The origin entity will be automatically excluded from the search if it is a player
	///
	/// @param origin   	The entity to center the search on
	/// @param radius 		The radius to search within from the center
	/// @param predicate	A predicate to apply to determine eligible players to return
	/// @return         	An optional containing the closest player to the origin entity, or an empty optional if none found
	public static Optional<Player> getNearestPlayer(Entity origin, double radius, Predicate<Player> predicate) {
		return getNearestPlayer(origin, radius, radius, radius, predicate);
	}

	/// Get the closest [Player] to a given entity, only including players within a radius, filtering by a [Predicate].<br/>
	/// This is significantly more efficient than non-player methods when looking for players,
	/// as it only searches the list of players rather than the pool of entities
	///
	/// **<u>NOTE:</u>** The origin entity will be automatically excluded from the search if it is a player
	///
	/// @param origin   	The entity to center the search on
	/// @param radiusX 		The radius to search within from the center, on the X axis
	/// @param radiusY 		The radius to search within from the center, on the Y axis
	/// @param radiusZ 		The radius to search within from the center, on the Z axis
	/// @param predicate	A predicate to apply to determine eligible players to return
	/// @return         	An optional containing the closest player to the origin entity, or an empty optional if none found
	public static Optional<Player> getNearestPlayer(Entity origin, double radiusX, double radiusY, double radiusZ, Predicate<Player> predicate) {
		return getNearestPlayer(origin.level(), origin.getBoundingBox().inflate(radiusX, radiusY, radiusZ), origin.position(),
								((Predicate<Player>)entity -> entity != origin).and(predicate));
	}

	/// Get the closest [Player] to an origin point, only including players within a radius, filtering by a [Predicate].<br/>
	/// This is significantly more efficient than non-player methods when looking for players,
	/// as it only searches the list of players rather than the pool of entities
	///
	/// @param level   		The level in which to search for players
	/// @param origin   	The center location to search around
	/// @param radius 		The radius to search within from the center
	/// @param predicate	A predicate to apply to determine eligible players to return
	/// @return         	An optional containing the closest player to the origin point, or an empty optional if none found
	public static Optional<Player> getNearestPlayer(Level level, Vec3 origin, double radius, Predicate<Player> predicate) {
		return getNearestPlayer(level, origin, radius, radius, radius, predicate);
	}

	/// Get the closest [Player] to an origin point, only including players within a radius, filtering by a [Predicate].<br/>
	/// This is significantly more efficient than non-player methods when looking for players,
	/// as it only searches the list of players rather than the pool of entities
	///
	/// @param level   		The level in which to search for players
	/// @param origin   	The center location to search around
	/// @param radiusX 		The radius to search within from the center, on the X axis
	/// @param radiusY 		The radius to search within from the center, on the Y axis
	/// @param radiusZ 		The radius to search within from the center, on the Z axis
	/// @param predicate	A predicate to apply to determine eligible players to return
	/// @return         	An optional containing the closest player to the origin point, or an empty optional if none found
	public static Optional<Player> getNearestPlayer(Level level, Vec3 origin, double radiusX, double radiusY, double radiusZ, Predicate<Player> predicate) {
		return getNearestPlayer(level, AABB.ofSize(origin, radiusX, radiusY, radiusZ), origin, predicate);
	}

	/// Get the closest [Player] to an origin point, only including players within a specified area, filtering by a [Predicate].<br/>
	/// This is significantly more efficient than non-player methods when looking for players,
	/// as it only searches the list of players rather than the pool of entities
	///
	/// @param level   		The level in which to search for players
	/// @param bounds   	The region in which to search
	/// @param origin   	The center location to search around
	/// @param predicate	A predicate to apply to determine eligible players to return
	/// @return         	An optional containing the closest player to the origin point, or an empty optional if none found
	public static Optional<Player> getNearestPlayer(Level level, AABB bounds, Vec3 origin, Predicate<Player> predicate) {
		double dist = Double.MAX_VALUE;
		Player closest = null;

		for (Player player : level.players()) {
			if (isEntityInBounds(player, bounds) && predicate.test(player)) {
				final double playerDist = player.distanceToSqr(origin);

				if (playerDist < dist) {
					dist = playerDist;
					closest = player;
				}
			}
		}

		return Optional.ofNullable(closest);
	}

	/// Get the closest [ServerPlayer] to an origin entity, only including players within a radius of that entity.<br/>
	/// This is significantly more efficient than non-player methods when looking for players,
	/// as it only searches the list of players rather than the pool of entities
	///
	/// **<u>NOTE:</u>** The origin entity will be automatically excluded from the search if it is a player
	/// **<u>WARNING:</u>** This will throw an exception if called on the client-side
	///
	/// @param origin   	The entity to center the search on
	/// @param radius 		The radius to search within from the center
	/// @return         	An optional containing the closest player to the origin entity, or an empty optional if none found
	public static Optional<ServerPlayer> getNearestServerPlayer(Entity origin, double radius) {
		return getNearestServerPlayer(origin, radius, radius, radius);
	}

	/// Get the closest [ServerPlayer] to an origin entity, only including players within a radius of that entity.<br/>
	/// This is significantly more efficient than non-player methods when looking for players,
	/// as it only searches the list of players rather than the pool of entities
	///
	/// **<u>NOTE:</u>** The origin entity will be automatically excluded from the search if it is a player
	/// **<u>WARNING:</u>** This will throw an exception if called on the client-side
	///
	/// @param origin   	The entity to center the search on
	/// @param radiusX 		The radius to search within from the center, on the X axis
	/// @param radiusY 		The radius to search within from the center, on the Y axis
	/// @param radiusZ 		The radius to search within from the center, on the Z axis
	/// @return         	An optional containing the closest player to the origin entity, or an empty optional if none found
	public static Optional<ServerPlayer> getNearestServerPlayer(Entity origin, double radiusX, double radiusY, double radiusZ) {
		return getNearestServerPlayer((ServerLevel)origin.level(), origin.getBoundingBox().inflate(radiusX, radiusY, radiusZ), origin.position(), entity -> entity != origin);
	}

	/// Get the closest [ServerPlayer] to an origin point, only including players within a radius of that point.<br/>
	/// This is significantly more efficient than non-player methods when looking for players,
	/// as it only searches the list of players rather than the pool of entities
	///
	/// @param level   		The level in which to search for players
	/// @param origin   	The center location to search around
	/// @param radius 		The radius to search within from the center
	/// @return         	An optional containing the closest player to the origin point, or an empty optional if none found
	public static Optional<ServerPlayer> getNearestServerPlayer(ServerLevel level, Vec3 origin, double radius) {
		return getNearestServerPlayer(level, origin, radius, radius, radius);
	}

	/// Get the closest [ServerPlayer] to an origin point, only including players within a radius of that point.<br/>
	/// This is significantly more efficient than non-player methods when looking for players,
	/// as it only searches the list of players rather than the pool of entities
	///
	/// @param level   		The level in which to search for players
	/// @param origin   	The center location to search around
	/// @param radiusX 		The radius to search within from the center, on the X axis
	/// @param radiusY 		The radius to search within from the center, on the Y axis
	/// @param radiusZ 		The radius to search within from the center, on the Z axis
	/// @return         	An optional containing the closest player to the origin point, or an empty optional if none found
	public static Optional<ServerPlayer> getNearestServerPlayer(ServerLevel level, Vec3 origin, double radiusX, double radiusY, double radiusZ) {
		return getNearestServerPlayer(level, AABB.ofSize(origin, radiusX, radiusY, radiusZ), origin);
	}

	/// Get the closest [ServerPlayer] to an origin point, only including players within a specified area.<br/>
	/// This is significantly more efficient than non-player methods when looking for players,
	/// as it only searches the list of players rather than the pool of entities
	///
	/// @param level   		The level in which to search for players
	/// @param bounds   	The region in which to search
	/// @param origin   	The center location to search around
	/// @return         	An optional containing the closest player to the origin point, or an empty optional if none found
	public static Optional<ServerPlayer> getNearestServerPlayer(ServerLevel level, AABB bounds, Vec3 origin) {
		return getNearestServerPlayer(level, bounds, origin, _ -> true);
	}

	/// Get the closest [ServerPlayer] to an origin entity, only including players within a radius of that entity, filtering by [Predicate].<br/>
	/// This is significantly more efficient than non-player methods when looking for players,
	/// as it only searches the list of players rather than the pool of entities
	///
	/// **<u>NOTE:</u>** The origin entity will be automatically excluded from the search if it is a player
	/// **<u>WARNING:</u>** This will throw an exception if called on the client-side
	///
	/// @param origin   	The entity to center the search on
	/// @param radius 		The radius to search within from the center
	/// @param predicate	A predicate to apply to determine eligible players to return
	/// @return         	An optional containing the closest player to the origin entity, or an empty optional if none found
	public static Optional<ServerPlayer> getNearestServerPlayer(Entity origin, double radius, Predicate<ServerPlayer> predicate) {
		return getNearestServerPlayer(origin, radius, radius, radius, predicate);
	}

	/// Get the closest [ServerPlayer] to an origin entity, only including players within a radius of that entity, filtering by [Predicate].<br/>
	/// This is significantly more efficient than non-player methods when looking for players,
	/// as it only searches the list of players rather than the pool of entities
	///
	/// **<u>NOTE:</u>** The origin entity will be automatically excluded from the search if it is a player
	/// **<u>WARNING:</u>** This will throw an exception if called on the client-side
	///
	/// @param origin   	The entity to center the search on
	/// @param radiusX 		The radius to search within from the center, on the X axis
	/// @param radiusY 		The radius to search within from the center, on the Y axis
	/// @param radiusZ 		The radius to search within from the center, on the Z axis
	/// @param predicate	A predicate to apply to determine eligible players to return
	/// @return         	An optional containing the closest player to the origin entity, or an empty optional if none found
	public static Optional<ServerPlayer> getNearestServerPlayer(Entity origin, double radiusX, double radiusY, double radiusZ, Predicate<ServerPlayer> predicate) {
		return getNearestServerPlayer((ServerLevel)origin.level(), origin.getBoundingBox().inflate(radiusX, radiusY, radiusZ), origin.position(),
									  ((Predicate<ServerPlayer>)entity -> entity != origin).and(predicate));
	}

	/// Get the closest [ServerPlayer] to an origin point, only including players within a radius of that point, filtering by [Predicate].<br/>
	/// This is significantly more efficient than non-player methods when looking for players,
	/// as it only searches the list of players rather than the pool of entities
	///
	/// @param level   		The level in which to search for players
	/// @param origin   	The center location to search around
	/// @param radius 		The radius to search within from the center
	/// @param predicate	A predicate to apply to determine eligible players to return
	/// @return         	An optional containing the closest player to the origin point, or an empty optional if none found
	public static Optional<ServerPlayer> getNearestServerPlayer(ServerLevel level, Vec3 origin, double radius, Predicate<ServerPlayer> predicate) {
		return getNearestServerPlayer(level, origin, radius, radius, radius, predicate);
	}

	/// Get the closest [ServerPlayer] to an origin point, only including players within a radius of that point, filtering by [Predicate].<br/>
	/// This is significantly more efficient than non-player methods when looking for players,
	/// as it only searches the list of players rather than the pool of entities
	///
	/// @param level   		The level in which to search for players
	/// @param origin   	The center location to search around
	/// @param radiusX 		The radius to search within from the center, on the X axis
	/// @param radiusY 		The radius to search within from the center, on the Y axis
	/// @param radiusZ 		The radius to search within from the center, on the Z axis
	/// @param predicate	A predicate to apply to determine eligible players to return
	/// @return         	An optional containing the closest player to the origin point, or an empty optional if none found
	public static Optional<ServerPlayer> getNearestServerPlayer(ServerLevel level, Vec3 origin, double radiusX, double radiusY, double radiusZ, Predicate<ServerPlayer> predicate) {
		return getNearestServerPlayer(level, AABB.ofSize(origin, radiusX, radiusY, radiusZ), origin, predicate);
	}

	/// Get the closest [ServerPlayer] to an origin point, only including players within a specified area, filtering by [Predicate].<br/>
	/// This is significantly more efficient than non-player methods when looking for players,
	/// as it only searches the list of players rather than the pool of entities
	///
	/// @param level   		The level in which to search for players
	/// @param bounds   	The region in which to search
	/// @param origin   	The center location to search around
	/// @param predicate	A predicate to apply to determine eligible players to return
	/// @return         	An optional containing the closest player to the origin point, or an empty optional if none found
	public static Optional<ServerPlayer> getNearestServerPlayer(ServerLevel level, AABB bounds, Vec3 origin, Predicate<ServerPlayer> predicate) {
		double dist = Double.MAX_VALUE;
		ServerPlayer closest = null;

		for (ServerPlayer player : level.players()) {
			if (isEntityInBounds(player, bounds) && predicate.test(player)) {
				final double playerDist = player.distanceToSqr(origin);

				if (playerDist < dist) {
					dist = playerDist;
					closest = player;
				}
			}
		}

		return Optional.ofNullable(closest);
	}

	/// Get all [Player]s within a radius of an origin entity.<br/>
	/// This is significantly more efficient than non-player methods when looking for players,
	/// as it only searches the list of players rather than the pool of entities
	///
	/// **<u>NOTE:</u>** The origin entity will be automatically excluded from the search if it is a player
	///
	/// @param origin   	The entity to center the search on
	/// @param radius 		The radius to search within from the center
	/// @return         	The list of all players within a radius of the origin entity
	public static List<Player> getPlayers(Entity origin, double radius) {
		return getPlayers(origin, radius, radius, radius);
	}

	/// Get all [Player]s within a radius of an origin entity.<br/>
	/// This is significantly more efficient than non-player methods when looking for players,
	/// as it only searches the list of players rather than the pool of entities
	///
	/// **<u>NOTE:</u>** The origin entity will be automatically excluded from the search if it is a player
	///
	/// @param origin   	The entity to center the search on
	/// @param radiusX 		The radius to search within from the center, on the X axis
	/// @param radiusY 		The radius to search within from the center, on the Y axis
	/// @param radiusZ 		The radius to search within from the center, on the Z axis
	/// @return         	The list of all players within a radius of the origin entity
	public static List<Player> getPlayers(Entity origin, double radiusX, double radiusY, double radiusZ) {
		return getPlayers(origin.level(), origin.getBoundingBox().inflate(radiusX, radiusY, radiusZ), entity -> entity != origin);
	}

	/// Get all [Player]s within a radius of an origin point.<br/>
	/// This is significantly more efficient than non-player methods when looking for players,
	/// as it only searches the list of players rather than the pool of entities
	///
	/// @param level   		The level in which to search for players
	/// @param origin   	The center location to search around
	/// @param radius 		The radius to search within from the center
	/// @return         	The list of all players within a radius of the origin point
	public static List<Player> getPlayers(Level level, Vec3 origin, double radius) {
		return getPlayers(level, origin, radius, radius, radius);
	}

	/// Get all [Player]s within a radius of an origin point.<br/>
	/// This is significantly more efficient than non-player methods when looking for players,
	/// as it only searches the list of players rather than the pool of entities
	///
	/// @param level   		The level in which to search for players
	/// @param origin   	The center location to search around
	/// @param radiusX 		The radius to search within from the center, on the X axis
	/// @param radiusY 		The radius to search within from the center, on the Y axis
	/// @param radiusZ 		The radius to search within from the center, on the Z axis
	/// @return         	The list of all players within a radius of the origin point
	public static List<Player> getPlayers(Level level, Vec3 origin, double radiusX, double radiusY, double radiusZ) {
		return getPlayers(level, AABB.ofSize(origin, radiusX, radiusY, radiusZ));
	}

	/// Get all [Player]s within a specified area<br/>
	/// This is significantly more efficient than non-player methods when looking for players,
	/// as it only searches the list of players rather than the pool of entities
	///
	/// @param level   		The level in which to search for players
	/// @param bounds   	The region in which to search
	/// @return         	The list of all players within an area
	public static List<Player> getPlayers(Level level, AABB bounds) {
		return getPlayers(level, bounds, _ -> true);
	}

	/// Get all [Player]s within a radius of an origin entity, filtering by a [Predicate].<br/>
	/// This is significantly more efficient than non-player methods when looking for players,
	/// as it only searches the list of players rather than the pool of entities
	///
	/// **<u>NOTE:</u>** The origin entity will be automatically excluded from the search if it is a player
	///
	/// @param origin   	The entity to center the search on
	/// @param radius 		The radius to search within from the center
	/// @param predicate	A predicate to apply to determine eligible players to return
	/// @return         	The list of all players within an area
	public static List<Player> getPlayers(Entity origin, double radius, Predicate<Player> predicate) {
		return getPlayers(origin, radius, radius, radius, predicate);
	}

	/// Get all [Player]s within a radius of an origin entity, filtering by a [Predicate].<br/>
	/// This is significantly more efficient than non-player methods when looking for players,
	/// as it only searches the list of players rather than the pool of entities
	///
	/// **<u>NOTE:</u>** The origin entity will be automatically excluded from the search if it is a player
	///
	/// @param origin   	The entity to center the search on
	/// @param radiusX 		The radius to search within from the center, on the X axis
	/// @param radiusY 		The radius to search within from the center, on the Y axis
	/// @param radiusZ 		The radius to search within from the center, on the Z axis
	/// @param predicate	A predicate to apply to determine eligible players to return
	/// @return         	The list of all players within an area
	public static List<Player> getPlayers(Entity origin, double radiusX, double radiusY, double radiusZ, Predicate<Player> predicate) {
		return getPlayers(origin.level(), origin.getBoundingBox().inflate(radiusX, radiusY, radiusZ),
						  ((Predicate<Player>)entity -> entity != origin).and(predicate));
	}

	/// Get all [Player]s within a radius of an origin entity, filtering by a [Predicate].<br/>
	/// This is significantly more efficient than non-player methods when looking for players,
	/// as it only searches the list of players rather than the pool of entities
	///
	/// @param level   		The level in which to search for players
	/// @param origin   	The center location to search around
	/// @param radius 		The radius to search within from the center
	/// @param predicate	A predicate to apply to determine eligible players to return
	/// @return         	The list of all players within an area
	public static List<Player> getPlayers(Level level, Vec3 origin, double radius, Predicate<Player> predicate) {
		return getPlayers(level, origin, radius, radius, radius, predicate);
	}

	/// Get all [Player]s within a specified area, filtering by a [Predicate].<br/>
	/// This is significantly more efficient than non-player methods when looking for players,
	/// as it only searches the list of players rather than the pool of entities
	///
	/// @param level   		The level in which to search for players
	/// @param origin   	The center location to search around
	/// @param radiusX 		The radius to search within from the center, on the X axis
	/// @param radiusY 		The radius to search within from the center, on the Y axis
	/// @param radiusZ 		The radius to search within from the center, on the Z axis
	/// @param predicate	A predicate to apply to determine eligible players to return
	/// @return         	The list of all players within an area
	public static List<Player> getPlayers(Level level, Vec3 origin, double radiusX, double radiusY, double radiusZ, Predicate<Player> predicate) {
		return getPlayers(level, AABB.ofSize(origin, radiusX, radiusY, radiusZ), predicate);
	}

	/// Get all [Player]s within a specified area, filtering by a [Predicate].<br/>
	/// This is significantly more efficient than non-player methods when looking for players,
	/// as it only searches the list of players rather than the pool of entities
	///
	/// @param level   		The level in which to search for players
	/// @param bounds   	The region in which to search
	/// @param predicate	A predicate to apply to determine eligible players to return
	/// @return         	The list of all players within an area
	public static List<Player> getPlayers(Level level, AABB bounds, Predicate<Player> predicate) {
		final List<Player> players = new ReferenceArrayList<>();

		for (Player player : level.players()) {
			if (isEntityInBounds(player, bounds) && predicate.test(player))
				players.add(player);
		}

		return players;
	}

	/// Get all [ServerPlayer]s within a radius of an origin entity.<br/>
	/// This is significantly more efficient than non-player methods when looking for players,
	/// as it only searches the list of players rather than the pool of entities
	///
	/// **<u>NOTE:</u>** The origin entity will be automatically excluded from the search if it is a player
	/// **<u>WARNING:</u>** This will throw an exception if called on the client-side
	///
	/// @param origin   	The entity to center the search on
	/// @param radius 		The radius to search within from the center
	/// @return         	The list of all players within a radius of the origin entity
	public static List<ServerPlayer> getServerPlayers(Entity origin, double radius) {
		return getServerPlayers(origin, radius, radius, radius);
	}

	/// Get all [ServerPlayer]s within a radius of an origin entity.<br/>
	/// This is significantly more efficient than non-player methods when looking for players,
	/// as it only searches the list of players rather than the pool of entities
	///
	/// **<u>NOTE:</u>** The origin entity will be automatically excluded from the search if it is a player
	/// **<u>WARNING:</u>** This will throw an exception if called on the client-side
	///
	/// @param origin   	The entity to center the search on
	/// @param radiusX 		The radius to search within from the center, on the X axis
	/// @param radiusY 		The radius to search within from the center, on the Y axis
	/// @param radiusZ 		The radius to search within from the center, on the Z axis
	/// @return         	The list of all players within a radius of the origin entity
	public static List<ServerPlayer> getServerPlayers(Entity origin, double radiusX, double radiusY, double radiusZ) {
		return getServerPlayers((ServerLevel)origin.level(), origin.getBoundingBox().inflate(radiusX, radiusY, radiusZ), entity -> entity != origin);
	}

	/// Get all [ServerPlayer]s within a radius of an origin point.<br/>
	/// This is significantly more efficient than non-player methods when looking for players,
	/// as it only searches the list of players rather than the pool of entities
	///
	/// @param level   		The level in which to search for players
	/// @param origin   	The center location to search around
	/// @param radius 		The radius to search within from the center
	/// @return         	The list of all players within a radius of the origin point
	public static List<ServerPlayer> getServerPlayers(ServerLevel level, Vec3 origin, double radius) {
		return getServerPlayers(level, origin, radius, radius, radius);
	}

	/// Get all [ServerPlayer]s within a radius of an origin point.<br/>
	/// This is significantly more efficient than non-player methods when looking for players,
	/// as it only searches the list of players rather than the pool of entities
	///
	/// @param level   		The level in which to search for players
	/// @param origin   	The center location to search around
	/// @param radiusX 		The radius to search within from the center, on the X axis
	/// @param radiusY 		The radius to search within from the center, on the Y axis
	/// @param radiusZ 		The radius to search within from the center, on the Z axis
	/// @return         	The list of all players within a radius of the origin point
	public static List<ServerPlayer> getServerPlayers(ServerLevel level, Vec3 origin, double radiusX, double radiusY, double radiusZ) {
		return getServerPlayers(level, AABB.ofSize(origin, radiusX, radiusY, radiusZ));
	}

	/// Get all [ServerPlayer]s within a specified area<br/>
	/// This is significantly more efficient than non-player methods when looking for players,
	/// as it only searches the list of players rather than the pool of entities
	///
	/// @param level   		The level in which to search for players
	/// @param bounds   	The region in which to search
	/// @return         	The list of all players within an area
	public static List<ServerPlayer> getServerPlayers(ServerLevel level, AABB bounds) {
		return getServerPlayers(level, bounds, _ -> true);
	}

	/// Get all [ServerPlayer]s within a radius of an origin entity, filtering by a [Predicate].<br/>
	/// This is significantly more efficient than non-player methods when looking for players,
	/// as it only searches the list of players rather than the pool of entities
	///
	/// **<u>NOTE:</u>** The origin entity will be automatically excluded from the search if it is a player
	/// **<u>WARNING:</u>** This will throw an exception if called on the client-side
	///
	/// @param origin   	The entity to center the search on
	/// @param radius 		The radius to search within from the center
	/// @param predicate	A predicate to apply to determine eligible players to return
	/// @return         	The list of all players within an area
	public static List<ServerPlayer> getServerPlayers(Entity origin, double radius, Predicate<ServerPlayer> predicate) {
		return getServerPlayers(origin, radius, radius, radius, predicate);
	}

	/// Get all [ServerPlayer]s within a radius of an origin entity, filtering by a [Predicate].<br/>
	/// This is significantly more efficient than non-player methods when looking for players,
	/// as it only searches the list of players rather than the pool of entities
	///
	/// **<u>NOTE:</u>** The origin entity will be automatically excluded from the search if it is a player
	/// **<u>WARNING:</u>** This will throw an exception if called on the client-side
	///
	/// @param origin   	The entity to center the search on
	/// @param radiusX 		The radius to search within from the center, on the X axis
	/// @param radiusY 		The radius to search within from the center, on the Y axis
	/// @param radiusZ 		The radius to search within from the center, on the Z axis
	/// @param predicate	A predicate to apply to determine eligible players to return
	/// @return         	The list of all players within an area
	public static List<ServerPlayer> getServerPlayers(Entity origin, double radiusX, double radiusY, double radiusZ, Predicate<ServerPlayer> predicate) {
		return getServerPlayers((ServerLevel)origin.level(), origin.getBoundingBox().inflate(radiusX, radiusY, radiusZ),
								((Predicate<ServerPlayer>)entity -> entity != origin).and(predicate));
	}

	/// Get all [ServerPlayer]s within a radius of an origin entity, filtering by a [Predicate].<br/>
	/// This is significantly more efficient than non-player methods when looking for players,
	/// as it only searches the list of players rather than the pool of entities
	///
	/// @param level   		The level in which to search for players
	/// @param origin   	The center location to search around
	/// @param radius 		The radius to search within from the center
	/// @param predicate	A predicate to apply to determine eligible players to return
	/// @return         	The list of all players within an area
	public static List<ServerPlayer> getServerPlayers(ServerLevel level, Vec3 origin, double radius, Predicate<ServerPlayer> predicate) {
		return getServerPlayers(level, origin, radius, radius, radius, predicate);
	}

	/// Get all [ServerPlayer]s within a radius of an origin entity, filtering by a [Predicate].<br/>
	/// This is significantly more efficient than non-player methods when looking for players,
	/// as it only searches the list of players rather than the pool of entities
	///
	/// @param level   		The level in which to search for players
	/// @param origin   	The center location to search around
	/// @param radiusX 		The radius to search within from the center, on the X axis
	/// @param radiusY 		The radius to search within from the center, on the Y axis
	/// @param radiusZ 		The radius to search within from the center, on the Z axis
	/// @param predicate	A predicate to apply to determine eligible players to return
	/// @return         	The list of all players within an area
	public static List<ServerPlayer> getServerPlayers(ServerLevel level, Vec3 origin, double radiusX, double radiusY, double radiusZ, Predicate<ServerPlayer> predicate) {
		return getServerPlayers(level, AABB.ofSize(origin, radiusX, radiusY, radiusZ), predicate);
	}

	/// Get all [ServerPlayer]s within a radius of an origin entity, filtering by a [Predicate].<br/>
	/// This is significantly more efficient than non-player methods when looking for players,
	/// as it only searches the list of players rather than the pool of entities
	///
	/// @param level   		The level in which to search for players
	/// @param bounds   	The region in which to search
	/// @param predicate	A predicate to apply to determine eligible players to return
	/// @return         	The list of all players within an area
	public static List<ServerPlayer> getServerPlayers(ServerLevel level, AABB bounds, Predicate<ServerPlayer> predicate) {
		final List<ServerPlayer> players = new ReferenceArrayList<>();

		for (ServerPlayer player : level.players()) {
			if (isEntityInBounds(player, bounds) && predicate.test(player))
				players.add(player);
		}

		return players;
	}

	/// Get all [entities][Entity] within a radius of an origin entity
	///
	/// **<u>NOTE:</u>** The origin entity will be automatically excluded from the search
	///
	/// @param origin   	The entity to center the search on
	/// @param radius 		The radius to search within from the center
	/// @return         	The list of all entities within a radius of the origin entity
	public static List<Entity> getEntities(Entity origin, double radius) {
		return getEntities(origin, radius, radius, radius);
	}

	/// Get all [entities][Entity] within a radius of an origin entity
	///
	/// **<u>NOTE:</u>** The origin entity will be automatically excluded from the search
	///
	/// @param origin   	The entity to center the search on
	/// @param radius 		The radius to search within from the center
	/// @param max			The maximum number of entities to retrieve before returning
	/// @return         	The list of all entities within a radius of the origin entity
	public static List<Entity> getEntities(Entity origin, double radius, int max) {
		return getEntities(origin, radius, radius, radius, max);
	}

	/// Get all [entities][Entity] within a radius of an origin entity
	///
	/// **<u>NOTE:</u>** The origin entity will be automatically excluded from the search
	///
	/// @param origin   	The entity to center the search on
	/// @param radiusX 		The radius to search within from the center, on the X axis
	/// @param radiusY 		The radius to search within from the center, on the Y axis
	/// @param radiusZ 		The radius to search within from the center, on the Z axis
	/// @return         	The list of all entities within a radius of the origin entity
	public static List<Entity> getEntities(Entity origin, double radiusX, double radiusY, double radiusZ) {
		return getEntities(origin.level(), origin.getBoundingBox().inflate(radiusX, radiusY, radiusZ), entity -> entity != origin);
	}

	/// Get all [entities][Entity] within a radius of an origin entity
	///
	/// **<u>NOTE:</u>** The origin entity will be automatically excluded from the search
	///
	/// @param origin   	The entity to center the search on
	/// @param radiusX 		The radius to search within from the center, on the X axis
	/// @param radiusY 		The radius to search within from the center, on the Y axis
	/// @param radiusZ 		The radius to search within from the center, on the Z axis
	/// @param max			The maximum number of entities to retrieve before returning
	/// @return         	The list of all entities within a radius of the origin entity
	public static List<Entity> getEntities(Entity origin, double radiusX, double radiusY, double radiusZ, int max) {
		return getEntities(origin.level(), origin.getBoundingBox().inflate(radiusX, radiusY, radiusZ), max, entity -> entity != origin);
	}

	/// Get all [entities][Entity] within a radius of an origin point
	///
	/// @param level   		The level in which to search for entities
	/// @param origin   	The center location to search around
	/// @param radius 		The radius to search within from the center
	/// @return         	The list of all entities within a radius of the origin point
	public static List<Entity> getEntities(Level level, Vec3 origin, double radius) {
		return getEntities(level, AABB.ofSize(origin, radius, radius, radius));
	}

	/// Get all [entities][Entity] within a radius of an origin point
	///
	/// @param level   		The level in which to search for entities
	/// @param origin   	The center location to search around
	/// @param radius 		The radius to search within from the center
	/// @param max			The maximum number of entities to retrieve before returning
	/// @return         	The list of all entities within a radius of the origin point
	public static List<Entity> getEntities(Level level, Vec3 origin, double radius, int max) {
		return getEntities(level, AABB.ofSize(origin, radius, radius, radius), max);
	}

	/// Get all [entities][Entity] within a radius of an origin point
	///
	/// @param level   		The level in which to search for entities
	/// @param origin   	The center location to search around
	/// @param radiusX 		The radius to search within from the center, on the X axis
	/// @param radiusY 		The radius to search within from the center, on the Y axis
	/// @param radiusZ 		The radius to search within from the center, on the Z axis
	/// @return         	The list of all entities within a radius of the origin point
	public static List<Entity> getEntities(Level level, Vec3 origin, double radiusX, double radiusY, double radiusZ) {
		return getEntities(level, AABB.ofSize(origin, radiusX, radiusY, radiusZ));
	}

	/// Get all [entities][Entity] within a radius of an origin point
	///
	/// @param level   		The level in which to search for entities
	/// @param origin   	The center location to search around
	/// @param radiusX 		The radius to search within from the center, on the X axis
	/// @param radiusY 		The radius to search within from the center, on the Y axis
	/// @param radiusZ 		The radius to search within from the center, on the Z axis
	/// @param max			The maximum number of entities to retrieve before returning
	/// @return         	The list of all entities within a radius of the origin point
	public static List<Entity> getEntities(Level level, Vec3 origin, double radiusX, double radiusY, double radiusZ, int max) {
		return getEntities(level, AABB.ofSize(origin, radiusX, radiusY, radiusZ), max);
	}

	/// Get all [entities][Entity] within a specified area
	///
	/// @param level   		The level in which to search for players
	/// @param bounds   	The region in which to search
	/// @return         	The list of all entities within a radius of the origin point
	public static List<Entity> getEntities(Level level, AABB bounds) {
		return getEntities(level, bounds, Entity.class);
	}

	/// Get all [entities][Entity] within a specified area
	///
	/// @param level   		The level in which to search for players
	/// @param bounds   	The region in which to search
	/// @return         	The list of all entities within a radius of the origin point
	public static List<Entity> getEntities(Level level, AABB bounds, int max) {
		return getEntities(level, bounds, Entity.class, max);
	}

	/// Get all [entities][Entity] within a radius of an origin entity, filtering by entity class
	///
	/// **<u>NOTE:</u>** The origin entity will be automatically excluded from the search
	///
	/// @param origin   	The entity to center the search on
	/// @param radius 		The radius to search within from the center
	/// @param minimumClass The minimum entity class type to search for
	/// @param <T> 			The class in which all checked entities should be or extend. More specific types are more efficient
	/// @return         	The list of all entities within a radius of the origin point
	public static <T extends Entity> List<T> getEntities(Entity origin, double radius, Class<T> minimumClass) {
		return getEntities(origin, radius, radius, radius, minimumClass);
	}

	/// Get all [entities][Entity] within a radius of an origin entity, filtering by entity class
	///
	/// **<u>NOTE:</u>** The origin entity will be automatically excluded from the search
	///
	/// @param origin   	The entity to center the search on
	/// @param radius 		The radius to search within from the center
	/// @param minimumClass The minimum entity class type to search for
	/// @param max			The maximum number of entities to retrieve before returning
	/// @param <T> 			The class in which all checked entities should be or extend. More specific types are more efficient
	/// @return         	The list of all entities within a radius of the origin point
	public static <T extends Entity> List<T> getEntities(Entity origin, double radius, Class<T> minimumClass, int max) {
		return getEntities(origin, radius, radius, radius, minimumClass, max);
	}

	/// Get all [entities][Entity] within a radius of an origin entity, filtering by entity class
	///
	/// **<u>NOTE:</u>** The origin entity will be automatically excluded from the search
	///
	/// @param origin   	The entity to center the search on
	/// @param radiusX 		The radius to search within from the center, on the X axis
	/// @param radiusY 		The radius to search within from the center, on the Y axis
	/// @param radiusZ 		The radius to search within from the center, on the Z axis
	/// @param minimumClass The minimum entity class type to search for
	/// @param <T> 			The class in which all checked entities should be or extend. More specific types are more efficient
	/// @return         	The list of all entities within a radius of the origin point
	public static <T extends Entity> List<T> getEntities(Entity origin, double radiusX, double radiusY, double radiusZ, Class<T> minimumClass) {
		return getEntities(origin.level(), origin.getBoundingBox().inflate(radiusX, radiusY, radiusZ), minimumClass, entity -> entity != origin);
	}

	/// Get all [entities][Entity] within a radius of an origin entity, filtering by entity class
	///
	/// **<u>NOTE:</u>** The origin entity will be automatically excluded from the search
	///
	/// @param origin   	The entity to center the search on
	/// @param radiusX 		The radius to search within from the center, on the X axis
	/// @param radiusY 		The radius to search within from the center, on the Y axis
	/// @param radiusZ 		The radius to search within from the center, on the Z axis
	/// @param minimumClass The minimum entity class type to search for
	/// @param max			The maximum number of entities to retrieve before returning
	/// @param <T> 			The class in which all checked entities should be or extend. More specific types are more efficient
	/// @return         	The list of all entities within a radius of the origin point
	public static <T extends Entity> List<T> getEntities(Entity origin, double radiusX, double radiusY, double radiusZ, Class<T> minimumClass, int max) {
		return getEntities(origin.level(), origin.getBoundingBox().inflate(radiusX, radiusY, radiusZ), minimumClass, max, entity -> entity != origin);
	}

	/// Get all [entities][Entity] within a radius of an origin point, filtering by entity class
	///
	/// @param origin   	The entity to center the search on
	/// @param radius 		The radius to search within from the center
	/// @param minimumClass The minimum entity class type to search for
	/// @param <T> 			The class in which all checked entities should be or extend. More specific types are more efficient
	/// @return         	The list of all entities within a radius of the origin point
	public static <T extends Entity> List<T> getEntities(Level level, Vec3 origin, double radius, Class<T> minimumClass) {
		return getEntities(level, AABB.ofSize(origin, radius, radius, radius), minimumClass);
	}

	/// Get all [entities][Entity] within a radius of an origin point, filtering by entity class
	///
	/// @param origin   	The entity to center the search on
	/// @param radius 		The radius to search within from the center
	/// @param minimumClass The minimum entity class type to search for
	/// @param max			The maximum number of entities to retrieve before returning
	/// @param <T> 			The class in which all checked entities should be or extend. More specific types are more efficient
	/// @return         	The list of all entities within a radius of the origin point
	public static <T extends Entity> List<T> getEntities(Level level, Vec3 origin, double radius, Class<T> minimumClass, int max) {
		return getEntities(level, AABB.ofSize(origin, radius, radius, radius), minimumClass, max);
	}

	/// Get all [entities][Entity] within a radius of an origin point, filtering by entity class
	///
	/// @param origin   	The entity to center the search on
	/// @param radiusX 		The radius to search within from the center, on the X axis
	/// @param radiusY 		The radius to search within from the center, on the Y axis
	/// @param radiusZ 		The radius to search within from the center, on the Z axis
	/// @param minimumClass The minimum entity class type to search for
	/// @param <T> 			The class in which all checked entities should be or extend. More specific types are more efficient
	/// @return         	The list of all entities within a radius of the origin point
	public static <T extends Entity> List<T> getEntities(Level level, Vec3 origin, double radiusX, double radiusY, double radiusZ, Class<T> minimumClass) {
		return getEntities(level, AABB.ofSize(origin, radiusX, radiusY, radiusZ), minimumClass);
	}

	/// Get all [entities][Entity] within a radius of an origin point, filtering by entity class
	///
	/// @param origin   	The entity to center the search on
	/// @param radiusX 		The radius to search within from the center, on the X axis
	/// @param radiusY 		The radius to search within from the center, on the Y axis
	/// @param radiusZ 		The radius to search within from the center, on the Z axis
	/// @param minimumClass The minimum entity class type to search for
	/// @param max			The maximum number of entities to retrieve before returning
	/// @param <T> 			The class in which all checked entities should be or extend. More specific types are more efficient
	/// @return         	The list of all entities within a radius of the origin point
	public static <T extends Entity> List<T> getEntities(Level level, Vec3 origin, double radiusX, double radiusY, double radiusZ, Class<T> minimumClass, int max) {
		return getEntities(level, AABB.ofSize(origin, radiusX, radiusY, radiusZ), minimumClass, max);
	}

	/// Get all [entities][Entity] within a specified area, filtering by entity class
	///
	/// @param level   		The level in which to search for players
	/// @param bounds   	The region in which to search
	/// @param minimumClass The minimum entity class type to search for
	/// @param <T> 			The class in which all checked entities should be or extend. More specific types are more efficient
	/// @return         	The list of all entities within a radius of the origin point
	public static <T extends Entity> List<T> getEntities(Level level, AABB bounds, Class<T> minimumClass) {
		return getEntities(level, bounds, minimumClass, _ -> true);
	}

	/// Get all [entities][Entity] within a specified area, filtering by entity class
	///
	/// @param level   		The level in which to search for players
	/// @param bounds   	The region in which to search
	/// @param minimumClass The minimum entity class type to search for
	/// @param max			The maximum number of entities to retrieve before returning
	/// @param <T> 			The class in which all checked entities should be or extend. More specific types are more efficient
	/// @return         	The list of all entities within a radius of the origin point
	public static <T extends Entity> List<T> getEntities(Level level, AABB bounds, Class<T> minimumClass, int max) {
		return getEntities(level, bounds, minimumClass, max, _ -> true);
	}

	/// Get all [entities][Entity] within a radius of an origin entity, filtering by a [Predicate]
	///
	/// **<u>NOTE:</u>** The origin entity will be automatically excluded from the search
	///
	/// @param origin   	The entity to center the search on
	/// @param radius 		The radius to search within from the center
	/// @param predicate	A predicate to apply to determine eligible entities to return
	/// @return         	The list of all entities within a radius of the origin entity
	public static List<Entity> getEntities(Entity origin, double radius, Predicate<Entity> predicate) {
		return getEntities(origin, radius, radius, radius, predicate);
	}

	/// Get all [entities][Entity] within a radius of an origin entity, filtering by a [Predicate]
	///
	/// **<u>NOTE:</u>** The origin entity will be automatically excluded from the search
	///
	/// @param origin   	The entity to center the search on
	/// @param radius 		The radius to search within from the center
	/// @param max			The maximum number of entities to retrieve before returning
	/// @param predicate	A predicate to apply to determine eligible entities to return
	/// @return         	The list of all entities within a radius of the origin entity
	public static List<Entity> getEntities(Entity origin, double radius, int max, Predicate<Entity> predicate) {
		return getEntities(origin, radius, radius, radius, max, predicate);
	}

	/// Get all [entities][Entity] within a radius of an origin entity, filtering by a [Predicate]
	///
	/// **<u>NOTE:</u>** The origin entity will be automatically excluded from the search
	///
	/// @param origin   	The entity to center the search on
	/// @param radiusX 		The radius to search within from the center, on the X axis
	/// @param radiusY 		The radius to search within from the center, on the Y axis
	/// @param radiusZ 		The radius to search within from the center, on the Z axis
	/// @param predicate	A predicate to apply to determine eligible entities to return
	/// @return         	The list of all entities within a radius of the origin entity
	public static List<Entity> getEntities(Entity origin, double radiusX, double radiusY, double radiusZ, Predicate<Entity> predicate) {
		return getEntities(origin.level(), origin.getBoundingBox().inflate(radiusX, radiusY, radiusZ),
						   ((Predicate<Entity>)entity -> entity != origin).and(predicate));
	}

	/// Get all [entities][Entity] within a radius of an origin entity, filtering by a [Predicate]
	///
	/// **<u>NOTE:</u>** The origin entity will be automatically excluded from the search
	///
	/// @param origin   	The entity to center the search on
	/// @param radiusX 		The radius to search within from the center, on the X axis
	/// @param radiusY 		The radius to search within from the center, on the Y axis
	/// @param radiusZ 		The radius to search within from the center, on the Z axis
	/// @param max			The maximum number of entities to retrieve before returning
	/// @param predicate	A predicate to apply to determine eligible entities to return
	/// @return         	The list of all entities within a radius of the origin entity
	public static List<Entity> getEntities(Entity origin, double radiusX, double radiusY, double radiusZ, int max, Predicate<Entity> predicate) {
		return getEntities(origin.level(), origin.getBoundingBox().inflate(radiusX, radiusY, radiusZ), max,
						   ((Predicate<Entity>)entity -> entity != origin).and(predicate));
	}

	/// Get all [entities][Entity] within a radius of an origin point, filtering by a [Predicate]
	///
	/// @param level   		The level in which to search for entities
	/// @param origin   	The center location to search around
	/// @param radius 		The radius to search within from the center
	/// @param predicate	A predicate to apply to determine eligible entities to return
	/// @return         	The list of all entities within a radius of the origin point
	public static List<Entity> getEntities(Level level, Vec3 origin, double radius, Predicate<Entity> predicate) {
		return getEntities(level, AABB.ofSize(origin, radius, radius, radius), predicate);
	}

	/// Get all [entities][Entity] within a radius of an origin point, filtering by a [Predicate]
	///
	/// @param level   		The level in which to search for entities
	/// @param origin   	The center location to search around
	/// @param radius 		The radius to search within from the center
	/// @param max			The maximum number of entities to retrieve before returning
	/// @param predicate	A predicate to apply to determine eligible entities to return
	/// @return         	The list of all entities within a radius of the origin point
	public static List<Entity> getEntities(Level level, Vec3 origin, double radius, int max, Predicate<Entity> predicate) {
		return getEntities(level, AABB.ofSize(origin, radius, radius, radius), max, predicate);
	}

	/// Get all [entities][Entity] within a radius of an origin point, filtering by a [Predicate]
	///
	/// @param level   		The level in which to search for entities
	/// @param origin   	The center location to search around
	/// @param radiusX 		The radius to search within from the center, on the X axis
	/// @param radiusY 		The radius to search within from the center, on the Y axis
	/// @param radiusZ 		The radius to search within from the center, on the Z axis
	/// @param predicate	A predicate to apply to determine eligible entities to return
	/// @return         	The list of all entities within a radius of the origin point
	public static List<Entity> getEntities(Level level, Vec3 origin, double radiusX, double radiusY, double radiusZ, Predicate<Entity> predicate) {
		return getEntities(level, AABB.ofSize(origin, radiusX, radiusY, radiusZ), predicate);
	}

	/// Get all [entities][Entity] within a radius of an origin point, filtering by a [Predicate]
	///
	/// @param level   		The level in which to search for entities
	/// @param origin   	The center location to search around
	/// @param radiusX 		The radius to search within from the center, on the X axis
	/// @param radiusY 		The radius to search within from the center, on the Y axis
	/// @param radiusZ 		The radius to search within from the center, on the Z axis
	/// @param predicate	A predicate to apply to determine eligible entities to return
	/// @return         	The list of all entities within a radius of the origin point
	public static List<Entity> getEntities(Level level, Vec3 origin, double radiusX, double radiusY, double radiusZ, int max, Predicate<Entity> predicate) {
		return getEntities(level, AABB.ofSize(origin, radiusX, radiusY, radiusZ), max, predicate);
	}

	/// Get all [entities][Entity] within a specified area, filtering by a [Predicate]
	///
	/// @param level   		The level in which to search for entities
	/// @param bounds   	The region in which to search
	/// @param predicate	A predicate to apply to determine eligible entities to return
	/// @return         	The list of all entities within a radius of the origin point
	public static List<Entity> getEntities(Level level, AABB bounds, Predicate<Entity> predicate) {
		return getEntities(level, bounds, Entity.class, predicate);
	}

	/// Get all [entities][Entity] within a specified area, filtering by a [Predicate]
	///
	/// @param level   		The level in which to search for entities
	/// @param bounds   	The region in which to search
	/// @param max			The maximum number of entities to retrieve before returning
	/// @param predicate	A predicate to apply to determine eligible entities to return
	/// @return         	The list of all entities within a radius of the origin point
	public static List<Entity> getEntities(Level level, AABB bounds, int max, Predicate<Entity> predicate) {
		return getEntities(level, bounds, Entity.class, max, predicate);
	}

	/// Get all [entities][Entity] within a radius of an origin entity, filtering by entity class and a [Predicate]
	///
	/// **<u>NOTE:</u>** The origin entity will be automatically excluded from the search
	///
	/// @param origin   	The entity to center the search on
	/// @param radius 		The radius to search within from the center
	/// @param minimumClass The minimum entity class type to search for
	/// @param predicate	A predicate to apply to determine eligible entities to return
	/// @param <T> 			The class in which all checked entities should be or extend. More specific types are more efficient
	/// @return         	The list of all entities within a radius of the origin point
	public static <T extends Entity> List<T> getEntities(Entity origin, double radius, Class<T> minimumClass, Predicate<? super T> predicate) {
		return getEntities(origin, radius, radius, radius, minimumClass, predicate);
	}

	/// Get all [entities][Entity] within a radius of an origin entity, filtering by entity class and a [Predicate]
	///
	/// **<u>NOTE:</u>** The origin entity will be automatically excluded from the search
	///
	/// @param origin   	The entity to center the search on
	/// @param radius 		The radius to search within from the center
	/// @param minimumClass The minimum entity class type to search for
	/// @param max			The maximum number of entities to retrieve before returning
	/// @param predicate	A predicate to apply to determine eligible entities to return
	/// @param <T> 			The class in which all checked entities should be or extend. More specific types are more efficient
	/// @return         	The list of all entities within a radius of the origin point
	public static <T extends Entity> List<T> getEntities(Entity origin, double radius, Class<T> minimumClass, int max, Predicate<? super T> predicate) {
		return getEntities(origin, radius, radius, radius, minimumClass, max, predicate);
	}

	/// Get all [entities][Entity] within a radius of an origin entity, filtering by entity class and a [Predicate]
	///
	/// **<u>NOTE:</u>** The origin entity will be automatically excluded from the search
	///
	/// @param origin   	The entity to center the search on
	/// @param radiusX 		The radius to search within from the center, on the X axis
	/// @param radiusY 		The radius to search within from the center, on the Y axis
	/// @param radiusZ 		The radius to search within from the center, on the Z axis
	/// @param minimumClass The minimum entity class type to search for
	/// @param predicate	A predicate to apply to determine eligible entities to return
	/// @param <T> 			The class in which all checked entities should be or extend. More specific types are more efficient
	/// @return         	The list of all entities within a radius of the origin point
	public static <T extends Entity> List<T> getEntities(Entity origin, double radiusX, double radiusY, double radiusZ, Class<T> minimumClass, Predicate<? super T> predicate) {
		return getEntities(origin.level(), origin.getBoundingBox().inflate(radiusX, radiusY, radiusZ), minimumClass,
						   ((Predicate<T>)entity -> entity != origin).and(predicate));
	}

	/// Get all [entities][Entity] within a radius of an origin entity, filtering by entity class and a [Predicate]
	///
	/// **<u>NOTE:</u>** The origin entity will be automatically excluded from the search
	///
	/// @param origin   	The entity to center the search on
	/// @param radiusX 		The radius to search within from the center, on the X axis
	/// @param radiusY 		The radius to search within from the center, on the Y axis
	/// @param radiusZ 		The radius to search within from the center, on the Z axis
	/// @param minimumClass The minimum entity class type to search for
	/// @param max			The maximum number of entities to retrieve before returning
	/// @param predicate	A predicate to apply to determine eligible entities to return
	/// @param <T> 			The class in which all checked entities should be or extend. More specific types are more efficient
	/// @return         	The list of all entities within a radius of the origin point
	public static <T extends Entity> List<T> getEntities(Entity origin, double radiusX, double radiusY, double radiusZ, Class<T> minimumClass, int max, Predicate<? super T> predicate) {
		return getEntities(origin.level(), origin.getBoundingBox().inflate(radiusX, radiusY, radiusZ), minimumClass, max,
						   ((Predicate<T>)entity -> entity != origin).and(predicate));
	}

	/// Get all [entities][Entity] within a radius of an origin point, filtering by entity class and a [Predicate]
	///
	/// @param level   		The level in which to search for entities
	/// @param origin   	The center location to search around
	/// @param radius 		The radius to search within from the center
	/// @param minimumClass The minimum entity class type to search for
	/// @param predicate	A predicate to apply to determine eligible entities to return
	/// @param <T> 			The class in which all checked entities should be or extend. More specific types are more efficient
	/// @return         	The list of all entities within a radius of the origin point
	public static <T extends Entity> List<T> getEntities(Level level, Vec3 origin, double radius, Class<T> minimumClass, Predicate<? super T> predicate) {
		return getEntities(level, AABB.ofSize(origin, radius, radius, radius), minimumClass, predicate);
	}

	/// Get all [entities][Entity] within a radius of an origin point, filtering by entity class and a [Predicate]
	///
	/// @param level   		The level in which to search for entities
	/// @param origin   	The center location to search around
	/// @param radius 		The radius to search within from the center
	/// @param minimumClass The minimum entity class type to search for
	/// @param max			The maximum number of entities to retrieve before returning
	/// @param predicate	A predicate to apply to determine eligible entities to return
	/// @param <T> 			The class in which all checked entities should be or extend. More specific types are more efficient
	/// @return         	The list of all entities within a radius of the origin point
	public static <T extends Entity> List<T> getEntities(Level level, Vec3 origin, double radius, Class<T> minimumClass, int max, Predicate<? super T> predicate) {
		return getEntities(level, AABB.ofSize(origin, radius, radius, radius), minimumClass, max, predicate);
	}

	/// Get all [entities][Entity] within a radius of an origin point, filtering by entity class and a [Predicate]
	///
	/// @param level   		The level in which to search for entities
	/// @param origin   	The center location to search around
	/// @param radiusX 		The radius to search within from the center, on the X axis
	/// @param radiusY 		The radius to search within from the center, on the Y axis
	/// @param radiusZ 		The radius to search within from the center, on the Z axis
	/// @param minimumClass The minimum entity class type to search for
	/// @param predicate	A predicate to apply to determine eligible entities to return
	/// @param <T> 			The class in which all checked entities should be or extend. More specific types are more efficient
	/// @return         	The list of all entities within a radius of the origin point
	public static <T extends Entity> List<T> getEntities(Level level, Vec3 origin, double radiusX, double radiusY, double radiusZ, Class<T> minimumClass, Predicate<? super T> predicate) {
		return getEntities(level, AABB.ofSize(origin, radiusX, radiusY, radiusZ), minimumClass, predicate);
	}

	/// Get all [entities][Entity] within a radius of an origin point, filtering by entity class and a [Predicate]
	///
	/// @param level   		The level in which to search for entities
	/// @param origin   	The center location to search around
	/// @param radiusX 		The radius to search within from the center, on the X axis
	/// @param radiusY 		The radius to search within from the center, on the Y axis
	/// @param radiusZ 		The radius to search within from the center, on the Z axis
	/// @param minimumClass The minimum entity class type to search for
	/// @param max			The maximum number of entities to retrieve before returning
	/// @param predicate	A predicate to apply to determine eligible entities to return
	/// @param <T> 			The class in which all checked entities should be or extend. More specific types are more efficient
	/// @return         	The list of all entities within a radius of the origin point
	public static <T extends Entity> List<T> getEntities(Level level, Vec3 origin, double radiusX, double radiusY, double radiusZ, Class<T> minimumClass, int max, Predicate<? super T> predicate) {
		return getEntities(level, AABB.ofSize(origin, radiusX, radiusY, radiusZ), minimumClass, max, predicate);
	}

	/// Get all [entities][Entity] within a specified area, filtering by entity class and a [Predicate]
	///
	/// @param level   		The level in which to search for entities
	/// @param bounds   	The region in which to search
	/// @param minimumClass The minimum entity class type to search for
	/// @param predicate	A predicate to apply to determine eligible entities to return
	/// @param <T> 			The class in which all checked entities should be or extend. More specific types are more efficient
	/// @return         	The list of all entities within a radius of the origin point
	public static <T extends Entity> List<T> getEntities(Level level, AABB bounds, Class<T> minimumClass, Predicate<? super T> predicate) {
		return getEntities(level, bounds, minimumClass, Integer.MAX_VALUE, predicate);
	}

	/// Get all [entities][Entity] within a specified area, filtering by entity class and a [Predicate]
	///
	/// @param level   		The level in which to search for entities
	/// @param bounds   	The region in which to search
	/// @param minimumClass The minimum entity class type to search for
	/// @param max			The maximum number of entities to retrieve before returning
	/// @param predicate	A predicate to apply to determine eligible entities to return
	/// @param <T> 			The class in which all checked entities should be or extend. More specific types are more efficient
	/// @return         	The list of all entities within a radius of the origin point
	public static <T extends Entity> List<T> getEntities(Level level, AABB bounds, Class<T> minimumClass, int max, Predicate<? super T> predicate) {
		final Set<T> foundEntities = new ReferenceOpenHashSet<>();
		final EntityTypeTest<Entity, T> typeTest = makeEntityTypeTest(minimumClass);

		level.getEntities().get(typeTest, bounds, entity -> {
			if (isEntityInBounds(entity, bounds) && predicate.test(entity)) {
				foundEntities.add(entity);

				if (foundEntities.size() >= max)
					return AbortableIterationConsumer.Continuation.ABORT;
			}

			return AbortableIterationConsumer.Continuation.CONTINUE;
		});

		if (foundEntities.size() < max) {
			final Pair<Collection<? extends Entity>, Function<Entity, ? extends Entity>> partEntities = SBLConstants.PLATFORM.getPartEntities(level);

			for (Entity part : partEntities.getFirst()) {
				final T entity = typeTest.tryCast(partEntities.getSecond().apply(part));

				if (entity != null && !foundEntities.contains(entity) && isEntityInBounds(part, bounds) && predicate.test(entity)) {
					foundEntities.add(entity);

					if (foundEntities.size() >= max)
						break;
				}
			}
		}

		return new ReferenceArrayList<>(foundEntities);
	}

	/// Find the first [Entity] matching a [Predicate] within a radius of an origin entity
	///
	/// This does not sort by proximity, instead returning as soon as any matching entity is found
	///
	/// **<u>NOTE:</u>** The origin entity will be automatically excluded from the search
	///
	/// @param origin   	The entity to center the search on
	/// @param radius 		The radius to search within from the center
	/// @param predicate	A predicate to apply to determine the entity to return
	/// @return				The first entity to meet the predicate condition, or an empty [Optional] if no match found
	public static Optional<Entity> findEntity(Entity origin, double radius, Predicate<Entity> predicate) {
		return findEntity(origin, radius, radius, radius, predicate);
	}

	/// Find the first [Entity] matching a [Predicate] within a radius of an origin entity
	///
	/// This does not sort by proximity, instead returning as soon as any matching entity is found
	///
	/// **<u>NOTE:</u>** The origin entity will be automatically excluded from the search
	///
	/// @param origin   	The entity to center the search on
	/// @param radiusX 		The radius to search within from the center, on the X axis
	/// @param radiusY 		The radius to search within from the center, on the Y axis
	/// @param radiusZ 		The radius to search within from the center, on the Z axis
	/// @param predicate	A predicate to apply to determine the entity to return
	/// @return				The first entity to meet the predicate condition, or an empty [Optional] if no match found
	public static Optional<Entity> findEntity(Entity origin, double radiusX, double radiusY, double radiusZ, Predicate<Entity> predicate) {
		return findEntity(origin.level(), origin.getBoundingBox().inflate(radiusX, radiusY, radiusZ),
						  ((Predicate<Entity>)entity -> entity != origin).and(predicate));
	}

	/// Find the first [Entity] matching a [Predicate] within a radius of an origin point
	///
	/// This does not sort by proximity, instead returning as soon as any matching entity is found
	///
	/// @param level   		The level in which to search for entities
	/// @param origin   	The center location to search around
	/// @param radius 		The radius to search within from the center
	/// @param predicate	A predicate to apply to determine the entity to return
	/// @return				The first entity to meet the predicate condition, or an empty [Optional] if no match found
	public static Optional<Entity> findEntity(Level level, Vec3 origin, double radius, Predicate<Entity> predicate) {
		return findEntity(level, AABB.ofSize(origin, radius, radius, radius), predicate);
	}

	/// Find the first [Entity] matching a [Predicate] within a radius of an origin point
	///
	/// This does not sort by proximity, instead returning as soon as any matching entity is found
	///
	/// @param level   		The level in which to search for entities
	/// @param origin   	The center location to search around
	/// @param radiusX 		The radius to search within from the center, on the X axis
	/// @param radiusY 		The radius to search within from the center, on the Y axis
	/// @param radiusZ 		The radius to search within from the center, on the Z axis
	/// @param predicate	A predicate to apply to determine the entity to return
	/// @return				The first entity to meet the predicate condition, or an empty [Optional] if no match found
	public static Optional<Entity> findEntity(Level level, Vec3 origin, double radiusX, double radiusY, double radiusZ, Predicate<Entity> predicate) {
		return findEntity(level, AABB.ofSize(origin, radiusX, radiusY, radiusZ), predicate);
	}

	/// Find the first [Entity] matching a [Predicate] within a specified area
	///
	/// This does not sort by proximity, instead returning as soon as any matching entity is found
	///
	/// @param level   		The level in which to search for entities
	/// @param bounds   	The region in which to search
	/// @param predicate	A predicate to apply to determine the entity to return
	/// @return				The first entity to meet the predicate condition, or an empty [Optional] if no match found
	public static Optional<Entity> findEntity(Level level, AABB bounds, Predicate<Entity> predicate) {
		return findEntity(level, bounds, Entity.class, predicate);
	}

	/// Find the first [Entity] matching a [Predicate] within a radius of an origin point, filtering by entity class type.<br/>
	/// This is significantly more efficient than non-filtering search methods, due to the way entities are stored
	///
	/// This does not sort by proximity, instead returning as soon as any matching entity is found
	///
	/// **<u>NOTE:</u>** The origin entity will be automatically excluded from the search
	///
	/// @param origin   	The entity to center the search on
	/// @param radius 		The radius to search within from the center
	/// @param minimumClass The minimum entity class type to search for
	/// @param predicate	A predicate to apply to determine the entity to return
	/// @param <T> 			The class in which all checked entities should be or extend. More specific types are more efficient
	/// @return				The first entity to meet the predicate condition, or an empty [Optional] if no match found
	public static <T extends Entity> Optional<T> findEntity(Entity origin, double radius, Class<T> minimumClass, Predicate<? super T> predicate) {
		return findEntity(origin, radius, radius, radius, minimumClass, predicate);
	}

	/// Find the first [Entity] matching a [Predicate] within a radius of an origin point, filtering by entity class type.<br/>
	/// This is significantly more efficient than non-filtering search methods, due to the way entities are stored
	///
	/// This does not sort by proximity, instead returning as soon as any matching entity is found
	///
	/// **<u>NOTE:</u>** The origin entity will be automatically excluded from the search
	///
	/// @param origin   	The entity to center the search on
	/// @param radiusX 		The radius to search within from the center, on the X axis
	/// @param radiusY 		The radius to search within from the center, on the Y axis
	/// @param radiusZ 		The radius to search within from the center, on the Z axis
	/// @param minimumClass The minimum entity class type to search for
	/// @param predicate	A predicate to apply to determine the entity to return
	/// @param <T> 			The class in which all checked entities should be or extend. More specific types are more efficient
	/// @return				The first entity to meet the predicate condition, or an empty [Optional] if no match found
	public static <T extends Entity> Optional<T> findEntity(Entity origin, double radiusX, double radiusY, double radiusZ, Class<T> minimumClass, Predicate<? super T> predicate) {
		return findEntity(origin.level(), origin.getBoundingBox().inflate(radiusX, radiusY, radiusZ), minimumClass,
						  ((Predicate<T>)entity -> entity != origin).and(predicate));
	}

	/// Find the first [Entity] matching a [Predicate] within a radius of an origin point, filtering by entity class type.<br/>
	/// This is significantly more efficient than non-filtering search methods, due to the way entities are stored
	///
	/// This does not sort by proximity, instead returning as soon as any matching entity is found
	///
	/// @param level   		The level in which to search for entities
	/// @param origin   	The center location to search around
	/// @param radius 		The radius to search within from the center
	/// @param minimumClass The minimum entity class type to search for
	/// @param predicate	A predicate to apply to determine the entity to return
	/// @param <T> 			The class in which all checked entities should be or extend. More specific types are more efficient
	/// @return				The first entity to meet the predicate condition, or an empty [Optional] if no match found
	public static <T extends Entity> Optional<T> findEntity(Level level, Vec3 origin, double radius, Class<T> minimumClass, Predicate<? super T> predicate) {
		return findEntity(level, AABB.ofSize(origin, radius, radius, radius), minimumClass, predicate);
	}

	/// Find the first [Entity] matching a [Predicate] within a radius of an origin point, filtering by entity class type.<br/>
	/// This is significantly more efficient than non-filtering search methods, due to the way entities are stored
	///
	/// This does not sort by proximity, instead returning as soon as any matching entity is found
	///
	/// @param level   		The level in which to search for entities
	/// @param origin   	The center location to search around
	/// @param radiusX 		The radius to search within from the center, on the X axis
	/// @param radiusY 		The radius to search within from the center, on the Y axis
	/// @param radiusZ 		The radius to search within from the center, on the Z axis
	/// @param minimumClass The minimum entity class type to search for
	/// @param predicate	A predicate to apply to determine the entity to return
	/// @param <T> 			The class in which all checked entities should be or extend. More specific types are more efficient
	/// @return				The first entity to meet the predicate condition, or an empty [Optional] if no match found
	public static <T extends Entity> Optional<T> findEntity(Level level, Vec3 origin, double radiusX, double radiusY, double radiusZ, Class<T> minimumClass, Predicate<? super T> predicate) {
		return findEntity(level, AABB.ofSize(origin, radiusX, radiusY, radiusZ), minimumClass, predicate);
	}

	/// Find the first [Entity] matching a [Predicate] within a radius of an origin point, filtering by entity class type.<br/>
	/// This is significantly more efficient than non-filtering search methods, due to the way entities are stored
	///
	/// This does not sort by proximity, instead returning as soon as any matching entity is found
	///
	/// @param level   		The level in which to search for entities
	/// @param bounds   	The region in which to search
	/// @param minimumClass The minimum entity class type to search for
	/// @param predicate	A predicate to apply to determine the entity to return
	/// @param <T> 			The class in which all checked entities should be or extend. More specific types are more efficient
	/// @return				The first entity to meet the predicate condition, or an empty [Optional] if no match found
	public static <T extends Entity> Optional<T> findEntity(Level level, AABB bounds, Class<T> minimumClass, Predicate<? super T> predicate) {
		final AtomicReference<@Nullable T> foundEntity = new AtomicReference<>(null);
		final EntityTypeTest<Entity, T> typeTest = makeEntityTypeTest(minimumClass);

		level.getEntities().get(typeTest, bounds, entity -> {
			if (isEntityInBounds(entity, bounds) && predicate.test(entity)) {
				foundEntity.set(entity);

				return AbortableIterationConsumer.Continuation.ABORT;
			}

			return AbortableIterationConsumer.Continuation.CONTINUE;
		});

		if (foundEntity.get() == null) {
			final Pair<Collection<? extends Entity>, Function<Entity, ? extends Entity>> partEntities = SBLConstants.PLATFORM.getPartEntities(level);

			for (Entity part : partEntities.getFirst()) {
				final T entity = typeTest.tryCast(partEntities.getSecond().apply(part));

				if (entity != null && isEntityInBounds(part, bounds) && predicate.test(entity)) {
					foundEntity.set(entity);

					break;
				}
			}
		}

		return Optional.ofNullable(foundEntity.get());
	}

	/// Find the first [Player] matching a [Predicate] within a radius of an origin entity.<br/>
	/// This is significantly more efficient than non-player methods when looking for players,
	/// as it only searches the list of players rather than the pool of entities
	///
	/// This does not sort by proximity, instead returning as soon as any matching player is found
	///
	/// **<u>NOTE:</u>** The origin entity will be automatically excluded from the search if it is a player
	///
	/// @param origin   	The entity to center the search on
	/// @param radius 		The radius to search within from the center
	/// @param predicate	A predicate to apply to determine the player to return
	/// @return				The first player to meet the predicate condition, or an empty [Optional] if no match found
	public static Optional<Player> findPlayer(Entity origin, double radius, Predicate<Player> predicate) {
		return findPlayer(origin, radius, radius, radius, predicate);
	}

	/// Find the first [Player] matching a [Predicate] within a radius of an origin entity.<br/>
	/// This is significantly more efficient than non-player methods when looking for players,
	/// as it only searches the list of players rather than the pool of entities
	///
	/// This does not sort by proximity, instead returning as soon as any matching player is found
	///
	/// **<u>NOTE:</u>** The origin entity will be automatically excluded from the search if it is a player
	///
	/// @param origin   	The entity to center the search on
	/// @param radiusX 		The radius to search within from the center, on the X axis
	/// @param radiusY 		The radius to search within from the center, on the Y axis
	/// @param radiusZ 		The radius to search within from the center, on the Z axis
	/// @param predicate	A predicate to apply to determine the player to return
	/// @return				The first player to meet the predicate condition, or an empty [Optional] if no match found
	public static Optional<Player> findPlayer(Entity origin, double radiusX, double radiusY, double radiusZ, Predicate<Player> predicate) {
		return findPlayer(origin.level(), origin.getBoundingBox().inflate(radiusX, radiusY, radiusZ),
						  ((Predicate<Player>)entity -> entity != origin).and(predicate));
	}

	/// Find the first [Player] matching a [Predicate] within a radius of an origin point.<br/>
	/// This is significantly more efficient than non-player methods when looking for players,
	/// as it only searches the list of players rather than the pool of entities
	///
	/// This does not sort by proximity, instead returning as soon as any matching player is found
	///
	/// @param level   		The level in which to search for players
	/// @param origin   	The center location to search around
	/// @param radius 		The radius to search within from the center
	/// @param predicate	A predicate to apply to determine the player to return
	/// @return				The first player to meet the predicate condition, or an empty [Optional] if no match found
	public static Optional<Player> findPlayer(Level level, Vec3 origin, double radius, Predicate<Player> predicate) {
		return findPlayer(level, AABB.ofSize(origin, radius, radius, radius), predicate);
	}

	/// Find the first [Player] matching a [Predicate] within a radius of an origin point.<br/>
	/// This is significantly more efficient than non-player methods when looking for players,
	/// as it only searches the list of players rather than the pool of entities
	///
	/// This does not sort by proximity, instead returning as soon as any matching player is found
	///
	/// @param level   		The level in which to search for players
	/// @param origin   	The center location to search around
	/// @param radiusX 		The radius to search within from the center, on the X axis
	/// @param radiusY 		The radius to search within from the center, on the Y axis
	/// @param radiusZ 		The radius to search within from the center, on the Z axis
	/// @param predicate	A predicate to apply to determine the player to return
	/// @return				The first player to meet the predicate condition, or an empty [Optional] if no match found
	public static Optional<Player> findPlayer(Level level, Vec3 origin, double radiusX, double radiusY, double radiusZ, Predicate<Player> predicate) {
		return findPlayer(level, AABB.ofSize(origin, radiusX, radiusY, radiusZ), predicate);
	}

	/// Find the first [Player] matching a [Predicate] within a radius of an origin point.<br/>
	/// This is significantly more efficient than non-player methods when looking for players,
	/// as it only searches the list of players rather than the pool of entities
	///
	/// This does not sort by proximity, instead returning as soon as any matching player is found
	///
	/// @param level   		The level in which to search for players
	/// @param bounds   	The region in which to search
	/// @param predicate	A predicate to apply to determine the player to return
	/// @return				The first player to meet the predicate condition, or an empty [Optional] if no match found
	public static Optional<Player> findPlayer(Level level, AABB bounds, Predicate<Player> predicate) {
		for (Player player : level.players()) {
			if (isEntityInBounds(player, bounds) && predicate.test(player))
				return Optional.of(player);
		}

		return Optional.empty();
	}

	/// Find the first [ServerPlayer] matching a [Predicate] within a radius of an origin entity.<br/>
	/// This is significantly more efficient than non-player methods when looking for players,
	/// as it only searches the list of players rather than the pool of entities
	///
	/// This does not sort by proximity, instead returning as soon as any matching player is found
	///
	/// **<u>NOTE:</u>** The origin entity will be automatically excluded from the search if it is a player
	/// **<u>WARNING:</u>** This will throw an exception if called on the client-side
	///
	/// @param origin   	The entity to center the search on
	/// @param radius 		The radius to search within from the center
	/// @param predicate	A predicate to apply to determine the player to return
	/// @return				The first player to meet the predicate condition, or an empty [Optional] if no match found
	public static Optional<ServerPlayer> findServerPlayer(Entity origin, double radius, Predicate<ServerPlayer> predicate) {
		return findServerPlayer(origin, radius, radius, radius, predicate);
	}

	/// Find the first [ServerPlayer] matching a [Predicate] within a radius of an origin entity.<br/>
	/// This is significantly more efficient than non-player methods when looking for players,
	/// as it only searches the list of players rather than the pool of entities
	///
	/// This does not sort by proximity, instead returning as soon as any matching player is found
	///
	/// **<u>NOTE:</u>** The origin entity will be automatically excluded from the search if it is a player
	/// **<u>WARNING:</u>** This will throw an exception if called on the client-side
	///
	/// @param origin   	The entity to center the search on
	/// @param radiusX 		The radius to search within from the center, on the X axis
	/// @param radiusY 		The radius to search within from the center, on the Y axis
	/// @param radiusZ 		The radius to search within from the center, on the Z axis
	/// @param predicate	A predicate to apply to determine the player to return
	/// @return				The first player to meet the predicate condition, or an empty [Optional] if no match found
	public static Optional<ServerPlayer> findServerPlayer(Entity origin, double radiusX, double radiusY, double radiusZ, Predicate<ServerPlayer> predicate) {
		return findServerPlayer((ServerLevel)origin.level(), origin.getBoundingBox().inflate(radiusX, radiusY, radiusZ),
						  ((Predicate<ServerPlayer>)entity -> entity != origin).and(predicate));
	}

	/// Find the first [ServerPlayer] matching a [Predicate] within a radius of an origin point.<br/>
	/// This is significantly more efficient than non-player methods when looking for players,
	/// as it only searches the list of players rather than the pool of entities
	///
	/// This does not sort by proximity, instead returning as soon as any matching player is found
	///
	/// @param level   		The level in which to search for players
	/// @param origin   	The center location to search around
	/// @param radius 		The radius to search within from the center
	/// @param predicate	A predicate to apply to determine the player to return
	/// @return				The first player to meet the predicate condition, or an empty [Optional] if no match found
	public static Optional<ServerPlayer> findServerPlayer(ServerLevel level, Vec3 origin, double radius, Predicate<ServerPlayer> predicate) {
		return findServerPlayer(level, AABB.ofSize(origin, radius, radius, radius), predicate);
	}

	/// Find the first [ServerPlayer] matching a [Predicate] within a radius of an origin point.<br/>
	/// This is significantly more efficient than non-player methods when looking for players,
	/// as it only searches the list of players rather than the pool of entities
	///
	/// This does not sort by proximity, instead returning as soon as any matching player is found
	///
	/// @param level   		The level in which to search for players
	/// @param origin   	The center location to search around
	/// @param radiusX 		The radius to search within from the center, on the X axis
	/// @param radiusY 		The radius to search within from the center, on the Y axis
	/// @param radiusZ 		The radius to search within from the center, on the Z axis
	/// @param predicate	A predicate to apply to determine the player to return
	/// @return				The first player to meet the predicate condition, or an empty [Optional] if no match found
	public static Optional<ServerPlayer> findServerPlayer(ServerLevel level, Vec3 origin, double radiusX, double radiusY, double radiusZ, Predicate<ServerPlayer> predicate) {
		return findServerPlayer(level, AABB.ofSize(origin, radiusX, radiusY, radiusZ), predicate);
	}

	/// Find the first [ServerPlayer] matching a [Predicate] within a radius of an origin point.<br/>
	/// This is significantly more efficient than non-player methods when looking for players,
	/// as it only searches the list of players rather than the pool of entities
	///
	/// This does not sort by proximity, instead returning as soon as any matching player is found
	///
	/// @param level   		The level in which to search for players
	/// @param bounds   	The region in which to search
	/// @param predicate	A predicate to apply to determine the player to return
	/// @return				The first player to meet the predicate condition, or an empty [Optional] if no match found
	public static Optional<ServerPlayer> findServerPlayer(ServerLevel level, AABB bounds, Predicate<ServerPlayer> predicate) {
		for (ServerPlayer player : level.players()) {
			if (isEntityInBounds(player, bounds) && predicate.test(player))
				return Optional.of(player);
		}

		return Optional.empty();
	}

	/// An extension method for determining whether an entity is in a region
	///
	/// This differs from the typical implementation in that it checks for the entire hitbox, not just checking if the center-bottom of the entity is within the bounds,
	/// ensuring that larger entities or multipart entities are properly identified
	public static boolean isEntityInBounds(Entity entity, AABB bounds) {
		if (bounds.contains(entity.position()))
			return true;

		return bounds.intersects(entity.getBoundingBox());
	}

	/// Internal method for wrapping the [EntityTypeTest] system to account for root [Entity] types,
	/// which don't need wasteful instance checks and casting for every instance
	@SuppressWarnings("unchecked")
    private static <T extends Entity> EntityTypeTest<Entity, T> makeEntityTypeTest(Class<T> forClass) {
		if (forClass != Entity.class)
			return EntityTypeTest.forClass(forClass);

		return (EntityTypeTest<Entity, T>)new EntityTypeTest<Entity, Entity>() {
			@Override
			public Entity tryCast(Entity entity) {
				return entity;
			}

			@Override
			public Class<? extends Entity> getBaseClass() {
				return Entity.class;
			}
		};
	}
}
