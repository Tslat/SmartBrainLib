package net.tslat.smartbrainlib.library.object;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.ThreadSafeLegacyRandomSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.SBLConstants;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.BiPredicate;

/// Wrapper around [RandomSource] to offer extended functionality and legible random functionality.
///
/// It uses a `RandomSource` as a base, maintaining compatibility with vanilla random number generation.
///
/// Code borrowed from `TslatModdingExtensions`
public class EasyRandom implements RandomSource  {
	protected final RandomSource random;

	protected EasyRandom(RandomSource random) {
		this.random = random;
	}

	/// Create a new EasyRandom instance
	@SuppressWarnings("deprecation")
    public static EasyRandom create() {
		return wrap(RandomSource.createThreadSafe());
	}

	/// Create a new EasyRandom instance, initially seeded with the given value
	@SuppressWarnings("deprecation")
    public static EasyRandom createSeeded(long seed) {
		return wrap(new ThreadSafeLegacyRandomSource(seed));
	}

	/// Create a new EasyRandom instance, wrapping an existing [RandomSource]
	public static EasyRandom wrap(RandomSource random) {
		return new EasyRandom(random);
	}

	/// Create a new thread-sensitive EasyRandom instance.
	///
	/// This random must only be used in one thread; however, it may benefit from additional performance
	public static EasyRandom createSingleThread() {
		return wrap(RandomSource.createThreadLocalInstance());
	}

	/// Create a new thread-sensitive EasyRandom instance, initially seeded with the given value
	///
	/// This random must only be used in one thread; however, it may benefit from additional performance
	public static EasyRandom createSingleThreadSeeded(long seed) {
		return wrap(RandomSource.createThreadLocalInstance(seed));
	}

	/// Return a random [Integer] value, including both negative and positive values
	public int wholeNumber() {
		return this.random.nextInt();
	}

	/// Return a random [Double] value, including both negative and positive values
	public float floatValue() {
		return this.random.nextFloat();
	}

	/// Return a random [Float] value, including both negative and positive values
	///
	/// Note: [#floatValue()] is typically significantly more performant and should be used
	/// where a double isn't specifically required
	public double doubleValue() {
		return this.random.nextDouble();
	}

	/// Return a random value distributed using Gaussian (Normal) distribution
	///
	/// @see <a href="https://en.wikipedia.org/wiki/Normal_distribution">Normal Distribution</a>
	public double gaussianValue() {
		return this.random.nextGaussian();
	}

	/// Return a random whole number between 0 and input bound (exclusive)
	public int numberUpTo(int upperBoundExc) {
		return this.random.nextInt(upperBoundExc);
	}

	/// Return a random value (non-whole) between 0 and the input bound (exclusive)
	public float valueUpTo(float upperBoundExc) {
		return floatValue() * upperBoundExc;
	}

	/// Return a random value (non-whole) between 0 and the input bound (exclusive)
	///
	/// Note: [#valueUpTo(float)] is typically significantly more performant and should be used
	/// where a double isn't specifically required
	public double valueUpTo(double upperBoundExc) {
		return doubleValue() * upperBoundExc;
	}

	/// Return either true or false, with equal chances of either
	public boolean fiftyFifty() {
		return this.random.nextBoolean();
	}

	/// Return true if a `1 in n` chance check passes
	///
	/// E.G. `n=5: 1/5 ≡ 20%` chance to return true
	public boolean oneInNChance(int n) {
		if (n <= 0)
			return false;

		return n == 1 || floatValue() < 1 / (float)n;
	}

	/// Return true if a percentage chance check passes.
	///
	/// E.G. `n=0.2: 20%` chance to return true
	///
	/// @param percentChance Input chance value, between 0 and 1 (inclusive)
	public boolean percentChance(float percentChance) {
		if (percentChance <= 0)
			return false;

		return percentChance >= 1 || floatValue() < percentChance;
	}

	/// Return true if a percentage chance check passes.
	///
	/// E.G. `n=0.2: 20%` chance to return true
	///
	/// Note: [#percentChance(float)] is typically significantly more performant and should be used
	/// where a double isn't specifically required
	///
	/// @param percentChance Input chance value, between 0 and 1 (inclusive)
	public boolean percentChance(double percentChance) {
		if (percentChance <= 0)
			return false;

		return percentChance >= 1 || doubleValue() < percentChance;
	}

	/// Return a random value distributed using Gaussian (Normal) distribution, scaled using an input value
	///
	/// Note that due to the nature of Gaussian distribution, a scale value does not imply that no number will
	/// generate larger than the specified value.
	///
	/// @see <a href="https://en.wikipedia.org/wiki/Normal_distribution">Normal Distribution</a>
	public double scaledGaussianValue(double scale) {
		if (scale == 0)
			return 0;

		return gaussianValue() * scale;
	}

