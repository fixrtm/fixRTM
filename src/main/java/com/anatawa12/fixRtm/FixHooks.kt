@file:JvmName("FixHooks")

package com.anatawa12.fixRtm

import jp.ngt.rtm.modelpack.ModelPackManager
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.fml.common.StartupQuery

@Suppress("unused") // called by transformer
fun onFMLReadData() {
    if (FMLCommonHandler.instance().side.isClient) {
        if (!ModelPackManager.INSTANCE.modelLoaded) {
            if (!StartupQuery.confirm("""
                It's very RECOMMENDED not to load world data before finished loading model packs.
                Do you want to continue loading world?
            """.trimIndent()))
                StartupQuery.abort()
        }
    }
}
