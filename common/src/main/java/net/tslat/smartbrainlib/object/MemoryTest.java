package net.tslat.smartbrainlib.object;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

/**
 * Wrapper helper class to allow for cleaner and more legible memory conditions for behaviours and sensors.
 * <p>
 * Functionally, this is just a list; so usage of this class is entirely optional although recommended.
 */
public class MemoryTest extends ObjectArrayList<Pair<MemoryModuleType<?>, MemoryStatus>> {
    private MemoryTest(int size) {
        super(size);
    }

    /**
     * Create a new MemoryTest instance, with a predefined size.
     *
     * @param size The intended number of {@link MemoryModuleType}s this test will contain
     * @return A new MemoryTest instance
     */
    public static MemoryTest builder(int size) {
        return new MemoryTest(size);
    }

    /**
     * Create a new MemoryTest instance, with a default initial size.<br>
     * You should use {@link #builder(int)} wherever possible, since it is more efficient.
     *
     * @return A new MemoryTest instance
     */
    public static MemoryTest builder() {
        return builder(ObjectArrayList.DEFAULT_INITIAL_CAPACITY);
    }

    /**
     * Add a condition for the memory having a value set
     *
     * @param memory The memory
     * @return this
     */
    public MemoryTest hasMemory(MemoryModuleType<?> memory) {
        return add(memory, MemoryStatus.VALUE_PRESENT);
    }

    /**
     * Add a condition for all the memories having a value set
     *
     * @param memories The memories
     * @return this
     */
    public MemoryTest hasMemories(MemoryModuleType<?>... memories) {
        for (MemoryModuleType<?> memory : memories) {
            hasMemory(memory);
        }

        return this;
    }

    /**
     * Add a condition for the memory not having a value set
     *
     * @param memory The memory
     * @return this
     */
    public MemoryTest noMemory(MemoryModuleType<?> memory) {
        return add(memory, MemoryStatus.VALUE_ABSENT);
    }

    /**
     * Add a condition for none of the memories having a value set
     *
     * @param memories The memories
     * @return this
     */
    public MemoryTest hasNoMemories(MemoryModuleType<?>... memories) {
        for (MemoryModuleType<?> memory : memories) {
            noMemory(memory);
        }

        return this;
    }

    /**
     * Adds a condition for the memory being used, but a specific status isn't required
     *
     * @param memory The memory
     * @return this
     */
    public MemoryTest usesMemory(MemoryModuleType<?> memory) {
        return add(memory, MemoryStatus.REGISTERED);
    }

    /**
     * Add a condition for all the memories being used, but a specific status isn't required
     *
     * @param memories The memories
     * @return this
     */
    public MemoryTest usesMemories(MemoryModuleType<?>... memories) {
        for (MemoryModuleType<?> memory : memories) {
            usesMemory(memory);
        }

        return this;
    }

    public MemoryTest add(MemoryModuleType<?> memory, MemoryStatus status) {
        super.add(Pair.of(memory, status));

        return this;
    }
}
