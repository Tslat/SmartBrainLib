package net.tslat.smartbrainlib.api.core.sensor.vanilla;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.phys.AABB;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.library.interfaces.TriPredicate;
import net.tslat.smartbrainlib.library.object.SquareRadius;
import net.tslat.smartbrainlib.registry.SBLSensors;
import net.tslat.smartbrainlib.util.BrainUtil;
import net.tslat.smartbrainlib.util.EntityRetrievalUtil;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/// Find the nearest player that is holding out a tempting item for the entity
///
/// @see net.minecraft.world.entity.ai.sensing.TemptingSensor
/// @param <BO> The brain owner entity
public class ItemTemptingSensor<BO extends LivingEntity> extends ExtendedSensor<BO> {
	protected static final List<MemoryModuleType<?>> MEMORIES = ObjectArrayList.of(MemoryModuleType.TEMPTING_PLAYER, MemoryModuleType.NEAREST_PLAYERS);

	protected TriPredicate<BO, ItemStack, Player> temptPredicate = (_, _, _) -> false;
	protected Function<BO, SquareRadius> radius = entity -> new SquareRadius(entity.getAttributeValue(Attributes.TEMPT_RANGE));

	/// Set the radius for the player sensor to scan
	///
	/// @param radius The radius, in blocks
	public ItemTemptingSensor<BO> temptRadius(double radius) {
		return temptRadius(radius, radius);
	}

	/// Set the radius for the player sensor to scan
	///
	/// @param xz The X/Z axis radius, in blocks
	/// @param y  The Y axis radius, in blocks
	public ItemTemptingSensor<BO> temptRadius(double xz, double y) {
		return temptRadius(bo -> new SquareRadius(xz, y));
	}

	/// Set the radius for the player sensor to scan
	///
	/// @param radiusFunction The function to determine the radius for the current scan tick
	public ItemTemptingSensor<BO> temptRadius(Function<BO, SquareRadius> radiusFunction) {
		this.radius = radiusFunction;

		return this;
	}

	/// Set the items to temptable items for the entity<br/>
	/// Automatically handles boilerplate player checks as part of the predicate
	///
	/// @param item The item the entity should be tempted by
	public ItemTemptingSensor<BO> temptedWith(ItemLike item) {
		return temptedWith((_, stack, _) -> stack.is(item.asItem()));
	}

	/// Set the items to temptable items for the entity<br/>
	/// Automatically handles boilerplate player checks as part of the predicate
	///
	/// @param stack The ItemStack the entity should be tempted by
	public ItemTemptingSensor<BO> temptedWith(ItemStack stack) {
		return temptedWith((_, heldStack, _) -> ItemStack.isSameItemSameComponents(heldStack, stack));
	}

	/// Set the items to temptable items for the entity<br/>
	/// Automatically handles boilerplate player checks as part of the predicate
	///
	/// @param tag The Item tag the entity should be tempted by
	public ItemTemptingSensor<BO> temptedWith(TagKey<Item> tag) {
		return temptedWith((_, stack, _) -> stack.is(tag));
	}

	/// Set the items to temptable items for the entity<br/>
	/// Automatically handles boilerplate player checks as part of the predicate
	///
	/// @param predicate The predicate to test for valid items for tempting, testing the entity, the player, and the item in the player's hand
	public ItemTemptingSensor<BO> temptedWith(final TriPredicate<BO, ItemStack, Player> predicate) {
		return temptPredicate((entity, stack, player) -> {
			if (player.isSpectator() || !player.isAlive())
				return false;

			return predicate.test(entity, stack, player);
		});
	}

	/// Set the predicate to determine whether the entity should be tempted
	///
	/// @param predicate The predicate to test for successful temptation, testing the entity, the player, and the item in the player's hand
	public ItemTemptingSensor<BO> temptPredicate(final TriPredicate<BO, ItemStack, Player> predicate) {
		this.temptPredicate = predicate;

		return this;
	}

	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// Set the scan rate for this sensor
	public ItemTemptingSensor<BO> scanRate(int scanRate) {
		return scanRate(_ -> scanRate);
	}

	/// Set the scan rate provider for this sensor
	///
	/// The provider will be sampled every time the sensor does a scan
	@Override
	public ItemTemptingSensor<BO> scanRate(ToIntFunction<BO> function) {
		return (ItemTemptingSensor<BO>)super.scanRate(function);
	}

	/// Set a callback function for when the sensor completes a scan
	@Override
	public ItemTemptingSensor<BO> afterScanning(Consumer<BO> callback) {
		return (ItemTemptingSensor<BO>)super.afterScanning(callback);
	}

	/// Set a condition that must be met in order to perform a scan
	///
	/// Failing the predicate will skip that scan tick and will not try again until the next scan tick as defined by [#scanRate]
	@Override
	public ItemTemptingSensor<BO> onlyScanIf(Predicate<BO> predicate) {
		return (ItemTemptingSensor<BO>)super.onlyScanIf(predicate);
	}
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="<Internal Handling>">
	/// @return The [SensorType] of the sensor, used for reverse lookups.
	@Override
	public SensorType<? extends ExtendedSensor<?>> type() {
		return SBLSensors.ITEM_TEMPTING.get();
	}

	/// The list of memory types this sensor saves to. This should contain any memory the sensor sets a value for in the brain<br/>
	/// Bonus points if it's a statically cached list
	///
	/// @return The list of memory types saves by this sensor
	@Override
	public List<MemoryModuleType<?>> memoriesUsed() {
		return MEMORIES;
	}

	/// Handle the Sensor's actual function here. Be wary of the performance implications of computation-heavy checks here
	///
	/// @param level The level the entity is in
	/// @param entity The owner of the brain
	@Override
	protected void doTick(ServerLevel level, BO entity) {
		final List<Player> nearbyPlayers = BrainUtil.getMemory(entity, MemoryModuleType.NEAREST_PLAYERS);
		final AABB bounds = this.radius.apply(entity).inflateAABB(entity.getBoundingBox());
		final Predicate<Player> predicate = pl -> bounds.contains(pl.position()) && (this.temptPredicate.test(entity, pl.getMainHandItem(), pl) || this.temptPredicate.test(entity, pl.getOffhandItem(), pl));
		Optional<Player> player;

		if (nearbyPlayers != null) {
			Player nearestPlayer = null;
			double nearestDistance = Double.MAX_VALUE;

			for (Player pl : nearbyPlayers) {
				if (predicate.test(pl)) {
					double dist = pl.distanceToSqr(entity);

					if (dist < nearestDistance) {
						nearestDistance = dist;
						nearestPlayer = pl;
					}
				}
			}

			player = Optional.ofNullable(nearestPlayer);
		}
		else {
			player = EntityRetrievalUtil.getNearestPlayer(entity.level(), bounds, entity.position(), predicate);
		}

		BrainUtil.setOrClearMemory(entity, MemoryModuleType.TEMPTING_PLAYER, player.orElse(null));
	}
	//</editor-fold>
}
