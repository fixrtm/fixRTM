package com.anatawa12.fixRtm.asm

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin

@IFMLLoadingPlugin.TransformerExclusions(
    "com.anatawa12.fixRtm.asm.",
    "com.anatawa12.fixRtm.libs.",
)
class FixRtmCorePlugin : IFMLLoadingPlugin {
    override fun getModContainerClass(): String? = null

    override fun getASMTransformerClass(): Array<String> = arrayOf()

    override fun getSetupClass(): String? = null

    override fun injectData(p0: MutableMap<String, Any>?) {
    }

    override fun getAccessTransformerClass(): String? = null
}
