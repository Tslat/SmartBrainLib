package net.tslat.smartbrainlib.library.object;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;
import org.apache.logging.log4j.util.InternalApi;

import java.util.List;

/// Wrapper around a [List] implementation to provide a cleaner and more legible interface for
/// memory conditions for [behaviours][ExtendedBehaviour#getMemoryRequirements()]
///
/// Typically, you would cache a static instance of this class in your behaviour class for fast returning
///
/// Use of this class isn't strictly required but is strongly recommended
public class MemoryTest extends ObjectArrayList<Pair<MemoryModuleType<?>, MemoryStatus>> {
    /// Create a new [MemoryTest] instance with a predefined size
    ///
    /// @param size The intended number of [MemoryModuleType]s this test will contain
    /// @return A new `MemoryTest` instance
    public static MemoryTest sized(int size) {
        return new MemoryTest(size);
    }

    /// Create a new [MemoryTest] instance with a default initial size.<br/>
    /// You should use [#sized(int)] wherever possible, since it is more efficient
    ///
    /// @return A new `MemoryTest` instance
    public static MemoryTest builder() {
        return sized(ObjectArrayList.DEFAULT_INITIAL_CAPACITY);
    }

    /// Add a condition for the provided memory having a value set
    public MemoryTest hasMemory(MemoryModuleType<?> memory) {
        return add(memory, MemoryStatus.VALUE_PRESENT);
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
        return add(memory, MemoryStatus.VALUE_ABSENT);
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
        return add(memory, MemoryStatus.REGISTERED);
    }

    /// Add a condition for all the provided memories being used, but a specific status isn't required
    public MemoryTest usesMemories(MemoryModuleType<?>... memories) {
        for (MemoryModuleType<?> memory : memories) {
            usesMemory(memory);
        }

        return this;
    }

    //<editor-fold defaultstate="collapsed" desc="<Boilerplate>">
    private MemoryTest(int size) {
        super(size);
    }

    @InternalApi
    public MemoryTest add(MemoryModuleType<?> memory, MemoryStatus status) {
        super.add(Pair.of(memory, status));

        return this;
    }
    //</editor-fold>
}
