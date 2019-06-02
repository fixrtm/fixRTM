package com.anatawa12.fixRtm.asm

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin


@IFMLLoadingPlugin.TransformerExclusions("kotlin", "com.anatawa12.fixRtm.asm", "org.jetbrains.annotations", "org.intellij.lang.annotations")
class FixRtmCorePlugin : IFMLLoadingPlugin {
    override fun getModContainerClass(): String? = null

    override fun getASMTransformerClass(): Array<String>? =
            arrayOf("com.anatawa12.fixRtm.asm.NPEInGetResourceSetTransform"
                    , "com.anatawa12.fixRtm.asm.NPEInTickProcessQueueTransformer"
                    , "com.anatawa12.fixRtm.asm.DummyModelSetTransform"
                    //, "com.anatawa12.fixRtm.asm.ConstructDummisTransformer"
            )

    override fun getSetupClass(): String? = null

    override fun injectData(p0: MutableMap<String, Any>?){
    }

    override fun getAccessTransformerClass(): String? = null
}