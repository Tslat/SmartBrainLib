package net.tslat.smartbrainlib.api.core.behaviour.base;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.library.object.MemoryTest;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/// Wrapper behaviour for a behaviour that should repeat from the start once finished, unless otherwise stated
///
/// While running and repeating, this behaviour is considered as still running<br/>
/// It will stop when it either runs out of repeats or the stop condition is met
public class RepeatingBehaviour<BO extends LivingEntity> extends ExtendedBehaviour<BO> {
    protected final ExtendedBehaviour<BO> child;

    protected Predicate<BO> shouldRepeat = _ -> true;
    protected ToIntFunction<BO> repeatCountProvider = _ -> Integer.MAX_VALUE;

    protected int repeats;

    public RepeatingBehaviour(ExtendedBehaviour<BO> child) {
        this.child = child;

        super();

        noTimeout();
    }

    /// Set the predicate that determines whether the wrapped behaviour should repeat or not at any given time
    @ApiStatus.NonExtendable
    public RepeatingBehaviour<BO> repeatingWhen(Predicate<BO> predicate) {
        this.shouldRepeat = predicate;

        return this;
    }

    /// Limit the number of repeats this behaviour should have when running
    ///
    /// `1` repeat results in the wrapped behaviour running twice
    @ApiStatus.NonExtendable
    public RepeatingBehaviour<BO> repeatNTimes(int repeats) {
        return repeatNTimes(_ -> repeats);
    }

    /// Limit the number of repeats this behaviour should have when running
    ///
    /// `1` repeat results in the wrapped behaviour running twice
    @ApiStatus.NonExtendable
    public RepeatingBehaviour<BO> repeatNTimes(ToIntFunction<BO> function) {
        this.repeatCountProvider = function;

        return this;
    }

    //<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
    /// Set a callback for when the behaviour successfully begins
    ///
    /// This is called immediately prior to [#start(LivingEntity)]
    @ApiStatus.NonExtendable
    @Override
    public RepeatingBehaviour<BO> whenStarting(Consumer<BO> callback) {
        return (RepeatingBehaviour<BO>)super.whenStarting(callback);
    }
    
    /// Set a callback for when the behaviour stops
    ///
    /// This is called immediately prior to [#stop(LivingEntity)]
    ///
    /// Note that the behaviour stopping does not necessarily mean it was successful
    @ApiStatus.NonExtendable
    @Override
    public RepeatingBehaviour<BO> whenStopping(Consumer<BO> callback) {
        return (RepeatingBehaviour<BO>)super.whenStopping(callback);
    }
    
    /// Set the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
    @ApiStatus.NonExtendable
    @Override
    public RepeatingBehaviour<BO> runFor(int ticks) {
        return (RepeatingBehaviour<BO>)super.runFor(ticks);
    }

    /// Set the range of ticks the behaviour should try to run for, once started<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is run
	/// 
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
    @ApiStatus.NonExtendable
    @Override
    public RepeatingBehaviour<BO> runFor(int minTicks, int maxTicks) {
        return (RepeatingBehaviour<BO>)super.runFor(minTicks, maxTicks);
    }

    /// Set a function to determine the number of ticks the behaviour should try to run for, once started
	///
	/// The behaviour may still be stopped before this time runs out through other conditions or manual stops
    @ApiStatus.NonExtendable
    @Override
    public RepeatingBehaviour<BO> runFor(ToIntFunction<BO> timeProvider) {
        return (RepeatingBehaviour<BO>)super.runFor(timeProvider);
    }

    /// Disable the tick-based timeout for this behaviour and instead rely exclusively on other conditions
	/// such as [#getMemoryRequirements()] failing or [#stopIf(Predicate)]
    @ApiStatus.NonExtendable
    @Override
    public RepeatingBehaviour<BO> noTimeout() {
        return (RepeatingBehaviour<BO>)super.noTimeout();
    }

    /// Set the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
    @ApiStatus.NonExtendable
    @Override
    public RepeatingBehaviour<BO> cooldownFor(int ticks) {
        return (RepeatingBehaviour<BO>)super.cooldownFor(ticks);
    }

    /// Set the range of ticks that this behaviour should be prevented from starting again after it has finished<br/>
	/// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is stopped
	///
	/// This is the length of time between when this behaviour stops and it can start again
    @ApiStatus.NonExtendable
    @Override
    public RepeatingBehaviour<BO> cooldownFor(int minTicks, int maxTicks) {
        return (RepeatingBehaviour<BO>)super.cooldownFor(minTicks, maxTicks);
    }

