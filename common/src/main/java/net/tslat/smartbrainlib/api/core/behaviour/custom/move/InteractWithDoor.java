package net.tslat.smartbrainlib.api.core.behaviour.custom.move;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;
import net.tslat.smartbrainlib.library.interfaces.TriPredicate;
import net.tslat.smartbrainlib.library.object.MemoryTest;
import net.tslat.smartbrainlib.util.BrainUtil;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/// SmartBrainLib equivalent of vanilla's [net.minecraft.world.entity.ai.behavior.InteractWithDoor]<br/>
/// By default, it causes entities who are traversing a doorway to open an interceding door, then close it once it has walked through, without interrupting the path
///
/// It will also hold the door open if other entities are traversing the doorway at the same time
///
/// @param <BO> The brain owner entity
public class InteractWithDoor<BO extends LivingEntity> extends ExtendedBehaviour<BO> {
    protected static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(3).hasMemory(MemoryModuleType.PATH).usesMemories(MemoryModuleType.DOORS_TO_CLOSE, MemoryModuleType.NEAREST_LIVING_ENTITIES);

    protected ToIntFunction<BO> doorInteractionDelay = _ -> 20;
    protected TriPredicate<BO, LivingEntity, BlockPos> holdDoorsOpenFor = (entity, other, doorPos) -> entity.getType() == other.getType() && doorPos.closerToCenterThan(other.position(), 2);

    protected int doorCloseCooldown = -1;
    protected @Nullable Node lastNode = null;
    
    /// Set a specific [EntityType] that the entity should hold doors open for when nearby
    @ApiStatus.NonExtendable
    public InteractWithDoor<BO> holdDoorsOpenFor(EntityType<?> entityType) {
        return holdDoorsOpenFor((_, other, doorPos) -> other.getType() == entityType && doorPos.closerToCenterThan(other.position(), 2));
    }
    
    /// Override the predicate that determines if and when the entity should hold the door open for nearby entities
    @ApiStatus.NonExtendable
    public InteractWithDoor<BO> holdDoorsOpenFor(TriPredicate<BO, LivingEntity, BlockPos> predicate) {
        this.holdDoorsOpenFor = predicate;

        return this;
    }

    /// Set the length of time (in ticks) between moving to/away from a door and interacting with it
    ///
    /// This should be considered more of a guideline than a hard-and-fast rule
    @ApiStatus.NonExtendable
    public InteractWithDoor<BO> doorInteractionDelay(int ticks) {
        return doorInteractionDelay(_ -> ticks);
    }
    
    /// Set a function to determine the length of time (in ticks) between moving to/away from a door and interacting with it
    ///
    /// This should be considered more of a guideline than a hard-and-fast rule
    @ApiStatus.NonExtendable
    public InteractWithDoor<BO> doorInteractionDelay(ToIntFunction<BO> delay) {
        this.doorInteractionDelay = delay;

        return this;
    }
    
    //<editor-fold defaultstate="collapsed" desc="<Polymorphic Overloads>">
    /// Set a callback for when the behaviour successfully begins
    ///
    /// This is called immediately prior to [ExtendedBehaviour#start(LivingEntity)]
    @ApiStatus.NonExtendable
    @Override
    public InteractWithDoor<BO> whenStarting(Consumer<BO> callback) {
        return (InteractWithDoor<BO>)super.whenStarting(callback);
    }
    
    /// Set a callback for when the behaviour stops
    ///
    /// This is called immediately prior to [ExtendedBehaviour#stop(LivingEntity)]
    ///
    /// Note that the behaviour stopping does not necessarily mean it was successful
    @ApiStatus.NonExtendable
    @Override
    public InteractWithDoor<BO> whenStopping(Consumer<BO> callback) {
        return (InteractWithDoor<BO>)super.whenStopping(callback);
    }
    
    /// Set the number of ticks the behaviour should try to run for, once started
    ///
    /// The behaviour may still be stopped before this time runs out through other conditions or manual stops
    @ApiStatus.NonExtendable
    @Override
    public InteractWithDoor<BO> runFor(int ticks) {
        return (InteractWithDoor<BO>)super.runFor(ticks);
    }
    
    /// Set the range of ticks the behaviour should try to run for, once started<br/>
    /// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is run
    ///
    /// The behaviour may still be stopped before this time runs out through other conditions or manual stops
    @ApiStatus.NonExtendable
    @Override
    public InteractWithDoor<BO> runFor(int minTicks, int maxTicks) {
        return (InteractWithDoor<BO>)super.runFor(minTicks, maxTicks);
    }
    
