package net.tslat.smartbrainlib.library.object;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

/// Wrapper around a [List] implementation to provide a cleaner and more legible interface for memory conditions for [behaviours][ExtendedBehaviour#getMemoryRequirements()]
///
/// Typically, you would cache a static instance of this class in your behaviour class for fast returning
///
/// Use of this class isn't strictly required but is strongly recommended
@Unmodifiable
public class MemoryTest extends ObjectArraySet<MemoryCondition<?, ?>> {
    /// Create a new [MemoryTest] instance with a default initial size
    ///
    /// You should use [#builder(int)] wherever possible, since it is more efficient
    public static MemoryTest builder() {
        return new MemoryTest();
    }

    /// Create a new [MemoryTest] instance with a predefined size
    ///
    /// @param size The intended number of [MemoryModuleType]s this test will contain
    public static MemoryTest builder(int size) {
        return new MemoryTest(size);
    }

    /// Add a condition for the provided memory having a value set
    public MemoryTest hasMemory(MemoryModuleType<?> memory) {
        add(memory, MemoryCondition.Present::new);
        
        return this;
    }

    /// Add a condition for all the provided memories having a value set
    public MemoryTest hasMemories(MemoryModuleType<?>... memories) {
        for (MemoryModuleType<?> memory : memories) {
            hasMemory(memory);
        }

        return this;
    }

    /// Add a condition for the provided memory not having a value set
    public MemoryTest noMemory(MemoryModuleType<?> memory) {
        add(memory, MemoryCondition.Absent::new);
        
        return this;
    }

    /// Add a condition for none of the provided memories having a value set
    public MemoryTest noMemories(MemoryModuleType<?>... memories) {
        for (MemoryModuleType<?> memory : memories) {
            noMemory(memory);
        }

        return this;
    }

    /// Adds a condition for the provided memory being used, but a specific status isn't required
    public MemoryTest usesMemory(MemoryModuleType<?> memory) {
        add(memory, MemoryCondition.Registered::new);
        
        return this;
    }

    /// Add a condition for all the provided memories being used, but a specific status isn't required
    public MemoryTest usesMemories(MemoryModuleType<?>... memories) {
        for (MemoryModuleType<?> memory : memories) {
            usesMemory(memory);
        }

        return this;
    }

    //<editor-fold defaultstate="collapsed" desc="<Internal Handling>">
    protected MemoryTest() {}

    protected MemoryTest(int size) {
        super(size);
    }

    @ApiStatus.Internal
    protected <M extends MemoryModuleType<?>> boolean add(M memory, Function<M, ? extends MemoryCondition<?, ?>> conditionFactory) {
        final MemoryCondition<?, ?> existingCondition = get(memory);
        final MemoryCondition<?, ?> newCondition = conditionFactory.apply(memory);
        
        if (existingCondition == null || shouldConditionReplace(existingCondition, newCondition)) {
            add(conditionFactory.apply(memory));
            
            return true;
        }

        return false;
    }

    @ApiStatus.Internal
    protected boolean remove(MemoryModuleType<?> memory) {
        boolean removed = false;
	    
	    //noinspection Java8CollectionRemoveIf
	    for (Iterator<MemoryCondition<?, ?>> iterator = iterator(); iterator.hasNext();) {
            if (iterator.next().memory() == memory)
                iterator.remove();
        }
        
        return removed;
    }
    
    @ApiStatus.Internal
    protected @Nullable MemoryCondition<?, ?> get(MemoryModuleType<?> memory) {
        for (MemoryCondition<?, ?> condition : this) {
            //noinspection ConstantValue
            if (condition != null && condition.memory() == memory)
                return condition;
        }
        
        return null;
    }

    @ApiStatus.Internal
    protected boolean contains(MemoryModuleType<?> memory) {
        return get(memory) != null;
    }
    
    @ApiStatus.Internal
    protected boolean shouldConditionReplace(MemoryCondition<?, ?> existingCondition, MemoryCondition<?, ?> newCondition) {
        return existingCondition.condition() == MemoryStatus.REGISTERED || newCondition.condition() != MemoryStatus.REGISTERED;
    }
    //</editor-fold>
}
