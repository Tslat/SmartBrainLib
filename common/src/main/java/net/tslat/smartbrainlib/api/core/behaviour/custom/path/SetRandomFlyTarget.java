package net.tslat.smartbrainlib.api.core.behaviour.custom.path;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.HoverRandomPos;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;
import net.tslat.smartbrainlib.library.interfaces.ToFloatBiFunction;
import net.tslat.smartbrainlib.library.object.SquareRadius;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.function.*;

/// Set a random position to fly to, taking into account the entity's current heading
///
/// Keeps the entity roughly near ground level, encouraging hover-flight rather than floating off into the sky
///
/// @see SetRandomMoveTarget
/// @param <BO> The brain owner entity
public class SetRandomFlyTarget<BO extends PathfinderMob> extends SetRandomMoveTarget<BO> {
	protected ToIntFunction<BO> verticalWeight = _ -> -2;
	protected ToIntFunction<BO> hoverHeightMin = _ -> 1;
	protected ToIntFunction<BO> hoverHeightMax = _ -> 3;
	
	/// Sets the function that determines a vertical position offset for target positions
	///
	/// Flight patterns will tend towards this direction, with bigger values pulling more strongly
	@ApiStatus.NonExtendable
	public SetRandomFlyTarget<BO> verticalWeight(ToIntFunction<BO> function) {
		this.verticalWeight = function;
		
		return this;
	}
	
	/// Set the distance (in blocks) that the entity should attempt to fly above ground height, on average
	@ApiStatus.NonExtendable
	public SetRandomFlyTarget<BO> hoverBetween(int min, int max) {
		return hoverBetween(_ -> min, _ -> max);
	}
	
	/// Set functions to determine the distance (in blocks) that the entity should attempt to fly above ground height, on average
	@ApiStatus.NonExtendable
	public SetRandomFlyTarget<BO> hoverBetween(ToIntFunction<BO> min, ToIntFunction<BO> max) {
		this.hoverHeightMin = min;
		this.hoverHeightMax = max;
		
		return this;
	}
	
	//<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
	/// Set the radius (in blocks) to look for flight positions
	@ApiStatus.NonExtendable
	@Override
	public SetRandomFlyTarget<BO> setRadius(double radius) {
		return (SetRandomFlyTarget<BO>)super.setRadius(radius);
	}
	
	/// Set the radius (in blocks) to look for flight positions
	@ApiStatus.NonExtendable
	@Override
	public SetRandomFlyTarget<BO> setRadius(double xz, double y) {
		return (SetRandomFlyTarget<BO>)super.setRadius(xz, y);
	}
	
	/// Set the function to determine the radius (in blocks) to look for flight positions
	@ApiStatus.NonExtendable
	@Override
	public SetRandomFlyTarget<BO> setRadius(Function<BO, SquareRadius> function) {
		return (SetRandomFlyTarget<BO>)super.setRadius(function);
	}
	
	/// Set the movement speed modifier for the path when chosen
	@ApiStatus.NonExtendable
	@Override
	public SetRandomFlyTarget<BO> speedModifier(float modifier) {
		return (SetRandomFlyTarget<BO>)super.speedModifier(modifier);
	}
	
	/// Set the function to determine the movement speed modifier for the path when chosen
	@ApiStatus.NonExtendable
	@Override
	public SetRandomFlyTarget<BO> speedModifier(ToFloatBiFunction<BO, Vec3> function) {
		return (SetRandomFlyTarget<BO>)super.speedModifier(function);
	}
	
	/// Sets a predicate to check whether a target movement position is valid or not
	@ApiStatus.NonExtendable
	@Override
	public SetRandomFlyTarget<BO> isValidPositionIf(BiPredicate<BO, Vec3> predicate) {
		return (SetRandomFlyTarget<BO>)super.isValidPositionIf(predicate);
	}
	
	/// Set the distance (in blocks) that the entity should be considered 'close enough' to the target position to have arrived
	@ApiStatus.NonExtendable
	@Override
	public SetRandomFlyTarget<BO> closeEnoughDist(int distance) {
		return (SetRandomFlyTarget<BO>)super.closeEnoughDist(distance);
	}
	
	/// Set the function to determine the distance (in blocks) that the entity should be considered 'close enough' to the target position to have arrived
	@ApiStatus.NonExtendable
	@Override
	public SetRandomFlyTarget<BO> closeEnoughDist(ToIntBiFunction<BO, Vec3> function) {
		return (SetRandomFlyTarget<BO>)super.closeEnoughDist(function);
	}
	
