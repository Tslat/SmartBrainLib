package net.tslat.smartbrainlib.example.boilerplate;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.tslat.smartbrainlib.SBLConstants;

/// Client-code boilerplate class for example implementations for `SmartBrainLib`
public class SBLExampleClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		if (SBLConstants.PLATFORM.isDevEnv())
			EntityRendererRegistry.register(SBLExampleCommon.SKELETON, SkeletonRenderer::new);
	}
}
