package net.tslat.smartbrainlib.util;

import com.google.common.base.Suppliers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.library.object.EasyRandom;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

/// Utility class utilising [EasyRandom] to facilitate various methods for sourcing randomness
///
/// Code borrowed from `TslatModdingExtensions`
public final class RandomUtil  {
	private static final Supplier<EasyRandom> RANDOM = Suppliers.memoize(EasyRandom::create);

	/// Return a random [Integer] value, including both negative and positive values
	public static int wholeNumber() {
		return RANDOM.get().wholeNumber();
	}

	/// Return a random [Double] value, including both negative and positive values
	public static float floatValue() {
		return RANDOM.get().floatValue();
	}

	/// Return a random [Float] value, including both negative and positive values
	///
	/// Note: [#floatValue()] is typically significantly more performant and should be used
	/// where a double isn't specifically required
	public static double doubleValue() {
		return RANDOM.get().doubleValue();
	}

	/// Return a random value distributed using Gaussian (Normal) distribution
	///
	/// @see <a href="https://en.wikipedia.org/wiki/Normal_distribution">Normal Distribution</a>
	public static double gaussianValue() {
		return RANDOM.get().gaussianValue();
	}

	/// Return a random whole number between 0 and input bound (exclusive)
	public static int numberUpTo(int upperBoundExc) {
		return RANDOM.get().numberUpTo(upperBoundExc);
	}

	/// Return a random value (non-whole) between 0 and the input bound (exclusive)
	public static float valueUpTo(float upperBoundExc) {
		return RANDOM.get().valueUpTo(upperBoundExc);
	}

	/// Return a random value (non-whole) between 0 and the input bound (exclusive)
	///
	/// Note: [#valueUpTo(float)] is typically significantly more performant and should be used
	/// where a double isn't specifically required
	public static double valueUpTo(double upperBoundExc) {
		return RANDOM.get().valueUpTo(upperBoundExc);
	}

	/// Return either true or false, with equal chances of either
	public static boolean fiftyFifty() {
		return RANDOM.get().fiftyFifty();
	}

	/// Return true if a `1 in n` chance check passes
	///
	/// E.G. `n=5: 1/5 ≡ 20%` chance to return true
	public static boolean oneInNChance(int n) {
		return RANDOM.get().oneInNChance(n);
	}

	/// Return true if a percentage chance check passes.
	///
	/// E.G. `n=0.2: 20%` chance to return true
	///
	/// @param percentChance Input chance value, between 0 and 1 (inclusive)
	public static boolean percentChance(float percentChance) {
		return RANDOM.get().percentChance(percentChance);
	}

	/// Return true if a percentage chance check passes.
	///
	/// E.G. `n=0.2: 20%` chance to return true
	///
	/// Note: [#percentChance(float)] is typically significantly more performant and should be used
	/// where a double isn't specifically required
	///
	/// @param percentChance Input chance value, between 0 and 1 (inclusive)
	public static boolean percentChance(double percentChance) {
		return RANDOM.get().percentChance(percentChance);
	}

	/// Return a random value distributed using Gaussian (Normal) distribution, scaled using an input value
	///
	/// Note that due to the nature of Gaussian distribution, a scale value does not imply that no number will
	/// generate larger than the specified value.
	///
	/// @see <a href="https://en.wikipedia.org/wiki/Normal_distribution">Normal Distribution</a>
	public static double scaledGaussianValue(double scale) {
		return RANDOM.get().scaledGaussianValue(scale);
	}

	/// Return a random value distributed using triangular distribution, with a default bound of 1
	///
	/// This can be used in a similar manner to Gaussian values, but with significantly less overhead, comparatively
	///
	/// @see <a href="https://en.wikipedia.org/wiki/Triangular_distribution">Triangular Distribution</a>
	public static double triangularValue() {
		return RANDOM.get().triangularValue();
	}

	/// Return a random value distributed using triangular distribution centered on 0, specifing the delta range
	public static double scaledTriangularValue(double maxDelta) {
		return RANDOM.get().scaledTriangularValue(maxDelta);
	}

	/// Return a random value distributed using triangular distribution, specifying the center point and delta range
	public static double scaledTriangularValue(double mean, double maxDelta) {
		return RANDOM.get().scaledTriangularValue(mean, maxDelta);
	}

	/// Return a random number between the minimum (inclusive) and maximum (exclusive) bounds
	public static int numberBetween(int minInc, int maxExc) {
		return RANDOM.get().numberBetween(minInc, maxExc);
	}

	/// Return a random value (non-whole) between the minimum (inclusive) and maximum (exclusive) bounds
	public static float valueBetween(float minInc, float maxExc) {
		return RANDOM.get().valueBetween(minInc, maxExc);
	}

	/// Return a random value (non-whole) between the minimum (inclusive) and maximum (exclusive) bounds
	///
	/// Note: [#valueBetween(float,float)] is typically significantly more performant and should be used
	/// where a double isn't specifically required
	public static double valueBetween(double minInc, double maxExc) {
		return RANDOM.get().valueBetween(minInc, maxExc);
	}

