package net.tslat.smartbrainlib.example.boilerplate;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.Skeleton;
import net.tslat.smartbrainlib.SmartBrainLib;
import net.tslat.smartbrainlib.example.SBLSkeleton;

public final class SBLExampleEntities {

	// Example entities begin ---->
	public static final EntityType<SBLSkeleton> SBL_SKELETON = Registry.register(Registry.ENTITY_TYPE,
			new ResourceLocation(SmartBrainLib.MOD_ID, "sbl_skeleton"),
			FabricEntityTypeBuilder.create(MobCategory.MONSTER, SBLSkeleton::new)
					.dimensions(EntityDimensions.fixed(0.6f, 1.99F)).trackRangeChunks(8).trackedUpdateRate(3).build());
	// Example entities end ---->

	public static void initStats() {
		FabricDefaultAttributeRegistry.register(SBL_SKELETON, Skeleton.createAttributes());
	}
}
