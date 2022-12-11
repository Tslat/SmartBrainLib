package net.tslat.smartbrainlib;

import net.fabricmc.api.ModInitializer;
import net.tslat.smartbrainlib.example.boilerplate.SBLExampleEntities;

public class SmartBrainLib implements ModInitializer {
	public static SBLExampleEntities MOBS;

	@Override
	public void onInitialize() {
		SBLConstants.SBL_LOADER.init();
	}
}