    /// Set a function to determine the number of ticks that this behaviour should be prevented from starting again after it has finished
	///
	/// This is the length of time between when this behaviour stops and it can start again
    @ApiStatus.NonExtendable
    @Override
    public RepeatingBehaviour<BO> cooldownFor(ToIntFunction<BO> timeProvider) {
        return (RepeatingBehaviour<BO>)super.cooldownFor(timeProvider);
    }

    /// Set an additional condition for the behaviour to be able to start
    ///
    /// Prevents this behaviour starting unless this predicate returns true
    ///
    /// @param predicate The condition for starting
    @ApiStatus.NonExtendable
    @Override
    public RepeatingBehaviour<BO> startCondition(Predicate<BO> predicate) {
        return (RepeatingBehaviour<BO>)super.startCondition(predicate);
    }

    /// Set a condition under which the behaviour should automatically stop<br/>
	/// Has no effect on one-shot behaviours that don't tick or have a runtime
	/// 
	/// Stops the behaviour immediately if the predicate returns true, ready to run again
    @ApiStatus.NonExtendable
    @Override
    public RepeatingBehaviour<BO> stopIf(Predicate<BO> predicate) {
        return (RepeatingBehaviour<BO>)super.stopIf(predicate);
    }
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="<Internal Handling>">
    /// The list of memory requirements this task has before starting<br/>
    /// This outlines the approximate state the brain should be in to allow this behaviour to run
    ///
    /// Ideally, this would be a statically cached list
    ///
    /// @return The [List] of [Memories][MemoryModuleType] and their associated required [status][MemoryStatus]
    /// @see MemoryTest
    @Override
    public Set<MemoryCondition<?, ?>> getMemoryRequirements() {
        return this.child.getMemoryRequirements();
    }
    
    /// Check all behaviour start conditions to determine whether the behaviour can start or not
    ///
    /// <u>NOTE:</u> This is an API method. You should not be calling or overriding this method unless you know what you are doing
    ///
    /// @see #startCondition(Predicate)
    /// @see #getMemoryRequirements()
    /// @see #runFor
    /// @see #checkExtraStartConditions(ServerLevel, LivingEntity)
    @ApiStatus.Internal
    @Override
    protected boolean canStart(ServerLevel level, BO entity, long gameTime) {
        return super.canStart(level, entity, gameTime) && this.child.tryStart(level, entity, gameTime);
    }
    
    /// Start the behaviour<br/>
    /// All pre-start checks have been checked and passed by this point
    ///
    /// <u>NOTE:</u> This is an API method. You should not be calling or overriding this method unless you know what you are doing
    ///
    /// @see #start(LivingEntity)
    /// @see #whenStarting(Consumer)
    @ApiStatus.Internal
    @Override
    protected void start(ServerLevel level, BO entity, long gameTime) {
        super.start(level, entity, gameTime);

        this.repeats = this.repeatCountProvider.applyAsInt(entity);
    }
    
    /// Determine whether the conditions for this behaviour are still valid for the current tick
    ///
    /// <u>NOTE:</u> This is an API method. You should not be calling or overriding this method unless you know what you are doing
    ///
    /// This method should not modify the behaviour in any way and should only act as a read-only view of applicability
    ///
    /// @see #shouldKeepRunning(LivingEntity)
    /// @see #stopIf(Predicate)
    @ApiStatus.Internal
    @Override
    protected boolean canStillUse(ServerLevel level, BO entity, long gameTime) {
        return (this.child.getStatus() != Status.STOPPED || this.repeats > 0) && !this.stopCondition.test(entity);
    }
    
    /// Perform any internal per-tick functionality for this behaviour
    ///
    /// <u>NOTE:</u> This is an API method. You should not be calling or overriding this method unless you know what you are doing
    @ApiStatus.Internal
    @Override
    protected void tick(ServerLevel level, BO entity, long gameTime) {
        super.tick(level, entity, gameTime);

        if (this.child.getStatus() != Status.STOPPED) {
            this.child.tickOrStop(level, entity, gameTime);

            return;
        }

        if (this.repeats > 0 && this.shouldRepeat.test(entity) && this.child.tryStart(level, entity, gameTime))
            this.repeats--;
    }
    
    /// Perform any internal cleanup functionality on task stop for this behaviour
    ///
    /// <u>NOTE:</u> This is an API method. You should not be calling or overriding this method unless you know what you are doing
    @ApiStatus.Internal
    @Override
    protected void stop(ServerLevel level, BO entity, long gameTime) {
        super.stop(level, entity, gameTime);

        if (this.child.getStatus() != Status.STOPPED)
            this.child.doStop(level, entity, gameTime);
    }

    @Override
    public String toString() {
        return "(" + getClass().getSimpleName() + "): " + this.child;
    }

    @Override
    public String debugString() {
        return getClass().getSimpleName() + " -> " + this.child.debugString();
    }
    //</editor-fold>
}