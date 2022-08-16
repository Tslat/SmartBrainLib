package net.tslat.smartbrainlib.example.boilerplate;

import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraftforge.client.event.EntityRenderersEvent;

final class SBLExampleEntityRenderers {
	static void registerEntityRenderers(final EntityRenderersEvent.RegisterRenderers ev) {
		ev.registerEntityRenderer(SBLExampleEntities.SBL_SKELETON.get(), SkeletonRenderer::new);
	}
}
