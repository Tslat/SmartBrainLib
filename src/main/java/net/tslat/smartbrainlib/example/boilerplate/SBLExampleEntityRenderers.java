package net.tslat.smartbrainlib.example.boilerplate;

import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.loading.FMLLoader;
import net.tslat.smartbrainlib.SmartBrainLib;

@EventBusSubscriber(modid = SmartBrainLib.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
final class SBLExampleEntityRenderers {
	
	@SubscribeEvent
	public static void onClientSetup(final FMLClientSetupEvent event) {
		if (!FMLLoader.isProduction()) {
			RenderingRegistry.registerEntityRenderingHandler(SBLExampleEntities.SBL_SKELETON.get(), SkeletonRenderer::new);
		}
	}
}
