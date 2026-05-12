package net.tslat.smartbrainlib.example.boilerplate;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.tslat.smartbrainlib.SBLConstants;
import net.tslat.smartbrainlib.example.SBLSkeleton;

/// Common-code boilerplate class for example implementations for `SmartBrainLib`
public final class SBLExampleCommon {
    public static EntityType<SBLSkeleton> SKELETON = registerEntity("sbl_skeleton", EntityType.Builder.of(SBLSkeleton::new, MobCategory.MONSTER).sized(0.6f, 1.99f));

    /// Perform initial setup
    public static void init() {
        registerEntityAttributes();
    }

    /// Register example entity [Attribute]s
    private static void registerEntityAttributes() {
        FabricDefaultAttributeRegistry.register(SKELETON, Skeleton.createAttributes().build());
    }

    /// Helper method for registering entities on `Fabric`
    private static <T extends Entity> EntityType<T> registerEntity(String name, EntityType.Builder<T> builder) {
        final Identifier identifier = SBLConstants.id(name);

        return Registry.register(BuiltInRegistries.ENTITY_TYPE,
                                 identifier,
                                 builder.build(ResourceKey.create(Registries.ENTITY_TYPE, identifier)));
    }
}
