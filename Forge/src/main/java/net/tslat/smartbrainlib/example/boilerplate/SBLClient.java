package net.tslat.smartbrainlib.example.boilerplate;

import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tslat.smartbrainlib.SBLConstants;
import net.tslat.smartbrainlib.SBLForge;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
final class SBLClient {
	@SubscribeEvent
	public static void registerEntityRenderers(final EntityRenderersEvent.RegisterRenderers ev) {
		if (SBLConstants.SBL_LOADER.isDevEnv())
			ev.registerEntityRenderer(SBLForge.SBL_SKELETON.get(), SkeletonRenderer::new);
	}
}
