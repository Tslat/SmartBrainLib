package net.tslat.smartbrainlib;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(SBLConstants.MOD_ID)
public class SmartBrainLib {
	public SmartBrainLib(FMLJavaModLoadingContext context) {
		SBLConstants.SBL_LOADER.init(context.getModBusGroup());
	}
}
