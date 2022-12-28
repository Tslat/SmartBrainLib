package net.tslat.smartbrainlib.example.boilerplate;

import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.tslat.smartbrainlib.SBLConstants;
import net.tslat.smartbrainlib.SBLQuilt;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;

public class SBLClient implements ClientModInitializer {
	@Override
	public void onInitializeClient(ModContainer mod) {
		if (SBLConstants.SBL_LOADER.isDevEnv())
			EntityRendererRegistry.register(SBLQuilt.SBL_SKELETON, SkeletonRenderer::new);
	}
}
