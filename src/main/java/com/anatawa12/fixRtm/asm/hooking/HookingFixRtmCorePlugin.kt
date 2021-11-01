/// Copyright (c) 2021 anatawa12 and other contributors
/// This file is/was part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

package com.anatawa12.fixRtm.asm.hooking

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin

@IFMLLoadingPlugin.SortingIndex(2000)
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