	/// Return a randomly selected element from the provided array of options, or null if the options array is empty
	@SafeVarargs
	public static <T> T selection(T... options) {
		return RANDOM.get().selection(options);
	}

	/// Return a randomly selected element from the provided list of options
	public static <T> T selection(List<T> options) {
		return RANDOM.get().selection(options);
	}

	/// Return a randomly selected element from the provided collection of options
	public static <T> T selection(Collection<T> options) {
		return RANDOM.get().selection(options);
	}

	/// Return a random BlockPos within a given radius of a central position
	public static BlockPos positionWithinRange(BlockPos centerPos, int radius) {
		return RANDOM.get().positionWithinRange(centerPos, radius);
	}

	/// Return a random BlockPos within a given radius of a central position
	public static BlockPos positionWithinRange(BlockPos centerPos, int xRadius, int yRadius, int zRadius) {
		return RANDOM.get().positionWithinRange(centerPos, xRadius, yRadius, zRadius);
	}

	/// Return a random BlockPos within a given radius of a central position, then adapting the resultant position down to the world's surface.
	///
	/// Useful for randomly generating nearby spawn positions or similar
	public static BlockPos positionWithinRange(BlockPos centerPos, int radius, boolean safeSurfacePlacement, @Nullable Level level) {
		return RANDOM.get().positionWithinRange(centerPos, radius, safeSurfacePlacement, level);
	}

	/// Return a random BlockPos within a given radius of a central position, then adapting the resultant position down to the world's surface.
	///
	/// Useful for randomly generating nearby spawn positions or similar
	public static BlockPos positionWithinRange(BlockPos centerPos, int xRadius, int yRadius, int zRadius, boolean safeSurfacePlacement, @Nullable Level level) {
		return RANDOM.get().positionWithinRange(centerPos, xRadius, yRadius, zRadius, safeSurfacePlacement, level);
	}

	/// Return a random BlockPos within a given radius of a central position, with options to adapt the resultant position down to the world's surface, or predicate the position.
	///
	/// If predicating the position, the call may return the center position if no successful predication is performed within the provided number of tries.
	public static BlockPos positionWithinRange(BlockPos centerPos, int xRadius, int yRadius, int zRadius, int minSpreadX, int minSpreadY, int minSpreadZ, boolean safeSurfacePlacement,
										@Nullable Level level, int tries, @Nullable BiPredicate<BlockState, BlockPos> statePredicate) {
		return RANDOM.get().positionWithinRange(centerPos, xRadius, yRadius, zRadius, minSpreadX, minSpreadY, minSpreadZ, safeSurfacePlacement, level, tries, statePredicate);
	}

	/// Return a random position within a 3d radius of the origin
	public static Vec3 positionInRadius(Vec3 origin, double radius) {
		return RANDOM.get().positionInRadius(origin, radius);
	}

	/// Return a random position within a radius of the origin
	public static Vec3 positionInRadius(Vec3 origin, double xRadius, double yRadius, double zRadius) {
		return RANDOM.get().positionInRadius(origin, xRadius, yRadius, zRadius);
	}

	/// Return a random position within a radius of the origin, using scaled Gaussian (Normal) distribution
	///
	/// @see <a href="https://en.wikipedia.org/wiki/Normal_distribution">Normal Distribution</a>
	public static Vec3 gaussianOffset(Vec3 origin, double xScale, double yScale, double zScale) {
		return RANDOM.get().gaussianOffset(origin, xScale, yScale, zScale);
	}

	/// Return a random position within a radius of the origin, using scaled Triangular distribution
	///
	/// @see <a href="https://en.wikipedia.org/wiki/Triangular_distribution">Triangular Distribution</a>
	public static Vec3 triangleOffset(Vec3 origin, double xScale, double yScale, double zScale) {
		return RANDOM.get().triangleOffset(origin, xScale, yScale, zScale);
	}

	/// Return a random position within an [AABB]
	public static Vec3 positionInBounds(AABB bounds) {
		return RANDOM.get().positionInBounds(bounds);
	}

	/// Return a random position within an [Entity], using its [bounding box](AABB) as the basis for bounds, but additionally including child parts if the entity
	/// is a multipart entity
	public static Vec3 positionInEntity(Entity entity) {
		return RANDOM.get().positionInEntity(entity);
	}

	/// Return a random position somewhere on the line between two positions
	public static Vec3 positionBetween(Vec3 from, Vec3 to) {
		return RANDOM.get().positionBetween(from, to);
	}

	/// Return a random colour, using a randomly selected hue, with defined saturation and brightness.
	///
	/// Result is in ARGB format
	public static int colour(float saturation, float brightness, float alpha) {
		return RANDOM.get().colour(saturation, brightness, alpha);
	}

	/// Return a random colour
	///
	/// Result is in ARGB format
	public static int colour() {
		return RANDOM.get().colour();
	}
}