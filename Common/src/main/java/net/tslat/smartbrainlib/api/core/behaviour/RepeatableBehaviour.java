package net.tslat.smartbrainlib.api.core.behaviour;

import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.tslat.smartbrainlib.object.SBLShufflingList;
import net.tslat.smartbrainlib.util.RandomUtil;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

/**
 * Group behaviour that runs the first behavior a set or random amount of times.<br>
 * Restarts upon reaching the repeat limit
 * @param <E> The entity
 */
public class RepeatableBehaviour<E extends LivingEntity> extends GroupBehaviour<E> {
    protected Predicate<E> repeatPredicate = livingEntity -> true;
    private int repeatCount = 0;
    private int maxRepeat = 0;

    public RepeatableBehaviour(Pair<ExtendedBehaviour<? super E>, Integer>... behaviours) {
        super(behaviours);
    }

    public RepeatableBehaviour(ExtendedBehaviour<? super E>... behaviours) {
        super(behaviours);
    }

    /**
     * Adds a callback predicate to repeat the behaviour
     */
    public RepeatableBehaviour<E> repeatIf(Predicate<E> predicate) {
        this.repeatPredicate = predicate;

        return this;
    }

    /**
     * Sets the amount of times the behaviour should repeat
     */
    public RepeatableBehaviour<E> repeat(int maxRepeat) {
        this.maxRepeat = maxRepeat;

        return this;
    }

    /**
     * Sets the amount of times the behaviour should repeat between two numbers
     */
    public RepeatableBehaviour<E> repeat(int minRepeat, int maxRepeat) {
        return repeat(RandomUtil.randomNumberBetween(minRepeat, maxRepeat));
    }

    @Override
    protected boolean shouldKeepRunning(E entity) {
        return this.runningBehaviour != null && this.runningBehaviour.getStatus() != Status.STOPPED;
    }

    @Override
    protected boolean timedOut(long gameTime) {
        return this.runningBehaviour != null ? super.timedOut(gameTime) && this.repeatCount >= this.maxRepeat : super.timedOut(gameTime);
    }

    @Override
    protected void tick(ServerLevel level, E owner, long gameTime) {
        this.runningBehaviour.tickOrStop(level, owner, gameTime);

        if (this.runningBehaviour.getStatus() == Status.STOPPED) {
            if (pickBehaviour(level, owner, gameTime, this.behaviours) != null)
                return;

            doStop(level, owner, gameTime);
        }
    }

    @Override
    protected @Nullable ExtendedBehaviour<? super E> pickBehaviour(ServerLevel level, E entity, long gameTime, SBLShufflingList<ExtendedBehaviour<? super E>> extendedBehaviours) {
        if (this.repeatCount >= this.maxRepeat)
            return null;

        ExtendedBehaviour<? super E> repeat = extendedBehaviours.get(0);

        if (repeat != null && repeatPredicate.test(entity) && repeat.tryStart(level, entity, gameTime)) {
            this.runningBehaviour = repeat;
            this.repeatCount++;

            return this.runningBehaviour;
        }

        return null;
    }

    @Override
    protected void stop(ServerLevel level, E entity, long gameTime) {
        super.stop(level, entity, gameTime);
        this.repeatCount = 0;
    }
}
