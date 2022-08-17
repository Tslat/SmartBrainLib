package net.tslat.smartbrainlib;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.tslat.smartbrainlib.example.boilerplate.SBLExampleEntities;

public class SmartBrainLib implements ModInitializer {
	public static SBLExampleEntities MOBS;
	public static final String VERSION = "1.0";
	public static final String MOD_ID = "smartbrainlib";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
			MOBS = new SBLExampleEntities();
			SBLExampleEntities.initStats();
		}
	}
}
