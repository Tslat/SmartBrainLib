package net.tslat.smartbrainlib;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.tslat.smartbrainlib.example.boilerplate.SBLExampleEntities;
import net.tslat.smartbrainlib.registry.SBLSensors;
import net.tslat.smartbrainlib.registry.SBLMemoryTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static net.tslat.smartbrainlib.SmartBrainLib.MOD_ID;

@Mod(MOD_ID)
public class SmartBrainLib {
	public static final String VERSION = "1.5";
	public static final String MOD_ID = "smartbrainlib";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	public SmartBrainLib() {
		setupRegistries(FMLJavaModLoadingContext.get().getModEventBus());
	}

	private void setupRegistries(IEventBus modEventBus) {
		SBLSensors.SENSORS.register(modEventBus);
		SBLMemoryTypes.MEMORY_TYPES.register(modEventBus);

		if (!FMLLoader.isProduction())
			SBLExampleEntities.init(modEventBus);
	}
}
