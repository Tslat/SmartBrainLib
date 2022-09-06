package net.tslat.smartbrainlib.example.boilerplate;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.SkeletonRenderer;

public class SmartBrainLibClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(SBLExampleEntities.SBL_SKELETON, SkeletonRenderer::new);
	}
}
