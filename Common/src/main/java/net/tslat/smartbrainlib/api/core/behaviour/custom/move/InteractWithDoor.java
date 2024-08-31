package net.tslat.smartbrainlib.api.core.behaviour.custom.move;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.object.TriPredicate;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.ToIntFunction;

/**
 * SmartBrainLib equivalent of vanilla's {@link net.minecraft.world.entity.ai.behavior.InteractWithDoor}
 * <p>
 * By default, it causes entities who are traversing a doorway to open an interceding door, then close it once it has walked through,
 * without interrupting the path.
 * It will also hold the door open if other entities are traversing the doorway at the same time
 * <p>
 * Defaults:
 * <ul>
 *     <li>Holds doors open for entities of the same type within 2 blocks of the door</li>
 * </ul>
 */
public class InteractWithDoor<E extends LivingEntity> extends ExtendedBehaviour<E> {
    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS = ObjectArrayList.of(Pair.of(MemoryModuleType.PATH, MemoryStatus.VALUE_PRESENT), Pair.of(MemoryModuleType.DOORS_TO_CLOSE, MemoryStatus.REGISTERED), Pair.of(MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryStatus.REGISTERED));

    protected ToIntFunction<E> doorInteractionDelay = entity -> 20;
    protected TriPredicate<E, LivingEntity, BlockPos> holdDoorsOpenFor = (entity, other, doorPos) -> entity.getType() == other.getType() && doorPos.closerToCenterThan(other.position(), 2);

    protected int doorCloseCooldown = -1;
    protected Node lastNode = null;

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    /**
     * Set the predicate that determines what other entities this entity will hold open a door for and under what conditions
     *
     * @param predicate The predicate to test when checking whether an entity should have the door held open for it
     * @return this
     */
    public InteractWithDoor<E> holdDoorsOpenFor(TriPredicate<E, LivingEntity, BlockPos> predicate) {
        this.holdDoorsOpenFor = predicate;

        return this;
    }

    /**
     * Set the tick delay between moving to/away from a door and interacting with it.
     * <p>
     * This should be considered more of a guideline than a hard-and-fast rule
     *
     * @param delay The time between traversing to/from a door, and interacting with it, in ticks
     * @return this
     */
    public InteractWithDoor<E> doorInteractionDelay(ToIntFunction<E> delay) {
        this.doorInteractionDelay = delay;

        return this;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        Path path = BrainUtils.getMemory(entity, MemoryModuleType.PATH);

        return !path.notStarted() && !path.isDone();
    }

    @Override
    protected boolean shouldKeepRunning(E entity) {
        return BrainUtils.hasMemory(entity, MemoryModuleType.PATH) && checkExtraStartConditions((ServerLevel)entity.level(), entity);
    }

    @Override
    protected void tick(E entity) {
        ServerLevel level = (ServerLevel)entity.level();
        Path path = BrainUtils.getMemory(entity, MemoryModuleType.PATH);
        BlockPos prevNodePos = path.getPreviousNode().asBlockPos();
        Node nextNode = path.getNextNode();
        BlockPos nextNodePos = path.getNextNode().asBlockPos();
        BlockState prevNodeBlockState = level.getBlockState(prevNodePos);
        BlockState nextNodeBlockState = level.getBlockState(nextNodePos);

        if (this.doorCloseCooldown < 0) {
            this.doorCloseCooldown = this.doorInteractionDelay.applyAsInt(entity);
            this.lastNode = path.getNextNode();
        }

        if (!Objects.equals(this.lastNode, path.getNextNode()) && --this.doorCloseCooldown < 0)
            return;

        BrainUtils.withMemory(entity, MemoryModuleType.DOORS_TO_CLOSE, doorsToClose -> checkAndCloseDoors(level, entity, doorsToClose, prevNodePos, nextNodePos));

        if (isInteractableDoor(prevNodeBlockState))
            tryOpenDoor(level, entity, prevNodeBlockState, prevNodePos);

        if (isInteractableDoor(nextNodeBlockState))
            tryOpenDoor(level, entity, nextNodeBlockState, nextNodePos);
    }

    protected void checkAndCloseDoors(ServerLevel level, E entity, Set<GlobalPos> doorsToClose, BlockPos prevNodePos, BlockPos nextNodePos) {
        for (Iterator<GlobalPos> iterator = doorsToClose.iterator(); iterator.hasNext();) {
            GlobalPos doorLocation = iterator.next();
            BlockPos doorPos = doorLocation.pos();

            if (doorPos.equals(prevNodePos) || doorPos.equals(nextNodePos))
                continue;

            if (doorLocation.dimension() != level.dimension() || !doorPos.closerToCenterThan(entity.position(), 3)) {
                iterator.remove();

                continue;
            }

            BlockState doorState = level.getBlockState(doorPos);

            if (isInteractableDoor(doorState)) {
                DoorBlock doorBlock = (DoorBlock)doorState.getBlock();

                if (doorBlock.isOpen(doorState) && !shouldHoldDoorOpenForOthers(entity, doorPos, BrainUtils.memoryOrDefault(entity, MemoryModuleType.NEAREST_LIVING_ENTITIES, List::of)))
                    doorBlock.setOpen(entity, level, doorState, doorPos, false);
            }

            iterator.remove();
        }
    }

    protected boolean shouldHoldDoorOpenForOthers(E entity, BlockPos doorPos, List<LivingEntity> others) {
        for (LivingEntity other : others) {
            if (!this.holdDoorsOpenFor.test(entity, other, doorPos))
                continue;

            Path path = BrainUtils.getMemory(entity, MemoryModuleType.PATH);

            if (path == null || path.isDone() || path.notStarted())
                continue;

            if (path.getPreviousNode().asBlockPos().equals(doorPos) || path.getNextNode().asBlockPos().equals(doorPos))
                return true;
        }

        return false;
    }

    protected boolean isInteractableDoor(BlockState state) {
        return state.is(BlockTags.MOB_INTERACTABLE_DOORS) && state.getBlock() instanceof DoorBlock;
    }

    protected void tryOpenDoor(ServerLevel level, E entity, BlockState blockState, BlockPos pos) {
        DoorBlock door = (DoorBlock)blockState.getBlock();

        if (!door.isOpen(blockState)) {
            door.setOpen(entity, level, blockState, pos, true);

            BrainUtils.computeMemoryIfAbsent(entity, MemoryModuleType.DOORS_TO_CLOSE, ObjectOpenHashSet::new).add(new GlobalPos(level.dimension(), pos));
        }
    }
}
