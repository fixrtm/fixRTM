package com.anatawa12.fixRtm

import com.anatawa12.fixRtm.Loggers.getLogger
import net.minecraftforge.fml.relauncher.FMLLaunchHandler

object Deprecation {
    private val LOGGER = getLogger("Deprecation")
    private val isDebugEnv = FMLLaunchHandler.isDeobfuscatedEnvironment()

    @JvmStatic
    fun found(name: String) {
        if (isDebugEnv) {
            throw DeprecationException(name)
        } else {
            LOGGER.warn("Deprecated function called", DeprecationException(name))
        }
    }


    class DeprecationException(val name: String) : RuntimeException("Deprecated function called!: $name")
}
