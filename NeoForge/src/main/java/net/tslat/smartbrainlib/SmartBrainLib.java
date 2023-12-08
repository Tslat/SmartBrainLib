package net.tslat.smartbrainlib;

import net.neoforged.fml.common.Mod;

@Mod(SBLConstants.MOD_ID)
public class SmartBrainLib {
	public SmartBrainLib() {
		SBLConstants.SBL_LOADER.init();
	}
}
