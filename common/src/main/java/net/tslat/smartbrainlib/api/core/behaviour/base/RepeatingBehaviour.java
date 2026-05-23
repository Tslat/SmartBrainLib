package net.tslat.smartbrainlib.api.core.behaviour.base;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.library.object.MemoryTest;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
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
    ///
    /// @param predicate The predicate
    /// @return this
    public RepeatingBehaviour<BO> repeatingWhen(Predicate<BO> predicate) {
        this.shouldRepeat = predicate;

        return this;
    }

    /// Limit the number of repeats this behaviour should have when running.
    ///
    /// `1` repeat results in the wrapped behaviour running twice
    ///
    /// @param repeats The number of times to repeat the behaviour
    public RepeatingBehaviour<BO> repeatNTimes(int repeats) {
        return repeatNTimes(_ -> repeats);
    }

    /// Limit the number of repeats this behaviour should have when running.
    ///
    /// `1` repeat results in the wrapped behaviour running twice
    ///
    /// @param function The number of times to repeat the behaviour
    public RepeatingBehaviour<BO> repeatNTimes(ToIntFunction<BO> function) {
        this.repeatCountProvider = function;

        return this;
    }

    //<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
    /// A callback for when the task begins. Use this to trigger effects or handle things when the entity activates this task
    ///
    /// @param callback The function to call when starting the behaviour, immediately prior to [#start(LivingEntity)]
    @ApiStatus.NonExtendable
    public RepeatingBehaviour<BO> whenStarting(Consumer<BO> callback) {
        return (RepeatingBehaviour<BO>)super.whenStarting(callback);
    }

    /// A callback for when the task stops. Use this to trigger effects or handle things when the entity ends this task
    ///
    /// Note that the task stopping does not necessarily mean it was successful
    ///
    /// @param callback The function to call when stopping the behaviour, immediately prior to [#stop(LivingEntity)]
    @ApiStatus.NonExtendable
    public RepeatingBehaviour<BO> whenStopping(Consumer<BO> callback) {
        return (RepeatingBehaviour<BO>)super.whenStopping(callback);
    }

    /// Set the length (in ticks) that the task should run for once activated, randomly selected between two values
    /// The value used is in _ticks_
    ///
    /// @param ticks The number of ticks to run for
    @ApiStatus.NonExtendable
    public RepeatingBehaviour<BO> runFor(int ticks) {
        return (RepeatingBehaviour<BO>)super.runFor(ticks);
    }

    /// Set the length (in ticks) that the task should run for once activated, randomly selected between two values
    /// The value used is in _ticks_
    ///
    /// @param minTicks The minimum number of ticks to run for
    /// @param maxTicks The maximum number of ticks to run for
    @ApiStatus.NonExtendable
    public RepeatingBehaviour<BO> runFor(int minTicks, int maxTicks) {
        return (RepeatingBehaviour<BO>)super.runFor(minTicks, maxTicks);
    }

    /// Set the length (in ticks) that the task should run for once activated
    ///
    /// @param timeProvider A function for the tick value
    @ApiStatus.NonExtendable
    public RepeatingBehaviour<BO> runFor(ToIntFunction<BO> timeProvider) {
        return (RepeatingBehaviour<BO>)super.runFor(timeProvider);
    }

    /// Prevent a tick-based timeout for this behaviour; and instead rely exclusively on other conditions
    /// such as [#getMemoryRequirements()] failing or [#stopIf(Predicate)]
    @ApiStatus.NonExtendable
    public RepeatingBehaviour<BO> noTimeout() {
        return (RepeatingBehaviour<BO>)super.noTimeout();
    }

    /// Set the length (in ticks) that the task should wait for between activations<br/>
    /// This is the time between when the task stops, and it is able to start again
    ///
    /// @param ticks The number of ticks to cooldown for
    @ApiStatus.NonExtendable
    public RepeatingBehaviour<BO> cooldownFor(int ticks) {
        return (RepeatingBehaviour<BO>)super.cooldownFor(ticks);
    }

    /// Set the length (in ticks) that the task should wait for between activations<br/>
    /// This is the time between when the task stops, and it is able to start again
    ///
    /// @param minTicks The minimum number of ticks to cooldown for
    /// @param maxTicks The maximum number of ticks to cooldown for
    @ApiStatus.NonExtendable
    public RepeatingBehaviour<BO> cooldownFor(int minTicks, int maxTicks) {
        return (RepeatingBehaviour<BO>)super.cooldownFor(minTicks, maxTicks);
    }

    /// Set the length (in ticks) that the task should wait for between activations<br/>
    /// This is the time between when the task stops, and it is able to start again
    ///
    /// @param timeProvider A function for the tick value to cooldown for
    @ApiStatus.NonExtendable
    public RepeatingBehaviour<BO> cooldownFor(ToIntFunction<BO> timeProvider) {
        return (RepeatingBehaviour<BO>)super.cooldownFor(timeProvider);
    }

    /// Set an additional condition for the behaviour to be able to start
    ///
    /// Prevents this behaviour starting unless this predicate returns true.
    ///
    /// @param predicate The condition for starting
    @ApiStatus.NonExtendable
    public RepeatingBehaviour<BO> startCondition(Predicate<BO> predicate) {
        return (RepeatingBehaviour<BO>)super.startCondition(predicate);
    }

    /// Set an automatic condition for the behaviour to stop<br/>
    /// Has no effect on one-shot behaviours that don't have a runtime
    ///
    /// Stops the behaviour if it is active and this predicate returns true
    ///
    /// @param predicate The condition to cause an early stop of the behaviour
    @ApiStatus.NonExtendable
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
    /// @see MemoryTest
    /// @return The [List] of [Memories][MemoryModuleType] and their associated required [status][MemoryStatus]
    @Override
    public List<MemoryCondition<?, ?>> getMemoryRequirements() {
        return this.child.getMemoryRequirements();
    }

    @Override
    protected boolean doStartCheck(ServerLevel level, BO entity, long gameTime) {
        return super.doStartCheck(level, entity, gameTime) && this.child.tryStart(level, entity, gameTime);
    }

    @Override
    protected void start(ServerLevel level, BO entity, long gameTime) {
        super.start(level, entity, gameTime);

        this.repeats = this.repeatCountProvider.applyAsInt(entity);
    }

    /// Determine whether the conditions for this behaviour are still valid for the current tick
    ///
    /// This method should not modify the behaviour in any way and should only act as a read-only view of applicability
    @Override
    protected boolean canStillUse(ServerLevel level, BO entity, long gameTime) {
        return (this.child.getStatus() != Status.STOPPED || this.repeats > 0) && !this.stopCondition.test(entity);
    }

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