package net.tslat.smartbrainlib;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ServiceLoader;

public class SBLConstants {
	public static final String VERSION = "1.16.4";
	public static final String MOD_ID = "smartbrainlib";
	public static final String MOD_NAME = "SmartBrainLib";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	public static final SBLLoader SBL_LOADER = ServiceLoader.load(SBLLoader.class).findFirst().get();
}