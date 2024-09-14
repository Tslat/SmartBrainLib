package net.tslat.smartbrainlib.util;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.SectionPos;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntitySectionStorage;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.LevelEntityGetterAdapter;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.SBLConstants;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * A helper class for retrieving entities from a given world.
 * <p>
 * This removes a lot of the overhead of vanilla's type-checking, casting and redundant stream-collection.
 * <p>
 * Ultimately this leaves some casting up to the end-user, and streamlines the actual retrieval functions to their most optimised form.
 */
@SuppressWarnings({"unchecked", "unused"})
public final class EntityRetrievalUtil {
	/**
	 * Get the nearest entity from an existing list of entities.
	 *
	 * @param origin   The center-point of the distance comparison
	 * @param entities The existing list of entities
	 * @return         The closest entity to the origin point, or null if the input list was empty
	 * @param <T>      The entity type
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
	 * NOTE: The origin entity will be automatically excluded from the search and will not be passed to the predicate
	 */
	public static <T> Optional<T> getNearestEntity(Entity origin, double radius) {
		return getNearestEntity(origin, radius, radius, radius);
	}

	/**
	 * NOTE: The origin entity will be automatically excluded from the search and will not be passed to the predicate
	 */
	public static <T> Optional<T> getNearestEntity(Entity origin, double radiusX, double radiusY, double radiusZ) {
		return (Optional<T>)getNearestEntity(origin.level(), origin.position(), radiusX, radiusY, radiusZ, entity -> entity != origin);
	}

	public static <T extends Entity> Optional<T> getNearestEntity(Level level, Vec3 origin, double radius) {
		return getNearestEntity(level, origin, radius, radius, radius);
	}

	public static <T extends Entity> Optional<T> getNearestEntity(Level level, Vec3 origin, double radiusX, double radiusY, double radiusZ) {
		return (Optional<T>)getNearestEntity(level, origin, radiusX, radiusY, radiusZ, Entity.class);
	}

	/**
	 * NOTE: The origin entity will be automatically excluded from the search and will not be passed to the predicate
	 */
	public static <T extends Entity> Optional<T> getNearestEntity(Entity origin, double radius, Class<T> minimumClass) {
		return getNearestEntity(origin, radius, radius, radius, minimumClass, entity -> entity != origin);
	}

	/**
	 * NOTE: The origin entity will be automatically excluded from the search and will not be passed to the predicate
	 */
	public static <T extends Entity> Optional<T> getNearestEntity(Entity origin, double radiusX, double radiusY, double radiusZ, Class<T> minimumClass) {
		return getNearestEntity(origin.level(), origin.position(), radiusX, radiusY, radiusZ, minimumClass, entity -> entity != origin);
	}

	public static <T extends Entity> Optional<T> getNearestEntity(Level level, Vec3 origin, double radius, Class<T> minimumClass) {
		return getNearestEntity(level, origin, radius, radius, radius, minimumClass);
	}

	public static <T extends Entity> Optional<T> getNearestEntity(Level level, Vec3 origin, double radiusX, double radiusY, double radiusZ, Class<T> minimumClass) {
		return getNearestEntity(level, AABB.ofSize(origin, radiusX, radiusY, radiusZ), origin, minimumClass, entity -> true);
	}

	/**
	 * NOTE: The origin entity will be automatically excluded from the search and will not be passed to the predicate
	 */
	public static <T extends Entity> T getNearestEntity(Entity origin, double radius, Predicate<? extends Entity> predicate) {
		return getNearestEntity(origin, radius, radius, radius, predicate);
	}

	/**
	 * NOTE: The origin entity will be automatically excluded from the search and will not be passed to the predicate
	 */
	public static <T extends Entity> T getNearestEntity(Entity origin, double radiusX, double radiusY, double radiusZ, Predicate<? extends Entity> predicate) {
		return (T)getNearestEntity(origin.level(), origin.position(), radiusX, radiusY, radiusZ, ((Predicate<Entity>)entity -> entity != origin).and((Predicate<Entity>)predicate)).orElse(null);
	}

	public static <T extends Entity> Optional<T> getNearestEntity(Level level, Vec3 origin, double radius, Predicate<Entity> predicate) {
		return getNearestEntity(level, origin, radius, radius, radius, predicate);
	}

	public static <T extends Entity> Optional<T> getNearestEntity(Level level, Vec3 origin, double radiusX, double radiusY, double radiusZ, Predicate<Entity> predicate) {
		return Optional.ofNullable(getNearestEntity(level, AABB.ofSize(origin, radiusX, radiusY, radiusZ), origin, predicate));
	}

	@Nullable
	public static <T extends Entity> T getNearestEntity(Level level, AABB bounds, Vec3 origin, Predicate<? extends Entity> predicate) {
		return (T)getNearestEntity(level, bounds, origin, Entity.class, (Predicate<Entity>)predicate).orElse(null);
	}

	/**
	 * NOTE: The origin entity will be automatically excluded from the search and will not be passed to the predicate
	 */
	public static <T extends Entity> Optional<T> getNearestEntity(Entity origin, double radius, Class<T> minimumClass, Predicate<T> predicate) {
		return getNearestEntity(origin, radius, radius, radius, minimumClass, predicate);
	}

