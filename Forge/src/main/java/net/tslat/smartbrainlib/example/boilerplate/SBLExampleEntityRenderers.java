package net.tslat.smartbrainlib.example.boilerplate;

import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.entity.EntityType;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.tslat.smartbrainlib.SBLConstants;

import java.util.function.Supplier;

public final class SBLExampleEntityRenderers {
	public static void registerEntityRenderers() {
		if (SBLConstants.SBL_LOADER.isDevEnv())
			RenderingRegistry.registerEntityRenderingHandler((EntityType)((Supplier)SBLExampleEntities.SBL_SKELETON).get(), SkeletonRenderer::new);
	}
}
