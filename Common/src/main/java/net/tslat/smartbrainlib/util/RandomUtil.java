package net.tslat.smartbrainlib.util;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiPredicate;

/**
 * Utility class for easy and legible random functionality.
 */
public final class RandomUtil {
	public static final EasyRandom RANDOM = new EasyRandom(RandomSource.createThreadSafe());

	public static ThreadLocalRandom getRandomInstance() {
		return ThreadLocalRandom.current();
	}

	public static boolean fiftyFifty() {
		return RANDOM.fiftyFifty();
	}

	public static boolean oneInNChance(int n) {
		return RANDOM.oneInNChance(n);
	}

	public static boolean percentChance(double percentChance) {
		return RANDOM.percentChance(percentChance);
	}

	public static boolean percentChance(float percentChance) {
		return RANDOM.percentChance(percentChance);
	}

	public static int randomNumberUpTo(int upperBound) {
		return RANDOM.randomNumberUpTo(upperBound);
	}

	public static float randomValueUpTo(float upperBound) {
		return RANDOM.randomValueUpTo(upperBound);
	}

	public static double randomValueUpTo(double upperBound) {
		return RANDOM.randomValueUpTo(upperBound);
	}

	public static double randomGaussianValue() {
		return RANDOM.randomGaussianValue();
	}

	public static double randomScaledGaussianValue(double scale) {
		return RANDOM.randomScaledGaussianValue(scale);
	}

	public static int randomNumberBetween(int min, int max) {
		return RANDOM.randomNumberBetween(min, max);
	}

	public static double randomValueBetween(double min, double max) {
		return RANDOM.randomValueBetween(min, max);
	}

	public static <T> T getRandomSelection(@NotNull T... options) {
		return RANDOM.getRandomSelection(options);
	}

	public static <T> T getRandomSelection(@NotNull List<T> options) {
		return RANDOM.getRandomSelection(options);
	}
	
	@NotNull
	public static BlockPos getRandomPositionWithinRange(BlockPos centerPos, int xRadius, int yRadius, int zRadius) {
		return RANDOM.getRandomPositionWithinRange(centerPos, xRadius, yRadius, zRadius);
	}

	@NotNull
	public static BlockPos getRandomPositionWithinRange(BlockPos centerPos, int xRadius, int yRadius, int zRadius, boolean safeSurfacePlacement, Level world) {
		return RANDOM.getRandomPositionWithinRange(centerPos, xRadius, yRadius, zRadius, safeSurfacePlacement, world);
	}

	@NotNull
	public static BlockPos getRandomPositionWithinRange(BlockPos centerPos, int xRadius, int yRadius, int zRadius, int minSpreadX, int minSpreadY, int minSpreadZ, boolean safeSurfacePlacement, Level world, int tries, @Nullable BiPredicate<BlockState, BlockPos> statePredicate) {
		return RANDOM.getRandomPositionWithinRange(centerPos, xRadius, yRadius, zRadius, minSpreadX, minSpreadY, minSpreadZ, safeSurfacePlacement, world, tries, statePredicate);
	}

	public static final class EasyRandom implements RandomSource  {
		private final RandomSource random;

		public EasyRandom() {
			this(RandomSource.create());
		}

		public EasyRandom(@NotNull RandomSource rand) {
			this.random = rand;
		}

		public RandomSource getSource() {
			return RandomSource.create();
		}

		public boolean fiftyFifty() {
			return random.nextBoolean();
		}

		public boolean oneInNChance(int n) {
			if (n <= 0)
				return false;

			return random.nextFloat() < 1 / (float)n;
		}

		public boolean percentChance(double percentChance) {
			if (percentChance <= 0)
				return false;

			if (percentChance >= 1)
				return true;

			return random.nextDouble() < percentChance;
		}

		public boolean percentChance(float percentChance) {
			if (percentChance <= 0)
				return false;

			if (percentChance >= 1)
				return true;

			return random.nextDouble() < percentChance;
		}

		public int randomNumberUpTo(int upperBound) {
			return random.nextInt(upperBound);
		}

