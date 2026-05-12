package net.tslat.smartbrainlib.example.boilerplate;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.tslat.smartbrainlib.SBLConstants;
import net.tslat.smartbrainlib.example.SBLSkeleton;

/// Common-code boilerplate class for example implementations for `SmartBrainLib`
public final class SBLExampleCommon {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, SBLConstants.MOD_ID);

    public static DeferredHolder<EntityType<?>, EntityType<SBLSkeleton>> SKELETON = ENTITY_TYPES.register("sbl_skeleton", () -> EntityType.Builder.of(SBLSkeleton::new, MobCategory.MONSTER).sized(0.6f, 1.99f).build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(SBLConstants.MOD_ID, "sbl_skeleton"))));

    /// Perform initial setup
    public static void init(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);

        eventBus.addListener(EntityAttributeCreationEvent.class, SBLExampleCommon::registerEntityAttributes);
    }

    /// Register example entity [Attribute]s
    private static void registerEntityAttributes(final EntityAttributeCreationEvent ev) {
        ev.put(SKELETON.get(), SBLSkeleton.createAttributes().build());
    }
}
