package net.tslat.smartbrainlib.example.boilerplate;

import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.tslat.smartbrainlib.SBLConstants;

/// Client-code boilerplate class for example implementations for `SmartBrainLib`
@EventBusSubscriber(value = Dist.CLIENT)
public final class SBLExampleClient {
	/// Register the renderers for `SmartBrainLib`'s example entities
	@SubscribeEvent
	public static void registerEntityRenderers(final EntityRenderersEvent.RegisterRenderers ev) {
		if (SBLConstants.PLATFORM.isDevEnv())
			ev.registerEntityRenderer(SBLExampleCommon.SKELETON.get(), SkeletonRenderer::new);
	}
}