	/// Set a callback for when the behaviour successfully begins
	///
	/// This is called immediately prior to [ExtendedBehaviour#start(LivingEntity)]
	@ApiStatus.NonExtendable
	@Override
	public SetRandomFlyTarget<BO> whenStarting(Consumer<BO> callback) {
		return (SetRandomFlyTarget<BO>)super.whenStarting(callback);
	}
	
	/// Set a callback for when the behaviour stops
	///
	/// This is called immediately prior to [#stop(LivingEntity)]
	///
	/// Note that the behaviour stopping does not necessarily mean it was successful
	@ApiStatus.NonExtendable
	@Override
	public SetRandomFlyTarget<BO> whenStopping(Consumer<BO> callback) {
		return (SetRandomFlyTarget<BO>)super.whenStopping(callback);
	}
	
	/// Set the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public SetRandomFlyTarget<BO> runFor(int ticks) {
		return (SetRandomFlyTarget<BO>)super.runFor(ticks);
	}
	
	/// Set the range of ticks the behaviour should try to run for, once started<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is run
	/// 
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public SetRandomFlyTarget<BO> runFor(int minTicks, int maxTicks) {
		return (SetRandomFlyTarget<BO>)super.runFor(minTicks, maxTicks);
	}
	
	/// Set a function to determine the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
	@ApiStatus.NonExtendable
	@Override
	public SetRandomFlyTarget<BO> runFor(ToIntFunction<BO> timeProvider) {
		return (SetRandomFlyTarget<BO>)super.runFor(timeProvider);
	}
	
	/// Disable the tick-based timeout for this behaviour and instead rely exclusively on other conditions
	/// such as [#getMemoryRequirements()] failing or [#stopIf(Predicate)]
	@ApiStatus.NonExtendable
	@Override
	public SetRandomFlyTarget<BO> noTimeout() {
		return (SetRandomFlyTarget<BO>)super.noTimeout();
	}
	
	/// Set the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public SetRandomFlyTarget<BO> cooldownFor(int ticks) {
		return (SetRandomFlyTarget<BO>)super.cooldownFor(ticks);
	}
	
	/// Set the range of ticks that this behaviour should be prevented from starting again after it has finished<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is stopped
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public SetRandomFlyTarget<BO> cooldownFor(int minTicks, int maxTicks) {
		return (SetRandomFlyTarget<BO>)super.cooldownFor(minTicks, maxTicks);
	}
	
	/// Set a function to determine the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
	@ApiStatus.NonExtendable
	@Override
	public SetRandomFlyTarget<BO> cooldownFor(ToIntFunction<BO> timeProvider) {
		return (SetRandomFlyTarget<BO>)super.cooldownFor(timeProvider);
	}
	
	/// Set an additional condition for the behaviour to be able to start
	///
	/// Prevents this behaviour starting unless this predicate returns true.
	///
	/// @param predicate The condition for starting
	@ApiStatus.NonExtendable
	@Override
	public SetRandomFlyTarget<BO> startCondition(Predicate<BO> predicate) {
		return (SetRandomFlyTarget<BO>)super.startCondition(predicate);
	}
	
	/// Set a condition under which the behaviour should automatically stop<br/>
	/// Has no effect on one-shot behaviours that don't tick or have a runtime
	/// 
	/// Stops the behaviour immediately if the predicate returns true, ready to run again
	@ApiStatus.NonExtendable
	@Override
	public SetRandomFlyTarget<BO> stopIf(Predicate<BO> predicate) {
		return (SetRandomFlyTarget<BO>)super.stopIf(predicate);
	}
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="<Internal Handling>">
	/// Get a new nearby position to attempt to move to
	///
	/// This position does not need to be predicated with the [#validPositionPredicate]
	@Override
	protected @Nullable Vec3 getTargetPos(BO entity) {
		final Vec3 entityFacing = entity.getViewVector(0);
		final SquareRadius radius = this.radius.apply(entity);
		final Vec3 hoverPos = HoverRandomPos.getPos(entity, Mth.ceil(radius.xzRadius()), Mth.ceil(radius.yRadius()), entityFacing.x, entityFacing.z, Mth.HALF_PI, this.hoverHeightMax.applyAsInt(entity), this.hoverHeightMin.applyAsInt(entity));

		if (hoverPos != null)
			return hoverPos;

		return AirAndWaterRandomPos.getPos(entity, Mth.ceil(radius.xzRadius()), Mth.ceil(radius.yRadius()), this.verticalWeight.applyAsInt(entity), entityFacing.x, entityFacing.z, Mth.HALF_PI);
	}
	//</editor-fold>
}
