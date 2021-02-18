package com.anatawa12.fixRtm.asm.preprocessing

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin

// after Patch, but first
@IFMLLoadingPlugin.SortingIndex(Int.MIN_VALUE + 2)
class PreprocessingFixRtmCorePlugin : IFMLLoadingPlugin {
    override fun getModContainerClass(): String? = null

    override fun getASMTransformerClass(): Array<String> = arrayOf(
        PreprocessorTransformer::class.qualifiedName!!,
    )

    override fun getSetupClass(): String? = null

    override fun injectData(p0: MutableMap<String, Any>?) {
    }

    override fun getAccessTransformerClass(): String? = null
}