		public float randomValueUpTo(float upperBound) {
			return random.nextFloat() * upperBound;
		}

		public double randomValueUpTo(double upperBound) {
			return random.nextDouble() * upperBound;
		}

		public double randomGaussianValue() {
			return random.nextGaussian();
		}

		public double randomScaledGaussianValue(double scale) {
			return random.nextGaussian() * scale;
		}

		public int randomNumberBetween(int min, int max) {
			return min + (int)Math.floor(random.nextDouble() * (1 + max - min));
		}

		public double randomValueBetween(double min, double max) {
			return min + random.nextDouble() * (max - min);
		}

		public <T> T getRandomSelection(@NotNull T... options) {
			return options[random.nextInt(options.length)];
		}

		public <T> T getRandomSelection(@NotNull List<T> options) {
			return options.get(random.nextInt(options.size()));
		}

		@NotNull
		public BlockPos getRandomPositionWithinRange(BlockPos centerPos, int xRadius, int yRadius, int zRadius) {
			return getRandomPositionWithinRange(centerPos, xRadius, yRadius, zRadius, false, null);
		}

		@NotNull
		public BlockPos getRandomPositionWithinRange(BlockPos centerPos, int xRadius, int yRadius, int zRadius, boolean safeSurfacePlacement, Level world) {
			return getRandomPositionWithinRange(centerPos, xRadius, yRadius, zRadius, 0, 0, 0, safeSurfacePlacement, world, 1, null);
		}

		@NotNull
		public BlockPos getRandomPositionWithinRange(BlockPos centerPos, int xRadius, int yRadius, int zRadius, int minSpreadX, int minSpreadY, int minSpreadZ, boolean safeSurfacePlacement, Level world, int tries, @Nullable BiPredicate<BlockState, BlockPos> statePredicate) {	BlockPos.MutableBlockPos mutablePos = centerPos.mutable();
			xRadius = Math.max(xRadius - minSpreadX, 0);
			yRadius = Math.max(yRadius - minSpreadY, 0);
			zRadius = Math.max(zRadius - minSpreadZ, 0);

			for (int i = 0; i < tries; i++) {
				double xAdjust = random.nextFloat() * xRadius * 2 - xRadius;
				double yAdjust = random.nextFloat() * yRadius * 2 - yRadius;
				double zAdjust = random.nextFloat() * zRadius * 2 - zRadius;
				int newX = (int)Math.floor(centerPos.getX() + xAdjust + minSpreadX * Math.signum(xAdjust));
				int newY = (int)Math.floor(centerPos.getY() + yAdjust + minSpreadY * Math.signum(yAdjust));
				int newZ = (int)Math.floor(centerPos.getZ() + zAdjust + minSpreadZ * Math.signum(zAdjust));

				mutablePos.set(newX, newY, newZ);

				if (safeSurfacePlacement && world != null)
					mutablePos.set(world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, mutablePos));

				if (statePredicate == null || statePredicate.test(world.getBlockState(mutablePos), mutablePos.immutable()))
					return mutablePos.immutable();
			}

			return centerPos;
		}

		@Override
		public EasyRandom fork() {
			return new EasyRandom(this.random.fork());
		}

		@Override
		public PositionalRandomFactory forkPositional() {
			return this.random.forkPositional();
		}

		@Override
		public void setSeed(long seed) {
			this.random.setSeed(seed);
		}

		@Override
		public int nextInt() {
			return this.random.nextInt();
		}

		@Override
		public int nextInt(int upperLimit) {
			return this.random.nextInt(upperLimit);
		}

		@Override
		public long nextLong() {
			return this.random.nextLong();
		}

		@Override
		public boolean nextBoolean() {
			return this.random.nextBoolean();
		}

		@Override
		public float nextFloat() {
			return this.random.nextFloat();
		}

		@Override
		public double nextDouble() {
			return this.random.nextDouble();
		}

		@Override
		public double nextGaussian() {
			return this.random.nextGaussian();
		}
	}
}