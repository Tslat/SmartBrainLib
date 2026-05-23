package net.tslat.smartbrainlib.library.object;

import it.unimi.dsi.fastutil.Arrays;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/// Wrapper around a [List] implementation to provide a cleaner and more legible interface for memory conditions for [behaviours][ExtendedBehaviour#getMemoryRequirements()]
///
/// Typically, you would cache a static instance of this class in your behaviour class for fast returning
///
/// Use of this class isn't strictly required but is strongly recommended
public class MemoryTest extends ObjectArrayList<MemoryCondition<?, ?>> {
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
        return add(memory, MemoryCondition.Present::new);
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
        return add(memory, MemoryCondition.Absent::new);
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
        return add(memory, MemoryCondition.Registered::new);
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
    protected <M extends MemoryModuleType<?>> MemoryTest add(M memory, Function<M, MemoryCondition<?, ?>> conditionFactory) {
        grow(this.size + 1);
        this.a[this.size++] = conditionFactory.apply(memory);
        assert this.size <= this.a.length;

        return this;
    }

    @ApiStatus.Internal
    protected <M extends MemoryModuleType<?>> @Nullable MemoryCondition<?, M> remove(M memory) {
        for (int i = 0; i < size(); i++) {
            final MemoryCondition<?, ?> condition = get(i);

            //noinspection ConstantValue
            if (condition != null && condition.memory() == memory) {
                final MemoryCondition<?, ?>[] a = this.a;
                this.size--;

                if (i != this.size)
                    System.arraycopy(a, i + 1, a, i, this.size - i);

                //noinspection DataFlowIssue
                a[this.size] = null;

                //noinspection unchecked
                return (MemoryCondition<?, M>)condition;
            }
        }

        return null;
    }

    @ApiStatus.Internal
    protected boolean contains(MemoryModuleType<?> memory) {
        for (MemoryCondition<?, ?> condition : this) {
            //noinspection ConstantValue
            if (condition != null && condition.memory() == memory)
                return true;
        }

        return false;
    }
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="<Immutability Handling>">
    /// Copy of [ObjectArrayList#grow(int)] due to access properties not allowing extended calls
    private void grow(int capacity) {
        if (capacity <= this.a.length)
            return;

        if (this.a != ObjectArrays.DEFAULT_EMPTY_ARRAY) {
            capacity = Math.clamp((long)this.a.length + (this.a.length >> 1), capacity, Arrays.MAX_ARRAY_SIZE);
        }
        else if (capacity < DEFAULT_INITIAL_CAPACITY) {
            capacity = DEFAULT_INITIAL_CAPACITY;
        }

        if (this.wrapped) {
            this.a = ObjectArrays.forceCapacity(this.a, capacity, this.size);
        }
        else {
            final Object[] array = new Object[capacity];

            System.arraycopy(this.a, 0, array, 0, this.size);

            //noinspection DataFlowIssue
            this.a = (MemoryCondition<?, ?>[])array;
        }

        assert this.size <= this.a.length;
    }

    @Override
    public boolean addAll(Collection<? extends MemoryCondition<?, ?>> collection) {
        throw new UnsupportedOperationException("Custom modification of MemoryTests is not allowed");
    }

    @Override
    public boolean addAll(ObjectList<? extends MemoryCondition<?, ?>> list) {
        throw new UnsupportedOperationException("Custom modification of MemoryTests is not allowed");
    }

    @Override
    public boolean addAll(int index, Collection<? extends MemoryCondition<?, ?>> collection) {
        throw new UnsupportedOperationException("Custom modification of MemoryTests is not allowed");
    }

    @Override
    public boolean addAll(int index, ObjectList<? extends MemoryCondition<?, ?>> list) {
        throw new UnsupportedOperationException("Custom modification of MemoryTests is not allowed");
    }

    @Override
    public boolean remove(Object value) {
        throw new UnsupportedOperationException("Custom modification of MemoryTests is not allowed");
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        throw new UnsupportedOperationException("Custom modification of MemoryTests is not allowed");
    }

    @Override
    public MemoryCondition<?, ?> remove(int index) {
        throw new UnsupportedOperationException("Custom modification of MemoryTests is not allowed");
    }

    @Override
    public MemoryCondition<?, ?> removeFirst() {
        throw new UnsupportedOperationException("Custom modification of MemoryTests is not allowed");
    }

    @Override
    public MemoryCondition<?, ?> removeLast() {
        throw new UnsupportedOperationException("Custom modification of MemoryTests is not allowed");
    }

    @Override
    public boolean removeIf(Predicate<? super MemoryCondition<?, ?>> filter) {
        throw new UnsupportedOperationException("Custom modification of MemoryTests is not allowed");
    }

    @Override
    public void removeElements(int from, int to) {
        throw new UnsupportedOperationException("Custom modification of MemoryTests is not allowed");
    }

    @ApiStatus.Internal
    @Override
    public boolean add(MemoryCondition<?, ?> memoryCondition) {
        throw new UnsupportedOperationException("Custom modification of MemoryTests is not allowed");
    }

    @Override
    public void add(int index, MemoryCondition<?, ?> memoryCondition) {
        throw new UnsupportedOperationException("Custom modification of MemoryTests is not allowed");
    }

    @Override
    public void addFirst(MemoryCondition<?, ?> memoryCondition) {
        throw new UnsupportedOperationException("Custom modification of MemoryTests is not allowed");
    }

    @Override
    public void addLast(MemoryCondition<?, ?> memoryCondition) {
        throw new UnsupportedOperationException("Custom modification of MemoryTests is not allowed");
    }

    @Override
    public void addElements(int index, MemoryCondition<?, ?>[] memories) {
        throw new UnsupportedOperationException("Custom modification of MemoryTests is not allowed");
    }

    @Override
    public void addElements(int index, MemoryCondition<?, ?>[] memories, int offset, int length) {
        throw new UnsupportedOperationException("Custom modification of MemoryTests is not allowed");
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        throw new UnsupportedOperationException("Custom modification of MemoryTests is not allowed");
    }
    //</editor-fold>
}