	/**
	 * NOTE: The origin entity will be automatically excluded from the search and will not be passed to the predicate
	 */
	public static <T extends Entity> Optional<T> getNearestEntity(Entity origin, double radiusX, double radiusY, double radiusZ, Class<T> minimumClass, Predicate<T> predicate) {
		return getNearestEntity(origin.level(), origin.position(), radiusX, radiusY, radiusZ, minimumClass, ((Predicate<T>)entity -> entity != origin).and(predicate));
	}

	public static <T extends Entity> Optional<T> getNearestEntity(Level level, Vec3 origin, double radius, Class<T> minimumClass, Predicate<T> predicate) {
		return getNearestEntity(level, AABB.ofSize(origin, radius, radius, radius), origin, minimumClass, predicate);
	}

	public static <T extends Entity> Optional<T> getNearestEntity(Level level, Vec3 origin, double radiusX, double radiusY, double radiusZ, Class<T> minimumClass, Predicate<T> predicate) {
		return getNearestEntity(level, AABB.ofSize(origin, radiusX, radiusY, radiusZ), origin, minimumClass, predicate);
	}

	/**
	 * Retrieve the entity found within the bounds that is closest to the origin point
	 * <p>
	 * Note that the output is blind-cast to your intended output type for ease of
	 * use. Make sure you check {@code instanceof} in your predicate if you intend
	 * to use any subclass of Entity
	 *
	 * @param level      The level to search in
	 * @param bounds     The region to search for entities in
	 * @param origin     The center-point of the search
	 * @param predicate  The predicate to filter entities by
	 * @return           The closest entity found that meets the given criteria, or null if none found
	 * @param <T>        The class in which all checked entities should be or extend. More specific typings are more efficient
	 */
	@Nullable
	public static <T extends Entity> Optional<T> getNearestEntity(Level level, AABB bounds, Vec3 origin, Class<T> minimumClass, Predicate<T> predicate) {
		final MutableDouble dist = new MutableDouble(Double.MAX_VALUE);
		final MutableObject<T> closest = new MutableObject<>(null);
		final EntityTypeTest<Entity, T> typeTest = makeLazyTypeTest(minimumClass);

		level.getEntities().get(typeTest, bounds, entity -> {
			if (predicate.test(entity)) {
				double entityDist = entity.distanceToSqr(origin);

				if (entityDist < dist.doubleValue()) {
					dist.setValue(entityDist);
					closest.setValue(entity);
				}
			}

			return AbortableIterationConsumer.Continuation.CONTINUE;
		});

		return Optional.ofNullable(closest.getValue());
	}

	/**
	 * NOTE: The origin entity will be automatically excluded from the search and will not be passed to the predicate
	 */
	public static <T extends Player> T getNearestPlayer(Entity origin, double radius) {
		return (T)getNearestPlayer(origin, radius, radius, radius, entity -> entity != origin);
	}

	/**
	 * NOTE: The origin entity will be automatically excluded from the search and will not be passed to the predicate
	 */
	public static <T extends Player> T getNearestPlayer(Entity origin, double radiusX, double radiusY, double radiusZ) {
		return getNearestPlayer(origin.level(), origin.position(), radiusX, radiusY, radiusZ, entity -> entity != origin);
	}

	public static <T extends Player> T getNearestPlayer(Level level, Vec3 origin, double radius) {
		return getNearestPlayer(level, origin, radius, radius, radius);
	}

	public static <T extends Player> T getNearestPlayer(Level level, Vec3 origin, double radiusX, double radiusY, double radiusZ) {
		return getNearestPlayer(level, AABB.ofSize(origin, radiusX, radiusY, radiusZ), origin);
	}

	public static <T extends Player> T getNearestPlayer(Level level, AABB area, Vec3 origin) {
		return (T)getNearestPlayer(level, area, origin, pl -> true);
	}

	/**
	 * NOTE: The origin entity will be automatically excluded from the search and will not be passed to the predicate
	 */
	public static Player getNearestPlayer(Entity origin, double radius, Predicate<Player> predicate) {
		return getNearestPlayer(origin, radius, radius, radius, predicate);
	}

	/**
	 * NOTE: The origin entity will be automatically excluded from the search and will not be passed to the predicate
	 */
	public static Player getNearestPlayer(Entity origin, double radiusX, double radiusY, double radiusZ, Predicate<Player> predicate) {
		return getNearestPlayer(origin.level(), origin.position(), radiusX, radiusY, radiusZ, ((Predicate<Player>)entity -> entity != origin).and(predicate));
	}

	public static <T extends Player> T getNearestPlayer(Level level, Vec3 origin, double radius, Predicate<Player> predicate) {
		return getNearestPlayer(level, origin, radius, radius, radius, predicate);
	}

	public static <T extends Player> T getNearestPlayer(Level level, Vec3 origin, double radiusX, double radiusY, double radiusZ, Predicate<Player> predicate) {
		return (T)getNearestPlayer(level, AABB.ofSize(origin, radiusX, radiusY, radiusZ), origin, predicate);
	}

