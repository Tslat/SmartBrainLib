package net.tslat.smartbrainlib;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.tslat.smartbrainlib.example.boilerplate.SBLExampleCommon;

/// Main entrypoint for `SmartBrainLib` for `NeoForge`
@Mod(SBLConstants.MOD_ID)
public class SmartBrainLib {
	public static final DeferredRegister<MemoryModuleType<?>> MEMORY_TYPES = DeferredRegister.create(Registries.MEMORY_MODULE_TYPE, SBLConstants.MOD_ID);
	public static final DeferredRegister<SensorType<?>> SENSORS = DeferredRegister.create(Registries.SENSOR_TYPE, SBLConstants.MOD_ID);

	public SmartBrainLib(IEventBus modBus) {
		MEMORY_TYPES.register(modBus);
		SENSORS.register(modBus);

		SBLCommon.init();

		if (SBLConstants.PLATFORM.isDevEnv())
			SBLExampleCommon.init(modBus);
	}
}