	/// Return a random value distributed using triangular distribution, with a default bound of 1
	///
	/// This can be used in a similar manner to Gaussian values, but with significantly less overhead, comparatively
	///
	/// @see <a href="https://en.wikipedia.org/wiki/Triangular_distribution">Triangular Distribution</a>
	public double triangularValue() {
		return scaledTriangularValue(1);
	}

	/// Return a random value distributed using triangular distribution centered on 0, specifing the delta range
	public double scaledTriangularValue(double maxDelta) {
		return scaledTriangularValue(0, maxDelta);
	}

	/// Return a random value distributed using triangular distribution, specifying the center point and delta range
	public double scaledTriangularValue(double mean, double maxDelta) {
		if (maxDelta == 0)
			return mean;

		return this.random.triangle(mean, maxDelta);
	}

	/// Return a random number between the minimum (inclusive) and maximum (exclusive) bounds
	public int numberBetween(int minInc, int maxExc) {
		return minInc + Mth.floor(floatValue() * (1 + maxExc - minInc));
	}

	/// Return a random value (non-whole) between the minimum (inclusive) and maximum (exclusive) bounds
	public float valueBetween(float minInc, float maxExc) {
		return minInc + floatValue() * (maxExc - minInc);
	}

	/// Return a random value (non-whole) between the minimum (inclusive) and maximum (exclusive) bounds
	///
	/// Note: [#valueBetween(float,float)] is typically significantly more performant and should be used
	/// where a double isn't specifically required
	public double valueBetween(double minInc, double maxExc) {
		return minInc + doubleValue() * (maxExc - minInc);
	}

	/// Return a randomly selected element from the provided array of options, or null if the options array is empty
	@SafeVarargs
    public final <T> T selection(T... options) {
		if (options.length == 0)
			throw new IllegalArgumentException("Cannot select random element from empty array!");

		return options[numberUpTo(options.length)];
	}

	/// Return a randomly selected element from the provided list of options
	public <T> T selection(List<T> options) {
		return options.get(numberUpTo(options.size()));
	}

	/// Return a randomly selected element from the provided collection of options
	public <T> T selection(Collection<T> options) {
        //noinspection unchecked
        return (T)selection(options.toArray());
	}

	/// Return a random BlockPos within a given radius of a central position
	public BlockPos positionWithinRange(BlockPos centerPos, int radius) {
		return positionWithinRange(centerPos, radius, radius, radius);
	}

	/// Return a random BlockPos within a given radius of a central position
	public BlockPos positionWithinRange(BlockPos centerPos, int xRadius, int yRadius, int zRadius) {
		return positionWithinRange(centerPos, xRadius, yRadius, zRadius, false, null);
	}

	/// Return a random BlockPos within a given radius of a central position, then adapting the resultant position down to the world's surface.
	///
	/// Useful for randomly generating nearby spawn positions or similar
	public BlockPos positionWithinRange(BlockPos centerPos, int radius, boolean safeSurfacePlacement, @Nullable Level level) {
		return positionWithinRange(centerPos, radius, radius, radius, 0, 0, 0, safeSurfacePlacement, level, 1, null);
	}

	/// Return a random BlockPos within a given radius of a central position, then adapting the resultant position down to the world's surface.
	///
	/// Useful for randomly generating nearby spawn positions or similar
	public BlockPos positionWithinRange(BlockPos centerPos, int xRadius, int yRadius, int zRadius, boolean safeSurfacePlacement, @Nullable Level level) {
		return positionWithinRange(centerPos, xRadius, yRadius, zRadius, 0, 0, 0, safeSurfacePlacement, level, 1, null);
	}

	/// Return a random BlockPos within a given radius of a central position, with options to adapt the resultant position down to the world's surface, or predicate the position.
	///
	/// If predicating the position, the call may return the center position if no successful predication is performed within the provided number of tries.
	public BlockPos positionWithinRange(BlockPos centerPos, int xRadius, int yRadius, int zRadius, int minSpreadX, int minSpreadY, int minSpreadZ, boolean safeSurfacePlacement,
										@Nullable Level level, int tries, @Nullable BiPredicate<BlockState, BlockPos> statePredicate) {
		final BlockPos.MutableBlockPos mutablePos = centerPos.mutable();
		final int minX = Math.min(minSpreadX, xRadius);
		final int minY = Math.min(minSpreadY, yRadius);
		final int minZ = Math.min(minSpreadZ, zRadius);
		final int maxX = Math.max(minSpreadX, xRadius);
		final int maxY = Math.max(minSpreadY, yRadius);
		final int maxZ = Math.max(minSpreadZ, zRadius);

		for (int i = 0; i < tries; i++) {
			int x = numberBetween(minX, maxX + 1) * (fiftyFifty() ? -1 : 1);
			int y = numberBetween(minY, maxY + 1) * (fiftyFifty() ? -1 : 1);
			int z = numberBetween(minZ, maxZ + 1) * (fiftyFifty() ? -1 : 1);

			mutablePos.setWithOffset(centerPos, x, y, z);

			if (level != null && safeSurfacePlacement)
				mutablePos.set(level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, mutablePos));

			if (level == null || statePredicate == null || statePredicate.test(level.getBlockState(mutablePos), mutablePos))
				return mutablePos.immutable();
		}

