package net.tslat.smartbrainlib;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;

import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.tslat.smartbrainlib.example.boilerplate.SBLExampleEntities;

public class SmartBrainLibClient implements ClientModInitializer {

	@Override
	public void onInitializeClient(ModContainer mod) {
		EntityRendererRegistry.register(SBLExampleEntities.SBL_SKELETON, SkeletonRenderer::new);
	}
}
