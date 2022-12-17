package net.tslat.smartbrainlib;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;

public class SmartBrainLib implements ModInitializer {
	@Override
	public void onInitialize(ModContainer mod) {
		SBLConstants.SBL_LOADER.init();
	}
}