    /// Set a function to determine the number of ticks the behaviour should try to run for, once started
    ///
    /// The behaviour may still be stopped before this time runs out through other conditions or manual stops
    @ApiStatus.NonExtendable
    @Override
    public InteractWithDoor<BO> runFor(ToIntFunction<BO> timeProvider) {
        return (InteractWithDoor<BO>)super.runFor(timeProvider);
    }
    
    /// Disable the tick-based timeout for this behaviour and instead rely exclusively on other conditions
    /// such as [#getMemoryRequirements()] failing or [#stopIf(Predicate)]
    @ApiStatus.NonExtendable
    @Override
    public InteractWithDoor<BO> noTimeout() {
        return (InteractWithDoor<BO>)super.noTimeout();
    }
    
    /// Set the number of ticks that this behaviour should be prevented from starting again after it has finished
    ///
    /// This is the length of time between when this behaviour stops and it can start again
    @ApiStatus.NonExtendable
    @Override
    public InteractWithDoor<BO> cooldownFor(int ticks) {
        return (InteractWithDoor<BO>)super.cooldownFor(ticks);
    }
    
    /// Set the range of ticks that this behaviour should be prevented from starting again after it has finished<br/>
    /// The actual duration will be a random number selected between the min and max values (inclusive) each time the behaviour is stopped
    ///
    /// This is the length of time between when this behaviour stops and it can start again
    @ApiStatus.NonExtendable
    @Override
    public InteractWithDoor<BO> cooldownFor(int minTicks, int maxTicks) {
        return (InteractWithDoor<BO>)super.cooldownFor(minTicks, maxTicks);
    }
    
    /// Set a function to determine the number of ticks that this behaviour should be prevented from starting again after it has finished
    ///
    /// This is the length of time between when this behaviour stops and it can start again
    @ApiStatus.NonExtendable
    @Override
    public InteractWithDoor<BO> cooldownFor(ToIntFunction<BO> timeProvider) {
        return (InteractWithDoor<BO>)super.cooldownFor(timeProvider);
    }
    
    /// Set an additional condition for the behaviour to be able to start
    ///
    /// Prevents this behaviour starting unless this predicate returns true
    @ApiStatus.NonExtendable
    @Override
    public InteractWithDoor<BO> startCondition(Predicate<BO> predicate) {
        return (InteractWithDoor<BO>)super.startCondition(predicate);
    }
    
    /// Set a condition under which the behaviour should automatically stop<br/>
    /// Has no effect on one-shot behaviours that don't tick or have a runtime
    ///
    /// Stops the behaviour immediately if the predicate returns true, ready to run again
    @ApiStatus.NonExtendable
    @Override
    public InteractWithDoor<BO> stopIf(Predicate<BO> predicate) {
        return (InteractWithDoor<BO>)super.stopIf(predicate);
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
        return MEMORY_REQUIREMENTS;
    }
    
