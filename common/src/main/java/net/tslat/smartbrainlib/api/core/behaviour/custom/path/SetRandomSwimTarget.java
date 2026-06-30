package net.tslat.smartbrainlib.api.core.behaviour.custom.path;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;
import net.tslat.smartbrainlib.library.interfaces.ToFloatBiFunction;
import net.tslat.smartbrainlib.library.object.SquareRadius;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.function.*;

/// Set a random position to swim to
///
/// @param <BO> The brain owner entity
public class SetRandomSwimTarget<BO extends PathfinderMob> extends SetRandomMoveTarget<BO> {
    //<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
    /// Set the radius (in blocks) to look for flight positions
    @ApiStatus.NonExtendable
    @Override
    public SetRandomSwimTarget<BO> setRadius(double radius) {
        return (SetRandomSwimTarget<BO>)super.setRadius(radius);
    }
    
    /// Set the radius (in blocks) to look for flight positions
    @ApiStatus.NonExtendable
    @Override
    public SetRandomSwimTarget<BO> setRadius(double xz, double y) {
        return (SetRandomSwimTarget<BO>)super.setRadius(xz, y);
    }
    
    /// Set the function to determine the radius (in blocks) to look for flight positions
    @ApiStatus.NonExtendable
    @Override
    public SetRandomSwimTarget<BO> setRadius(Function<BO, SquareRadius> function) {
        return (SetRandomSwimTarget<BO>)super.setRadius(function);
    }
    
    /// Set the movement speed modifier for the path when chosen
    @ApiStatus.NonExtendable
    @Override
    public SetRandomSwimTarget<BO> speedModifier(float modifier) {
        return (SetRandomSwimTarget<BO>)super.speedModifier(modifier);
    }
    
    /// Set the function to determine the movement speed modifier for the path when chosen
    @ApiStatus.NonExtendable
    @Override
    public SetRandomSwimTarget<BO> speedModifier(ToFloatBiFunction<BO, Vec3> function) {
        return (SetRandomSwimTarget<BO>)super.speedModifier(function);
    }
    
    /// Sets a predicate to check whether a target movement position is valid or not
    @ApiStatus.NonExtendable
    @Override
    public SetRandomSwimTarget<BO> isValidPositionIf(BiPredicate<BO, Vec3> predicate) {
        return (SetRandomSwimTarget<BO>)super.isValidPositionIf(predicate);
    }
    
    /// Set the distance (in blocks) that the entity should be considered 'close enough' to the target position to have arrived
    @ApiStatus.NonExtendable
    @Override
    public SetRandomSwimTarget<BO> closeEnoughDist(int distance) {
        return (SetRandomSwimTarget<BO>)super.closeEnoughDist(distance);
    }
    
    /// Set the function to determine the distance (in blocks) that the entity should be considered 'close enough' to the target position to have arrived
    @ApiStatus.NonExtendable
    @Override
    public SetRandomSwimTarget<BO> closeEnoughDist(ToIntBiFunction<BO, Vec3> function) {
        return (SetRandomSwimTarget<BO>)super.closeEnoughDist(function);
    }
    
    /// Set a callback for when the behaviour successfully begins
    ///
    /// This is called immediately prior to [ExtendedBehaviour#start(LivingEntity)]
    @ApiStatus.NonExtendable
    @Override
    public SetRandomSwimTarget<BO> whenStarting(Consumer<BO> callback) {
        return (SetRandomSwimTarget<BO>)super.whenStarting(callback);
    }
    
    /// Set a callback for when the behaviour stops
    ///
    /// This is called immediately prior to [ExtendedBehaviour#stop(LivingEntity)]
    ///
    /// Note that the behaviour stopping does not necessarily mean it was successful
    @ApiStatus.NonExtendable
    @Override
    public SetRandomSwimTarget<BO> whenStopping(Consumer<BO> callback) {
        return (SetRandomSwimTarget<BO>)super.whenStopping(callback);
    }
    
    /// Set the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
    @ApiStatus.NonExtendable
    @Override
    public SetRandomSwimTarget<BO> runFor(int ticks) {
        return (SetRandomSwimTarget<BO>)super.runFor(ticks);
    }
    
    /// Set the range of ticks the behaviour should try to run for, once started<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is run
	/// 
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
    @ApiStatus.NonExtendable
    @Override
    public SetRandomSwimTarget<BO> runFor(int minTicks, int maxTicks) {
        return (SetRandomSwimTarget<BO>)super.runFor(minTicks, maxTicks);
    }
    
    /// Set a function to determine the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
    @ApiStatus.NonExtendable
    @Override
    public SetRandomSwimTarget<BO> runFor(ToIntFunction<BO> timeProvider) {
        return (SetRandomSwimTarget<BO>)super.runFor(timeProvider);
    }
    
    /// Disable the tick-based timeout for this behaviour and instead rely exclusively on other conditions
	/// such as [#getMemoryRequirements()] failing or [#stopIf(Predicate)]
    @ApiStatus.NonExtendable
    @Override
    public SetRandomSwimTarget<BO> noTimeout() {
        return (SetRandomSwimTarget<BO>)super.noTimeout();
    }
    
    /// Set the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
    @ApiStatus.NonExtendable
    @Override
    public SetRandomSwimTarget<BO> cooldownFor(int ticks) {
        return (SetRandomSwimTarget<BO>)super.cooldownFor(ticks);
    }
    
    /// Set the range of ticks that this behaviour should be prevented from starting again after it has finished<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is stopped
	///
	/// This is the length of time between when this behaviour stops and it can start again
    @ApiStatus.NonExtendable
    @Override
    public SetRandomSwimTarget<BO> cooldownFor(int minTicks, int maxTicks) {
        return (SetRandomSwimTarget<BO>)super.cooldownFor(minTicks, maxTicks);
    }
    
    /// Set a function to determine the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
    @ApiStatus.NonExtendable
    @Override
    public SetRandomSwimTarget<BO> cooldownFor(ToIntFunction<BO> timeProvider) {
        return (SetRandomSwimTarget<BO>)super.cooldownFor(timeProvider);
    }
    
    /// Set an additional condition for the behaviour to be able to start
    ///
    /// Prevents this behaviour starting unless this predicate returns true.
    ///
    /// @param predicate The condition for starting
    @ApiStatus.NonExtendable
    @Override
    public SetRandomSwimTarget<BO> startCondition(Predicate<BO> predicate) {
        return (SetRandomSwimTarget<BO>)super.startCondition(predicate);
    }
    
    /// Set a condition under which the behaviour should automatically stop<br/>
	/// Has no effect on one-shot behaviours that don't tick or have a runtime
	/// 
	/// Stops the behaviour immediately if the predicate returns true, ready to run again
    @ApiStatus.NonExtendable
    @Override
    public SetRandomSwimTarget<BO> stopIf(Predicate<BO> predicate) {
        return (SetRandomSwimTarget<BO>)super.stopIf(predicate);
    }
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="<Internal Handling>">
    /// Get a new nearby position to attempt to move to
    ///
    /// This position does not need to be predicated with the [#validPositionPredicate]
    @Override
    protected @Nullable Vec3 getTargetPos(BO entity) {
        final SquareRadius radius = this.radius.apply(entity);
        
        return BehaviorUtils.getRandomSwimmablePos(entity, Mth.ceil(radius.xzRadius()), Mth.ceil(radius.yRadius()));
    }
    //</editor-fold>
}