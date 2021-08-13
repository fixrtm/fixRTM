/// Copyright (c) 2021 anatawa12 and other contributors
/// This file is/was part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

package com.anatawa12.fixRtm.asm

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin

// the core plugin only for TransformerExclusions
@IFMLLoadingPlugin.TransformerExclusions(
    "kotlin.",
    "kotlinx.",
    "io.sigpipe.jbsdiff.",
    "org.intellij.lang.annotations.",
    "org.jetbrains.annotations.",
    "org.apache.commons.compress.",
    "org.tukaani.xz.",
)
class FixRtmDevEnvironmentOnlyCorePlugin : IFMLLoadingPlugin {
    override fun getModContainerClass(): String? = null

    override fun getASMTransformerClass(): Array<String> = arrayOf()

    override fun getSetupClass(): String? = null

    override fun injectData(p0: MutableMap<String, Any>?) {
    }

    override fun getAccessTransformerClass(): String? = null
}
