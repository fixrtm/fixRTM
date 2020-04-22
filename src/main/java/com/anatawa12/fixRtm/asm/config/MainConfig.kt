package com.anatawa12.fixRtm.asm.config

import net.minecraftforge.fml.common.Loader

object MainConfig {
    private val configFile = Loader.instance().configDir.resolve("fix-rtn.cfg")
            .apply { appendText("") }
    private val cfg = KVSConfig().apply { configFile.reader().useLines { loadFile( it ) } }

    @JvmField
    val multiThreadModelConstructEnabled = cfg.enableDisableProp(
            "multi thread model construct",
            "constructs models using a thread with a number of logical cores",
            true)

}
