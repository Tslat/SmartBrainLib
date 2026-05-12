package net.tslat.smartbrainlib;

import net.fabricmc.api.ModInitializer;
import net.tslat.smartbrainlib.example.boilerplate.SBLExampleCommon;

/// Main entrypoint for `SmartBrainLib` for `Fabric`
public class SmartBrainLib implements ModInitializer {
	@Override
	public void onInitialize() {
		SBLCommon.init();

		if (SBLConstants.PLATFORM.isDevEnv())
			SBLExampleCommon.init();
	}
}
