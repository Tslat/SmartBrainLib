package net.tslat.smartbrainlib;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.tslat.smartbrainlib.example.boilerplate.SBLExampleEntities;
import net.tslat.smartbrainlib.registry.SBLMemoryTypes;
import net.tslat.smartbrainlib.registry.SBLSensors;

public class SmartBrainLib implements ModInitializer {
	public static SBLExampleEntities MOBS;
	public static final String VERSION = "1.4";
	public static final String MOD_ID = "smartbrainlib";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		SBLMemoryTypes.init();
		SBLSensors.init();

		if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
			MOBS = new SBLExampleEntities();
			SBLExampleEntities.initStats();
		}
	}
}
