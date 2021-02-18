package com.anatawa12.fixRtm.asm.hooking

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin


class HookingFixRtmCorePlugin : IFMLLoadingPlugin {
    override fun getASMTransformerClass(): Array<String> = arrayOf(
        HookingTransformer::class.qualifiedName!!,
    )

    override fun getModContainerClass(): String? = null

    override fun getSetupClass(): String? = null

    override fun injectData(data: MutableMap<String, Any>?) {
    }

    override fun getAccessTransformerClass(): String? = null
}
