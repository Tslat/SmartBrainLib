package net.tslat.smartbrainlib.example.boilerplate;

import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.tslat.smartbrainlib.SBLConstants;
import net.tslat.smartbrainlib.SBLNeoForge;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
final class SBLClient {
	@SubscribeEvent
	public static void registerEntityRenderers(final EntityRenderersEvent.RegisterRenderers ev) {
		if (SBLConstants.SBL_LOADER.isDevEnv())
			ev.registerEntityRenderer(SBLNeoForge.SBL_SKELETON.get(), SkeletonRenderer::new);
	}
}