		return centerPos;
	}

	/// Return a random position within a 3d radius of the origin
	public Vec3 positionInRadius(Vec3 origin, double radius) {
		return positionInRadius(origin, radius, radius, radius);
	}

	/// Return a random position within a radius of the origin
	public Vec3 positionInRadius(Vec3 origin, double xRadius, double yRadius, double zRadius) {
		return origin.add(valueBetween(-xRadius, xRadius), valueBetween(-yRadius, yRadius), valueBetween(-zRadius, zRadius));
	}

	/// Return a random position within a radius of the origin, using scaled Gaussian (Normal) distribution
	///
	/// @see <a href="https://en.wikipedia.org/wiki/Normal_distribution">Normal Distribution</a>
	public Vec3 gaussianOffset(Vec3 origin, double xScale, double yScale, double zScale) {
		return origin.add(scaledGaussianValue(xScale), scaledGaussianValue(yScale), scaledGaussianValue(zScale));
	}

	/// Return a random position within a radius of the origin, using scaled Triangular distribution
	///
	/// @see <a href="https://en.wikipedia.org/wiki/Triangular_distribution">Triangular Distribution</a>
	public Vec3 triangleOffset(Vec3 origin, double xScale, double yScale, double zScale) {
		return origin.add(scaledTriangularValue(xScale), scaledTriangularValue(yScale), scaledTriangularValue(zScale));
	}

	/// Return a random position within an [AABB]
	public Vec3 positionInBounds(AABB bounds) {
		return new Vec3(valueBetween(bounds.minX, bounds.maxX), valueBetween(bounds.minY, bounds.maxY), valueBetween(bounds.minZ, bounds.maxZ));
	}

	/// Return a random position within an [Entity], using its [bounding box](AABB) as the basis for bounds, but additionally including child parts if the entity
	/// is a multipart entity
	public Vec3 positionInEntity(Entity entity) {
		final Entity[] partEntities = SBLConstants.PLATFORM.getPartEntities(entity);

		if (partEntities.length > 0 && !oneInNChance(partEntities.length))
			return positionInBounds(selection(partEntities).getBoundingBox());

		return positionInBounds(entity.getBoundingBox());
	}

	/// Return a random position somewhere on the line between two positions
	public Vec3 positionBetween(Vec3 from, Vec3 to) {
		return from.lerp(to, doubleValue());
	}

	/// Return a random colour, using a randomly selected hue, with defined saturation and brightness.
	///
	/// Result is in ARGB format
	public int colour(float saturation, float brightness, float alpha) {
		return Mth.hsvToArgb(floatValue(), saturation, brightness, Mth.ceil(alpha * 255));
	}

	/// Return a random colour
	///
	/// Result is in ARGB format
	public int colour() {
		return colour(1, 1, 1);
	}

	//<editor-fold defaultstate="collapsed" desc="<Overridden Methods>">
	@ApiStatus.Internal
	@Override
	public EasyRandom fork() {
		return new EasyRandom(this.random.fork());
	}

	@ApiStatus.Internal
	@Override
	public PositionalRandomFactory forkPositional() {
		return this.random.forkPositional();
	}

	@ApiStatus.Internal
	@Override
	public void setSeed(long seed) {
		this.random.setSeed(seed);
	}

	@ApiStatus.Internal
	@Override
	public int nextInt() {
		return this.random.nextInt();
	}

	@ApiStatus.Internal
	@Override
	public int nextInt(int upperLimit) {
		return this.random.nextInt(upperLimit);
	}

	@ApiStatus.Internal
	@Override
	public long nextLong() {
		return this.random.nextLong();
	}

	@ApiStatus.Internal
	@Override
	public boolean nextBoolean() {
		return this.random.nextBoolean();
	}

	@ApiStatus.Internal
	@Override
	public float nextFloat() {
		return this.random.nextFloat();
	}

	@ApiStatus.Internal
	@Override
	public double nextDouble() {
		return this.random.nextDouble();
	}

	@ApiStatus.Internal
	@Override
	public double nextGaussian() {
		return this.random.nextGaussian();
	}
	//</editor-fold>
}