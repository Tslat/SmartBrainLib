package net.tslat.smartbrainlib.example.boilerplate;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.tslat.smartbrainlib.SBLConstants;
import net.tslat.smartbrainlib.SBLFabric;

public class SBLClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		if (SBLConstants.SBL_LOADER.isDevEnv())
			EntityRendererRegistry.INSTANCE.register(SBLFabric.SBL_SKELETON, (dispatcher, context) -> new SkeletonRenderer(dispatcher));
	}
}
