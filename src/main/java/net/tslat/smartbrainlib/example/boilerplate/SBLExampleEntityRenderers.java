package net.tslat.smartbrainlib.example.boilerplate;

import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraftforge.client.event.EntityRenderersEvent;

final class SBLExampleEntityRenderers {
	static void registerEntityRenderers(final EntityRenderersEvent.RegisterRenderers ev) {
		ev.registerEntityRenderer(SBLExampleEntities.BRAIN_BASED_ZOMBIE.get(), ZombieRenderer::new);
	}
}
