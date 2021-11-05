/// Copyright (c) 2021 anatawa12 and other contributors
/// This file is part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

package com.anatawa12.fixRtm.asm.patching

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin

// after sorting, but first
@IFMLLoadingPlugin.SortingIndex(Int.MIN_VALUE + 1)
class PatchingFixRtmCorePlugin : IFMLLoadingPlugin {
    override fun getModContainerClass(): String? = null

    override fun getASMTransformerClass(): Array<String> = arrayOf(
        PatchApplier::class.qualifiedName!!,
    )

    override fun getSetupClass(): String? = null

    override fun injectData(p0: MutableMap<String, Any>?) {
    }

    override fun getAccessTransformerClass(): String? = null
}
