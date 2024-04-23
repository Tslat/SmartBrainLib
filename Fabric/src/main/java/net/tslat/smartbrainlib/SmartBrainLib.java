package net.tslat.smartbrainlib;

import net.fabricmc.api.ModInitializer;

public class SmartBrainLib implements ModInitializer {
	@Override
	public void onInitialize() {
		SBLConstants.SBL_LOADER.init(null);
	}
}
