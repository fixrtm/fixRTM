package com.anatawa12.fixRtm.asm

import net.minecraft.launchwrapper.Launch
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin


@IFMLLoadingPlugin.TransformerExclusions(
        "kotlin",
        "com.anatawa12.fixRtm.asm",
        "org.jetbrains.annotations",
        "org.intellij.lang.annotations",
        "io.sigpipe",
        "org.apache.commons.compress"
)
class FixRtmCorePlugin : IFMLLoadingPlugin {
    init {
        Launch.classLoader.registerTransformer("com.anatawa12.fixRtm.asm.PatchApplier")
    }

    override fun getModContainerClass(): String? = null

    override fun getASMTransformerClass(): Array<String>? = arrayOf()

    override fun getSetupClass(): String? = null

    override fun injectData(p0: MutableMap<String, Any>?){
    }

    override fun getAccessTransformerClass(): String? = null
}
