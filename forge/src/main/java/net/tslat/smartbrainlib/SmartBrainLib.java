package net.tslat.smartbrainlib;

import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.tslat.smartbrainlib.example.boilerplate.SBLExampleCommon;

/// Main entrypoint for `SmartBrainLib` for `Forge`
@Mod(SBLConstants.MOD_ID)
public class SmartBrainLib {
	public static final DeferredRegister<MemoryModuleType<?>> MEMORY_TYPES = DeferredRegister.create(ForgeRegistries.Keys.MEMORY_MODULE_TYPES, SBLConstants.MOD_ID);
	public static final DeferredRegister<SensorType<?>> SENSORS = DeferredRegister.create(ForgeRegistries.Keys.SENSOR_TYPES, SBLConstants.MOD_ID);

	public SmartBrainLib(FMLJavaModLoadingContext context) {
		final BusGroup busGroup = context.getModBusGroup();

		MEMORY_TYPES.register(busGroup);
		SENSORS.register(busGroup);

		SBLCommon.init();

		if (SBLConstants.PLATFORM.isDevEnv())
			SBLExampleCommon.init(busGroup);
	}
}
