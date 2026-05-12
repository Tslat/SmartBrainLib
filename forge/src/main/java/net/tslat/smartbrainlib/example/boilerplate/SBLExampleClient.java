package net.tslat.smartbrainlib.example.boilerplate;

import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tslat.smartbrainlib.SBLConstants;

/// Client-code boilerplate class for example implementations for `SmartBrainLib`
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public final class SBLExampleClient {
	/// Register the renderers for `SmartBrainLib`'s example entities
	@SubscribeEvent
	public static void registerEntityRenderers(final EntityRenderersEvent.RegisterRenderers ev) {
		if (SBLConstants.PLATFORM.isDevEnv())
			ev.registerEntityRenderer(SBLExampleCommon.SKELETON.get(), SkeletonRenderer::new);
	}
}
