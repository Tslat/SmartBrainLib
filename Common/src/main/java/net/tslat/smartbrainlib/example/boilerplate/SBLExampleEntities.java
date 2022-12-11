package net.tslat.smartbrainlib.example.boilerplate;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.tslat.smartbrainlib.SBLConstants;
import net.tslat.smartbrainlib.example.SBLSkeleton;

import java.util.function.Supplier;

public final class SBLExampleEntities {
	public static void init() {}

	public static final Supplier<EntityType<SBLSkeleton>> SBL_SKELETON = SBLConstants.SBL_LOADER.registerEntityType("sbl_skeleton", () ->
			EntityType.Builder.of(SBLSkeleton::new, MobCategory.MONSTER).sized(0.6f, 1.99f).clientTrackingRange(8).build("sbl_skeleton"));
}
