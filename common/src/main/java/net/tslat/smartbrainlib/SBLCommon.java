package net.tslat.smartbrainlib;

import net.tslat.smartbrainlib.registry.SBLMemoryTypes;
import net.tslat.smartbrainlib.registry.SBLSensors;

/// Common-code general purpose class, typically used for mod setup
public final class SBLCommon {
    /// Initial entrypoint, called from the `SmartBrainLib`'s entrypoint at instantiation
    public static void init() {
        SBLMemoryTypes.init();
        SBLSensors.init();
    }
}
