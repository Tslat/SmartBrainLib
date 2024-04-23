package net.tslat.smartbrainlib;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(SBLConstants.MOD_ID)
public class SmartBrainLib {
	public SmartBrainLib(IEventBus modBus) {
		SBLConstants.SBL_LOADER.init(modBus);
	}
}