	/**
	 * Get all players within a given region that meet a given criteria.
	 * <p>
	 * This is an implicitly superior alternative to {@link #getEntities} when only looking for players, as this
	 * only searches the level's players, which is always a significantly smaller pool of targets to search from.
	 * <p>
	 * This should be used in all circumstances where you are only looking for players
	 *
	 * @param level      The level in which to search
	 * @param bounds     The region in which to find players
	 * @param predicate  The criteria to meet for a player to be included in the returned list
	 * @return           A list of all players that are within the given region that meet the criteria in the predicate
	 */
	@Nullable
	public static Player getNearestPlayer(Level level, AABB bounds, Vec3 origin, Predicate<Player> predicate) {
		double dist = Double.MAX_VALUE;
		Player closest = null;

		for (Player player : level.players()) {
			if (bounds.contains(player.position()) && predicate.test(player)) {
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
	 * NOTE: The origin entity will be automatically excluded from the search and will not be passed to the predicate
	 */
	public static List<Player> getPlayers(Entity origin, double radius) {
		return getPlayers(origin, radius, radius, radius, entity -> entity != origin);
	}

	/**
	 * NOTE: The origin entity will be automatically excluded from the search and will not be passed to the predicate
	 */
	public static List<Player> getPlayers(Entity origin, double radiusX, double radiusY, double radiusZ) {
		return getPlayers(origin.level(), origin.position(), radiusX, radiusY, radiusZ, entity -> entity != origin);
	}

	public static <T extends Player> List<T> getPlayers(Level level, Vec3 origin, double radius) {
		return getPlayers(level, origin, radius, radius, radius);
	}

	public static <T extends Player> List<T> getPlayers(Level level, Vec3 origin, double radiusX, double radiusY, double radiusZ) {
		return (List<T>)getPlayers(level, AABB.ofSize(origin, radiusX, radiusY, radiusZ));
	}

	public static <T extends Player> List<T> getPlayers(Level level, Vec3 from, Vec3 to) {
		return (List<T>)getPlayers(level, new AABB(from, to));
	}

	public static List<Player> getPlayers(Level level, AABB area) {
		return getPlayers(level, area, pl -> true);
	}

	/**
	 * NOTE: The origin entity will be automatically excluded from the search and will not be passed to the predicate
	 */
	public static List<Player> getPlayers(Entity origin, double radius, Predicate<Player> predicate) {
		return getPlayers(origin, radius, radius, radius, predicate);
	}

	/**
	 * NOTE: The origin entity will be automatically excluded from the search and will not be passed to the predicate
	 */
	public static List<Player> getPlayers(Entity origin, double radiusX, double radiusY, double radiusZ, Predicate<Player> predicate) {
		return getPlayers(origin.level(), origin.position(), radiusX, radiusY, radiusZ, ((Predicate<Player>)entity -> entity != origin).and(predicate));
	}

	public static <T extends Player> List<T> getPlayers(Level level, Vec3 origin, double radius, Predicate<Player> predicate) {
		return getPlayers(level, origin, radius, radius, radius, predicate);
	}

	public static <T extends Player> List<T> getPlayers(Level level, Vec3 origin, double radiusX, double radiusY, double radiusZ, Predicate<Player> predicate) {
		return (List<T>)getPlayers(level, AABB.ofSize(origin, radiusX, radiusY, radiusZ), predicate);
	}

	public static <T extends Player> List<T> getPlayers(Level level, Vec3 from, Vec3 to, Predicate<Player> predicate) {
		return (List<T>)getPlayers(level, new AABB(from, to), predicate);
	}

	/**
	 * Get all players within a given region that meet a given criteria.
	 * <p>
	 * This is an implicitly superior alternative to {@link #getEntities} when only looking for players, as this
	 * only searches the level's players, which is always a significantly smaller pool of targets to search from.
	 * <br>
	 * This should be used in all circumstances where you are only looking for players
	 *
	 * @param level      The level in which to search
	 * @param bounds     The region in which to find players
	 * @param predicate  The criteria to meet for a player to be included in the returned list
	 * @return           A list of all players that are within the given region that meet the criteria in the predicate
	 */
	public static List<Player> getPlayers(Level level, AABB bounds, Predicate<Player> predicate) {
		List<Player> players = new ObjectArrayList<>();

		for (Player player : level.players()) {
			if (bounds.contains(player.position()) && predicate.test(player))
				players.add(player);
		}

		return players;
	}

	/**
	 * NOTE: The origin entity will be automatically excluded from the search and will not be passed to the predicate
	 */
	public static <T> List<T> getEntities(Entity origin, double radius) {
		return getEntities(origin, radius, radius, radius, entity -> entity != origin);
	}

	/**
	 * NOTE: The origin entity will be automatically excluded from the search and will not be passed to the predicate
	 */
	public static <T> List<T> getEntities(Entity origin, double radiusX, double radiusY, double radiusZ) {
		return (List<T>)getEntities(origin.level(), origin.position(), radiusX, radiusY, radiusZ, entity -> entity != origin);
	}

	public static <T extends Entity> List<T> getEntities(Level level, Vec3 origin, double radius) {
		return getEntities(level, AABB.ofSize(origin, radius, radius, radius));
	}

	public static <T extends Entity> List<T> getEntities(Level level, Vec3 origin, double radiusX, double radiusY, double radiusZ) {
		return getEntities(level, AABB.ofSize(origin, radiusX, radiusY, radiusZ));
	}

	public static <T extends Entity> List<T> getEntities(Level level, Vec3 from, Vec3 to) {
		return getEntities(level, new AABB(from, to));
	}

	public static <T extends Entity> List<T> getEntities(Level level, AABB area) {
		return (List<T>)getEntities(level, area, Entity.class);
	}

	/**
	 * NOTE: The origin entity will be automatically excluded from the search and will not be passed to the predicate
	 */
	public static <T extends Entity> List<T> getEntities(Entity origin, double radius, Class<T> minimumClass) {
		return getEntities(origin, radius, radius, radius, minimumClass, entity -> entity != origin);
	}

	/**
	 * NOTE: The origin entity will be automatically excluded from the search and will not be passed to the predicate
	 */
	public static <T extends Entity> List<T> getEntities(Entity origin, double radiusX, double radiusY, double radiusZ, Class<T> minimumClass) {
		return getEntities(origin.level(), origin.position(), radiusX, radiusY, radiusZ, minimumClass, entity -> entity != origin);
	}

	public static <T extends Entity> List<T> getEntities(Level level, Vec3 origin, double radius, Class<T> minimumClass) {
		return getEntities(level, AABB.ofSize(origin, radius, radius, radius), minimumClass);
	}

	public static <T extends Entity> List<T> getEntities(Level level, Vec3 origin, double radiusX, double radiusY, double radiusZ, Class<T> minimumClass) {
		return getEntities(level, AABB.ofSize(origin, radiusX, radiusY, radiusZ), minimumClass);
	}

	public static <T extends Entity> List<T> getEntities(Level level, Vec3 from, Vec3 to, Class<T> minimumClass) {
		return getEntities(level, new AABB(from, to), minimumClass);
	}

	public static <T extends Entity> List<T> getEntities(Level level, AABB bounds, Class<T> minimumClass) {
		return getEntities(level, bounds, minimumClass, entity -> true);
	}

	/**
	 * NOTE: The origin entity will be automatically excluded from the search and will not be passed to the predicate
	 */
	public static <T> List<T> getEntities(Entity origin, double radius, Predicate<? extends Entity> predicate) {
		return getEntities(origin, radius, radius, radius, predicate);
	}

	/**
	 * NOTE: The origin entity will be automatically excluded from the search and will not be passed to the predicate
	 */
	public static <T> List<T> getEntities(Entity origin, double radiusX, double radiusY, double radiusZ, Predicate<? extends Entity> predicate) {
		return (List<T>)getEntities(origin.level(), origin.position(), radiusX, radiusY, radiusZ, ((Predicate<Entity>)entity -> entity != origin).and((Predicate<Entity>)predicate));
	}

	public static <T extends Entity> List<T> getEntities(Level level, Vec3 origin, double radius, Predicate<Entity> predicate) {
		return getEntities(level, AABB.ofSize(origin, radius, radius, radius), predicate);
	}

	public static <T extends Entity> List<T> getEntities(Level level, Vec3 origin, double radiusX, double radiusY, double radiusZ, Predicate<Entity> predicate) {
		return getEntities(level, AABB.ofSize(origin, radiusX, radiusY, radiusZ), predicate);
	}

	public static <T extends Entity> List<T> getEntities(Level level, Vec3 from, Vec3 to, Predicate<Entity> predicate) {
		return getEntities(level, new AABB(from, to), predicate);
	}

	public static <T extends Entity> List<T> getEntities(Level level, AABB area, Predicate<? extends Entity> predicate) {
		return (List<T>)getEntities(level, area, Entity.class, (Predicate<Entity>)predicate);
	}

	/**
	 * NOTE: The origin entity will be automatically excluded from the search and will not be passed to the predicate
	 */
	public static <T extends Entity> List<T> getEntities(Entity origin, double radius, Class<T> minimumClass, Predicate<T> predicate) {
		return getEntities(origin, radius, radius, radius, minimumClass, predicate);
	}

	/**
	 * NOTE: The origin entity will be automatically excluded from the search and will not be passed to the predicate
	 */
	public static <T extends Entity> List<T> getEntities(Entity origin, double radiusX, double radiusY, double radiusZ, Class<T> minimumClass, Predicate<T> predicate) {
		return getEntities(origin.level(), origin.position(), radiusX, radiusY, radiusZ, minimumClass, ((Predicate<T>)entity -> entity != origin).and(predicate));
	}

	public static <T extends Entity> List<T> getEntities(Level level, Vec3 origin, double radius, Class<T> minimumClass, Predicate<T> predicate) {
		return getEntities(level, AABB.ofSize(origin, radius, radius, radius), minimumClass, predicate);
	}

	public static <T extends Entity> List<T> getEntities(Level level, Vec3 origin, double radiusX, double radiusY, double radiusZ, Class<T> minimumClass, Predicate<T> predicate) {
		return getEntities(level, AABB.ofSize(origin, radiusX, radiusY, radiusZ), minimumClass, predicate);
	}

	public static <T extends Entity> List<T> getEntities(Level level, Vec3 from, Vec3 to, Class<T> minimumClass, Predicate<T> predicate) {
		return getEntities(level, new AABB(from, to), minimumClass, predicate);
	}

	/**
	 * Get all entities within a given region that meet a given criteria.
	 *
	 * @param level         The level to search in
	 * @param bounds        The region to search for entities in
	 * @param minimumClass  The minimum common class (E.G. LivingEntity) that all entities found must be
	 * @param predicate     The predicate determining a valid match
	 * @return              A list of all entities present in the given area that are of at least the minimumClass type, meeting the predicate conditions
	 * @param <T>           The class in which all checked entities should be or extend. More specific typings are more efficient
	 */
	public static <T extends Entity> List<T> getEntities(Level level, AABB bounds, Class<T> minimumClass, Predicate<T> predicate) {
		final List<T> foundEntities = new ObjectArrayList<>();
		final EntityTypeTest<Entity, T> typeTest = makeLazyTypeTest(minimumClass);

		level.getEntities().get(typeTest, bounds, entity -> {
			if (predicate.test(entity))
				foundEntities.add(entity);

			return AbortableIterationConsumer.Continuation.CONTINUE;
		});

		Pair<Collection<? extends Entity>, Function<Entity, ? extends Entity>> partEntities = SBLConstants.SBL_LOADER.getPartEntities(level);

		for (Entity part : partEntities.getFirst()) {
			T entity = typeTest.tryCast(partEntities.getSecond().apply(part));

			if (entity != null && part.getBoundingBox().intersects(bounds) && predicate.test(entity))
				foundEntities.add(entity);
		}

		return foundEntities;
	}

	/**
	 * NOTE: The origin entity will be automatically excluded from the search and will not be passed to the predicate
	 */
	public static <T extends Player> Optional<T> findPlayer(Entity origin, double radius, Predicate<Player> predicate) {
		return findPlayer(origin, radius, radius, radius, predicate);
	}

	/**
	 * NOTE: The origin entity will be automatically excluded from the search and will not be passed to the predicate
	 */
	public static <T extends Player> Optional<T> findPlayer(Entity origin, double radiusX, double radiusY, double radiusZ, Predicate<Player> predicate) {
		return findPlayer(origin.level(), origin.position(), radiusX, radiusY, radiusZ, ((Predicate<Player>)entity -> entity != origin).and(predicate));
	}

	public static <T extends Player> Optional<T> findPlayer(Level level, Vec3 origin, double radius, Predicate<Player> predicate) {
		return findPlayer(level, AABB.ofSize(origin, radius, radius, radius), predicate);
	}

	public static <T extends Player> Optional<T> findPlayer(Level level, Vec3 origin, double radiusX, double radiusY, double radiusZ, Predicate<Player> predicate) {
		return findPlayer(level, AABB.ofSize(origin, radiusX, radiusY, radiusZ), predicate);
	}

	public static <T extends Player> Optional<T> findPlayer(Level level, Vec3 from, Vec3 to, Predicate<Player> predicate) {
		return findPlayer(level, new AABB(from, to), predicate);
	}

	/**
	 * Find a single player within a given region that meets a given criteria.
	 * <p>
	 * This is a short-circuiting operation that ends immediately upon finding a matching target, offering optimal efficiency when searching for a specific player.
	 *
	 * @param level      The level to search in
	 * @param bounds     The region to search for players in
	 * @param predicate  The predicate determining a valid match
	 * @return           The first player that meets the predicate conditions, or an empty {@link Optional} if none found
	 */
	public static <T extends Player> Optional<T> findPlayer(Level level, AABB bounds, Predicate<Player> predicate) {
		for (Player player : level.players()) {
			if (bounds.contains(player.position()) && predicate.test(player))
				return (Optional<T>)Optional.of(player);
		}

		return Optional.empty();
	}

	/**
	 * NOTE: The origin entity will be automatically excluded from the search and will not be passed to the predicate
	 */
	public static <T extends Entity> Optional<T> findEntity(Entity origin, double radius, Predicate<Entity> predicate) {
		return findEntity(origin, radius, radius, radius, predicate);
	}

	/**
	 * NOTE: The origin entity will be automatically excluded from the search and will not be passed to the predicate
	 */
	public static <T extends Entity> Optional<T> findEntity(Entity origin, double radiusX, double radiusY, double radiusZ, Predicate<Entity> predicate) {
		return findEntity(origin.level(), origin.position(), radiusX, radiusY, radiusZ, ((Predicate<Entity>)entity -> entity != origin).and(predicate));
	}

	public static <T extends Entity> Optional<T> findEntity(Level level, Vec3 origin, double radius, Predicate<Entity> predicate) {
		return findEntity(level, AABB.ofSize(origin, radius, radius, radius), predicate);
	}

	public static <T extends Entity> Optional<T> findEntity(Level level, Vec3 origin, double radiusX, double radiusY, double radiusZ, Predicate<Entity> predicate) {
		return findEntity(level, AABB.ofSize(origin, radiusX, radiusY, radiusZ), predicate);
	}

	public static <T extends Entity> Optional<T> findEntity(Level level, Vec3 from, Vec3 to, Predicate<Entity> predicate) {
		return findEntity(level, new AABB(from, to), predicate);
	}

	public static <T extends Entity> Optional<T> findEntity(Level level, AABB bounds, Predicate<Entity> predicate) {
		return (Optional<T>)findEntity(level, bounds, Entity.class, predicate);
	}

	/**
	 * NOTE: The origin entity will be automatically excluded from the search and will not be passed to the predicate
	 */
	public static <T extends Entity> Optional<T> findEntity(Entity origin, double radius, Class<T> minimumClass, Predicate<T> predicate) {
		return findEntity(origin, radius, radius, radius, minimumClass, predicate);
	}

	/**
	 * NOTE: The origin entity will be automatically excluded from the search and will not be passed to the predicate
	 */
	public static <T extends Entity> Optional<T> findEntity(Entity origin, double radiusX, double radiusY, double radiusZ, Class<T> minimumClass, Predicate<T> predicate) {
		return findEntity(origin.level(), origin.position(), radiusX, radiusY, radiusZ, minimumClass, ((Predicate<T>)entity -> entity != origin).and(predicate));
	}

	public static <T extends Entity> Optional<T> findEntity(Level level, Vec3 origin, double radius, Class<T> minimumClass, Predicate<T> predicate) {
		return findEntity(level, AABB.ofSize(origin, radius, radius, radius), minimumClass, predicate);
	}

	public static <T extends Entity> Optional<T> findEntity(Level level, Vec3 origin, double radiusX, double radiusY, double radiusZ, Class<T> minimumClass, Predicate<T> predicate) {
		return findEntity(level, AABB.ofSize(origin, radiusX, radiusY, radiusZ), minimumClass, predicate);
	}

	public static <T extends Entity> Optional<T> findEntity(Level level, Vec3 from, Vec3 to, Class<T> minimumClass, Predicate<T> predicate) {
		return findEntity(level, new AABB(from, to), minimumClass, predicate);
	}

	/**
	 * Find a single entity within a given region that meet a given criteria.
	 * <p>
	 * This is a short-circuiting operation that ends immediately upon finding a matching target, offering optimal efficiency when searching for a specific entity.
	 * <p>
	 * If using one of the override methods that do not take a <i>minimumClass</i> argument, it's up to the user to ensure you
	 * type-check the entity instance in the predicate to prevent class exceptions from the returned object
	 *
	 * @param level         The level to search in
	 * @param bounds        The region to search for entities in
	 * @param minimumClass  The minimum common class (E.G. LivingEntity) that all entities found must be
	 * @param predicate     The predicate determining a valid match
	 * @return              The first entity that is of at least the minimumClass type, meeting the predicate conditions, or an empty {@link Optional} if none found
	 * @param <T>           The class in which all checked entities should be or extend. More specific typings are more efficient
	 */
	public static <T extends Entity> Optional<T> findEntity(Level level, AABB bounds, Class<T> minimumClass, Predicate<T> predicate) {
		final AtomicReference<T> foundEntity = new AtomicReference<>(null);
		final EntityTypeTest<Entity, T> typeTest = makeLazyTypeTest(minimumClass);

		level.getEntities().get(typeTest, bounds, entity -> {
			if (predicate.test(entity)) {
				foundEntity.set(entity);

				return AbortableIterationConsumer.Continuation.ABORT;
			}

			return AbortableIterationConsumer.Continuation.CONTINUE;
		});

		if (foundEntity.get() == null) {
			Pair<Collection<? extends Entity>, Function<Entity, ? extends Entity>> partEntities = SBLConstants.SBL_LOADER.getPartEntities(level);

			for (Entity part : partEntities.getFirst()) {
				T entity = typeTest.tryCast(partEntities.getSecond().apply(part));

				if (entity != null && part.getBoundingBox().intersects(bounds) && predicate.test(entity)) {
					foundEntity.set(entity);

					break;
				}
			}
		}

		return Optional.ofNullable(foundEntity.get());
	}

	/**
	 * NOTE: The origin entity will be automatically excluded from the search and will not be passed to the predicate
	 */
	public static <T extends Entity> List<T> findEntities(Entity origin, double radius, int max, Predicate<Entity> predicate) {
		return (List<T>)findEntities(origin, radius, radius, radius, Entity.class, max, predicate);
	}

	/**
	 * NOTE: The origin entity will be automatically excluded from the search and will not be passed to the predicate
	 */
	public static <T extends Entity> List<T> findEntities(Entity origin, double radiusX, double radiusY, double radiusZ, int max, Predicate<Entity> predicate) {
		return (List<T>)findEntities(origin.level(), origin.position(), radiusX, radiusY, radiusZ, Entity.class, max, ((Predicate<Entity>)entity -> entity != origin).and(predicate));
	}

	public static <T extends Entity> List<T> findEntities(Level level, Vec3 origin, double radius, int max, Predicate<Entity> predicate) {
		return (List<T>)findEntities(level, AABB.ofSize(origin, radius, radius, radius), Entity.class, max, predicate);
	}

	public static <T extends Entity> List<T> findEntities(Level level, Vec3 origin, double radiusX, double radiusY, double radiusZ, int max, Predicate<Entity> predicate) {
		return (List<T>)findEntities(level, AABB.ofSize(origin, radiusX, radiusY, radiusZ), Entity.class, max, predicate);
	}

	public static <T extends Entity> List<T> findEntities(Level level, Vec3 from, Vec3 to, int max, Predicate<Entity> predicate) {
		return (List<T>)findEntities(level, new AABB(from, to), Entity.class, max, predicate);
	}

	/**
	 * NOTE: The origin entity will be automatically excluded from the search and will not be passed to the predicate
	 */
	public static <T extends Entity> List<T> findEntities(Entity origin, double radius, Class<T> minimumClass, int max, Predicate<? super T> predicate) {
		return findEntities(origin, radius, radius, radius, minimumClass, max, predicate);
	}

	/**
	 * NOTE: The origin entity will be automatically excluded from the search and will not be passed to the predicate
	 */
	public static <T extends Entity> List<T> findEntities(Entity origin, double radiusX, double radiusY, double radiusZ, Class<T> minimumClass, int max, Predicate<? super T> predicate) {
		return findEntities(origin.level(), origin.position(), radiusX, radiusY, radiusZ, minimumClass, max, ((Predicate<T>)entity -> entity != origin).and(predicate));
	}

	public static <T extends Entity> List<T> findEntities(Level level, Vec3 origin, double radius, Class<T> minimumClass, int max, Predicate<? super T> predicate) {
		return findEntities(level, AABB.ofSize(origin, radius, radius, radius), minimumClass, max, predicate);
	}

	public static <T extends Entity> List<T> findEntities(Level level, Vec3 origin, double radiusX, double radiusY, double radiusZ, Class<T> minimumClass, int max, Predicate<? super T> predicate) {
		return findEntities(level, AABB.ofSize(origin, radiusX, radiusY, radiusZ), minimumClass, max, predicate);
	}

	public static <T extends Entity> List<T> findEntities(Level level, Vec3 from, Vec3 to, Class<T> minimumClass, int max, Predicate<? super T> predicate) {
		return findEntities(level, new AABB(from, to), minimumClass, max, predicate);
	}

	/**
	 * Find up to a predefined number of entity instances in the given region of the given minimum common class type, predicated by the provided predicate.
	 * <p>
	 * This is a short-circuiting operation that ends immediately upon finding enough matching targets, offering optimal efficiency when searching for a specific entities.
	 * <p>
	 * If using one of the override methods that do not take a <i>minimumClass</i> argument, it's up to the user to ensure you
	 * type-check the entity instance in the predicate to prevent class exceptions from the returned object
	 *
	 * @param level         The level to search in
	 * @param bounds        The region to search for entities in
	 * @param minimumClass  The minimum common class (E.G. LivingEntity) that all entities found must be
	 * @param max           The maximum amount of entities to search for
	 * @param predicate     The predicate determining a valid match
	 * @return              A list of all entities (up to max amount) that are of at least the minimumClass type, meeting the predicate conditions
	 * @param <T>           The class in which all checked entities should be or extend. More specific typings are more efficient
	 */
	public static <T extends Entity> List<T> findEntities(Level level, AABB bounds, Class<T> minimumClass, int max, Predicate<? super T> predicate) {
		final List<T> foundEntities = new ObjectArrayList<>(max);
		final EntityTypeTest<Entity, T> typeTest = makeLazyTypeTest(minimumClass);

		level.getEntities().get(typeTest, bounds, entity -> {
			if (predicate.test(entity)) {
				foundEntities.add(entity);

				if (foundEntities.size() >= max)
					return AbortableIterationConsumer.Continuation.ABORT;
			}

			return AbortableIterationConsumer.Continuation.CONTINUE;
		});

		if (foundEntities.size() < max) {
			Pair<Collection<? extends Entity>, Function<Entity, ? extends Entity>> partEntities = SBLConstants.SBL_LOADER.getPartEntities(level);

			for (Entity part : partEntities.getFirst()) {
				T entity = typeTest.tryCast(partEntities.getSecond().apply(part));

				if (entity != null && part.getBoundingBox().intersects(bounds) && predicate.test(entity)) {
					foundEntities.add(entity);

					if (foundEntities.size() >= max)
						break;
				}
			}
		}

		return foundEntities;
	}

	/**
	 * NOTE: The returned stream may include the origin entity in its contents
	 */
	public static Stream<Entity> streamEntities(Entity origin, double radius) {
		return streamEntities(origin, radius, radius, radius);
	}

	/**
	 * NOTE: The returned stream may include the origin entity in its contents
	 */
	public static Stream<Entity> streamEntities(Entity origin, double radiusX, double radiusY, double radiusZ) {
		return streamEntities(origin.level(), origin.position(), radiusX, radiusY, radiusZ);
	}

	public static Stream<Entity> streamEntities(Level level, Vec3 origin, double radius) {
		return streamEntities(level, origin, radius, radius, radius);
	}

	public static Stream<Entity> streamEntities(Level level, Vec3 origin, double radiusX, double radiusY, double radiusZ) {
		return streamEntities(level, AABB.ofSize(origin, radiusX, radiusY, radiusZ), Entity.class);
	}

	public static Stream<Entity> streamEntities(Level level, Vec3 fromPos, Vec3 toPos) {
		return streamEntities(level, new AABB(fromPos, toPos), Entity.class);
	}

	/**
	 * NOTE: The returned stream may include the origin entity in its contents
	 */
	public static <T extends Entity> Stream<T> streamEntities(Entity origin, double radius, Class<T> minimumClass) {
		return streamEntities(origin, radius, radius, radius, minimumClass);
	}

	/**
	 * NOTE: The returned stream may include the origin entity in its contents
	 */
	public static <T extends Entity> Stream<T> streamEntities(Entity origin, double radiusX, double radiusY, double radiusZ, Class<T> minimumClass) {
		return streamEntities(origin.level(), origin.position(), radiusX, radiusY, radiusZ, minimumClass);
	}

	public static <T extends Entity> Stream<T> streamEntities(Level level, Vec3 origin, double radius, Class<T> minimumClass) {
		return streamEntities(level, origin, radius, radius, radius, minimumClass);
	}

	public static <T extends Entity> Stream<T> streamEntities(Level level, Vec3 origin, double radiusX, double radiusY, double radiusZ, Class<T> minimumClass) {
		return streamEntities(level, AABB.ofSize(origin, radiusX, radiusY, radiusZ), minimumClass);
	}

	public static <T extends Entity> Stream<T> streamEntities(Level level, Vec3 fromPos, Vec3 toPos, Class<T> minimumClass) {
		return streamEntities(level, new AABB(fromPos, toPos), minimumClass);
	}

	/**
	 * Create a stream of all entities in the given area for the given minimum common class type.
	 * <p>
	 * This is usually less efficient than the standard List lookups, however you may want to use this in a few circumstances:
	 * <ol>
	 *     <li>You specifically need/want a stream that can properly short-circuit for better performance
	 *     <li>You are looking for a number of entities that meet some criteria but may quit searching before checking all of them
	 * </ol>
	 *
	 * @param level         The level to search in
	 * @param bounds        The region to search for entities in
	 * @param minimumClass  The minimum common class (E.G. LivingEntity) that all entities found must be
	 * @return              A stream of all entities in the bounds of the minimum class type
	 * @param <T>           The class in which all returned entities should be or extend. More specific typings are more efficient
	 */
	public static <T extends Entity> Stream<T> streamEntities(Level level, AABB bounds, Class<T> minimumClass) {
		level.getProfiler().incrementCounter("getEntities");

		if (!(level.getEntities() instanceof LevelEntityGetterAdapter<Entity> entities))
			return (minimumClass == Entity.class ? (List)getEntities(level, bounds, entity -> true) : getEntities(level, bounds, minimumClass, entity -> true)).stream();

		final EntitySectionStorage<Entity> entitySectionStorage = entities.sectionStorage;
		final EntityTypeTest<Entity, T> clazzLookup = makeLazyTypeTest(minimumClass);
		final int minSectionX = SectionPos.posToSectionCoord(bounds.minX - 2);
		final int minSectionY = SectionPos.posToSectionCoord(bounds.minY - 4);
		final int minSectionZ = SectionPos.posToSectionCoord(bounds.minZ - 2);
		final int maxSectionX = SectionPos.posToSectionCoord(bounds.maxX + 2);
		final int maxSectionY = SectionPos.posToSectionCoord(bounds.maxY);
		final int maxSectionZ = SectionPos.posToSectionCoord(bounds.maxZ + 2);
		final Pair<Collection<? extends Entity>, Function<Entity, ? extends Entity>> partEntities = SBLConstants.SBL_LOADER.getPartEntities(level);
		final Stream<T> stream = IntStream.rangeClosed(minSectionX, maxSectionX)
				.mapToObj(sectionX -> entitySectionStorage.sectionIds.subSet(SectionPos.asLong(sectionX, 0, 0), SectionPos.asLong(sectionX, -1, -1)).iterator())
				.<Long>mapMulti((iterator, consumer) -> consumer.accept(iterator.nextLong()))
				.filter(sectionId -> {
					int sectionYPos = SectionPos.y(sectionId);

					if (sectionYPos < minSectionY || sectionYPos > maxSectionY)
						return false;

					int sectionZPos = SectionPos.z(sectionId);

                    return sectionZPos >= minSectionZ && sectionZPos <= maxSectionZ;
                })
				.map(entitySectionStorage::getSection)
				.filter(section -> section != null && !section.isEmpty() && section.getStatus().isAccessible())
				.map(section -> section.storage.find(clazzLookup.getBaseClass()))
				.filter(collection -> !collection.isEmpty())
				.<Entity>mapMulti(Iterable::forEach)
				.map(clazzLookup::tryCast)
				.filter(entity -> entity != null && entity.getBoundingBox().intersects(bounds));

		return partEntities.getFirst().isEmpty() ?
				stream :
				Stream.concat(
						stream,
						partEntities.getFirst().stream()
								.map(entity -> Pair.of(entity, clazzLookup.tryCast(partEntities.getSecond().apply(entity))))
								.filter(entity -> entity.getFirst().getBoundingBox().intersects(bounds))
								.map(Pair::getSecond));
	}

	/**
	 * Internal method for wrapping the {@link EntityTypeTest} system to account for root {@link Entity} types,
	 * which don't need wasteful instance checks and casting for every instance
	 */
	@ApiStatus.Internal
	private static <T extends Entity> EntityTypeTest<Entity, T> makeLazyTypeTest(Class<T> forClass) {
		if (forClass != Entity.class)
			return EntityTypeTest.forClass(forClass);

		return (EntityTypeTest<Entity, T>)new EntityTypeTest<Entity, Entity>() {
			@Nullable
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
