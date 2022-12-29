package net.tslat.smartbrainlib.example.boilerplate;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.tslat.smartbrainlib.SBLConstants;
import net.tslat.smartbrainlib.SBLQuilt;

public class SBLClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		if (SBLConstants.SBL_LOADER.isDevEnv())
			EntityRendererRegistry.register(SBLQuilt.SBL_SKELETON, SkeletonRenderer::new);
	}
}