    /// Check any extra conditions required for this behaviour to start
    ///
    /// By this stage, memory conditions from [#getMemoryRequirements()] have already been checked
    ///
    /// @return Whether the conditions have been met to start the behaviour
    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, BO entity) {
        return isPathStillValid(BrainUtil.getMemory(entity, MemoryModuleType.PATH));
    }
    
    /// Check any additional conditions for whether the behaviour should continue running<br/>
    /// This is checked before [#tick(LivingEntity)]
    ///
    /// The behaviour's [runtime][#runFor] has already been checked at this stage, and [#stopIf(Predicate)] will be checked immediately after this method if returning true
    ///
    /// Memories are not guaranteed to be in their required state here, so if you have required memories, it might be worth checking them here
    @Override
    protected boolean shouldKeepRunning(BO entity) {
        return isPathStillValid(BrainUtil.getMemory(entity, MemoryModuleType.PATH));
    }
    
    /// Run the per-tick behaviour for this behaviour<br/>
    /// This is called <u>every tick</u> this behaviour is active, so be aware of any performance-intensive functionality handled here
    ///
    /// [#shouldKeepRunning(LivingEntity)] and [#stopIf(Predicate)] have both been checked immediately prior to this method being called
    ///
    /// NOTE: Memory requirements are __not__ guaranteed at this stage. If you are retrieving brain memories, you may need to check their presence before use
    @Override
    protected void tick(BO entity) {
        final ServerLevel level = (ServerLevel)entity.level();
        final Path path = BrainUtil.getMemory(entity, MemoryModuleType.PATH);
        //noinspection DataFlowIssue
        final BlockInWorld prevNodeBlock = new BlockInWorld(level, path.getPreviousNode().asBlockPos(), false);
        final BlockInWorld nextNodeBlock = new BlockInWorld(level, path.getNextNodePos(), false);

        if (this.doorCloseCooldown < 0) {
            this.doorCloseCooldown = this.doorInteractionDelay.applyAsInt(entity);
            this.lastNode = path.getNextNode();
        }

        if (!Objects.equals(this.lastNode, path.getNextNode()) && --this.doorCloseCooldown < 0)
            return;

        final Set<GlobalPos> doorsToClose = BrainUtil.memoryOrDefault(entity, MemoryModuleType.DOORS_TO_CLOSE, new ObjectOpenHashSet<>());
        
        checkAndCloseDoors(level, entity, doorsToClose, prevNodeBlock, nextNodeBlock);
        
        final DoorBlock prevNodeDoor = getInteractableDoor(prevNodeBlock);
        final DoorBlock nextNodeDoor = getInteractableDoor(nextNodeBlock);
        
        if (prevNodeDoor != null)
            tryOpenDoor(level, entity, doorsToClose, prevNodeBlock, prevNodeDoor);

        if (nextNodeDoor != null)
            tryOpenDoor(level, entity, doorsToClose, nextNodeBlock, nextNodeDoor);
        
        BrainUtil.setMemory(entity, MemoryModuleType.DOORS_TO_CLOSE, doorsToClose);
    }
    
    /// @return Whether the given [Path] still exists, and is mid-traversal for the purpose of opening/closing doors
    protected boolean isPathStillValid(@Nullable Path path) {
        return path != null && !path.notStarted() && !path.isDone();
    }
    
    /// Get a valid [DoorBlock] from the given block if it is a door and can be interacted with
    protected @Nullable DoorBlock getInteractableDoor(BlockInWorld block) {
        final BlockState state = block.getState();
	    
	    //noinspection ConstantValue
	    if (state != null && state.getBlock() instanceof DoorBlock door && state.is(BlockTags.MOB_INTERACTABLE_DOORS))
            return door;
        
        return null;
    }

    /// Check through the existing known door positions and attempt to close them
    protected void checkAndCloseDoors(ServerLevel level, BO entity, Set<GlobalPos> doorsToClose, BlockInWorld prevNode, BlockInWorld nextNode) {
        final List<LivingEntity> nearbyEntities = BrainUtil.memoryOrDefault(entity, MemoryModuleType.NEAREST_LIVING_ENTITIES, List.of());
        
        for (Iterator<GlobalPos> iterator = doorsToClose.iterator(); iterator.hasNext();) {
            final GlobalPos doorLocation = iterator.next();
            final BlockPos doorPos = doorLocation.pos();
            final BlockInWorld door = new BlockInWorld(level, doorPos, false);

            if (doorPos.equals(prevNode.getPos()) || doorPos.equals(nextNode.getPos()))
                continue;

            if (doorLocation.dimension() != level.dimension() || !doorPos.closerToCenterThan(entity.position(), 3)) {
                iterator.remove();

                continue;
            }

            final BlockState doorState = door.getState();
            final DoorBlock doorBlock = getInteractableDoor(door);

            if (doorBlock != null && doorBlock.isOpen(doorState) && !shouldHoldDoorOpenForOthers(entity, doorPos, nearbyEntities))
                doorBlock.setOpen(entity, level, doorState, doorPos, false);

            iterator.remove();
        }
    }

    /// Determine whether the entity should leave a door open (instead of closing it) for any nearby entities that are about to walk through it
    protected boolean shouldHoldDoorOpenForOthers(BO entity, BlockPos doorPos, List<LivingEntity> others) {
        for (LivingEntity other : others) {
            if (!this.holdDoorsOpenFor.test(entity, other, doorPos))
                continue;

            final Path path = BrainUtil.getMemory(other, MemoryModuleType.PATH);

            if (path == null || path.isDone() || path.notStarted())
                continue;
	        
	        //noinspection DataFlowIssue
	        if (path.getPreviousNode().asBlockPos().equals(doorPos) || path.getNextNode().asBlockPos().equals(doorPos))
                return true;
        }

        return false;
    }

    /// Attempt to open a [DoorBlock], saving it to memory to close later
    protected void tryOpenDoor(ServerLevel level, BO entity, Set<GlobalPos> doorsToClose, BlockInWorld block, DoorBlock doorBlock) {
        final BlockState state = block.getState();
	    
	    //noinspection ConstantValue
	    if (state != null && !doorBlock.isOpen(state)) {
            doorBlock.setOpen(entity, level, state, block.getPos(), true);
            doorsToClose.add(new GlobalPos(level.dimension(), block.getPos()));
        }
    }
    //</editor-fold>
}
