package net.tslat.smartbrainlib;

import com.google.common.base.Suppliers;
import net.minecraft.resources.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ServiceLoader;
import java.util.function.Supplier;

/// Static holder class for `SmartBrainLib`'s global constants
public final class SBLConstants {
	public static final String MOD_ID = "smartbrainlib";
	private static final Identifier BASE_ID = Identifier.fromNamespaceAndPath(MOD_ID, "");
	public static final Supplier<Logger> LOGGER = Suppliers.memoize(() -> LogManager.getLogger(MOD_ID));

	public static final SBLPlatform PLATFORM = ServiceLoader.load(SBLPlatform.class).findFirst().orElseThrow();

	/// Create an [Identifier] under `SmartBrainLib`'s [namespace][#MOD_ID]
	public static Identifier id(String path) {
		return BASE_ID.withPath(path);
	}
}