package net.tslat.smartbrainlib.api.core.behaviour;

import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/**
 * Wrapper behaviour for a behaviour that should repeat from the start once finished, unless otherwise stated
 * <br>
 * While running and repeating, this behaviour is considered as still running.<br>
 * It will stop when it either runs out of repeats or the stop condition is met.
 */
public class RepeatingBehaviour<E extends LivingEntity> extends ExtendedBehaviour<E> {
    protected final ExtendedBehaviour<E> child;

    protected Predicate<E> shouldRepeat = entity -> true;
    protected ToIntFunction<E> repeatCountProvider = entity -> Integer.MAX_VALUE;

    protected int repeats;

    public RepeatingBehaviour(ExtendedBehaviour<E> child) {
        super();

        this.child = child;

        for (Pair<MemoryModuleType<?>, MemoryStatus> memoryReq : getMemoryRequirements()) {
            this.entryCondition.put(memoryReq.getFirst(), memoryReq.getSecond());
        }

        noTimeout();
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return this.child == null ? List.of() : this.child.getMemoryRequirements();
    }

    /**
     * Set the predicate that determines whether the wrapped behaviour should repeat or not at any given time
     *
     * @param predicate The predicate
     * @return this
     */
    public RepeatingBehaviour<E> repeatingWhen(Predicate<E> predicate) {
        this.shouldRepeat = predicate;

        return this;
    }

    /**
     * Limit the amount of repeats this behaviour should have when running.
     * <br>
     * 1 repeat results in the wrapped behaviour running twice
     *
     * @param repeats The number of times to repeat the behaviour
     * @return this
     */
    public RepeatingBehaviour<E> repeatNTimes(int repeats) {
        return repeatNTimes(entity -> repeats);
    }

    /**
     * Limit the amount of repeats this behaviour should have when running.
     * <br>
     * 1 repeat results in the wrapped behaviour running twice
     *
     * @param function The number of times to repeat the behaviour
     * @return this
     */
    public RepeatingBehaviour<E> repeatNTimes(ToIntFunction<E> function) {
        this.repeatCountProvider = function;

        return this;
    }

    @Override
    protected boolean doStartCheck(ServerLevel level, E entity, long gameTime) {
        return super.doStartCheck(level, entity, gameTime) && this.child.tryStart(level, entity, gameTime);
    }

    @Override
    protected void start(ServerLevel level, E entity, long gameTime) {
        super.start(level, entity, gameTime);

        this.repeats = this.repeatCountProvider.applyAsInt(entity);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, E entity, long gameTime) {
        return !this.stopCondition.test(entity) && (this.child.getStatus() != Status.STOPPED || this.repeats > 0);
    }

    @Override
    protected void tick(ServerLevel level, E entity, long gameTime) {
        super.tick(level, entity, gameTime);

        if (this.child.getStatus() != Status.STOPPED) {
            this.child.tickOrStop(level, entity, gameTime);

            return;
        }

        if (this.repeats > 0 && this.shouldRepeat.test(entity) && this.child.tryStart(level, entity, gameTime))
            this.repeats--;
    }

    @Override
    protected void stop(ServerLevel level, E entity, long gameTime) {
        super.stop(level, entity, gameTime);

        if (this.child.getStatus() != Status.STOPPED)
            this.child.doStop(level, entity, gameTime);
    }
}