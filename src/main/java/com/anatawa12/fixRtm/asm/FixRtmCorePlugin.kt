/// Copyright (c) 2019 anatawa12 and other contributors
/// This file is part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

package com.anatawa12.fixRtm.asm

import jp.ngt.ngtlib.NGTCore
import jp.ngt.rtm.RTMCore
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin
import java.awt.Graphics
import java.awt.GraphicsEnvironment
import javax.swing.JOptionPane

@IFMLLoadingPlugin.TransformerExclusions(
    "com.anatawa12.fixRtm.asm.",
    "com.anatawa12.fixRtm.libs.",
)
// late as possible
@IFMLLoadingPlugin.SortingIndex(Int.MAX_VALUE - 1)
class FixRtmCorePlugin : IFMLLoadingPlugin {
    override fun getModContainerClass(): String? = null

    override fun getASMTransformerClass(): Array<String> = arrayOf()

    override fun getSetupClass(): String? = null

    override fun injectData(p0: MutableMap<String, Any>?) {
        try {
            // check for class load error
            Class.forName("jp.ngt.rtm.RTMCore", false, FixRtmCorePlugin::class.java.classLoader)
            Class.forName("jp.ngt.ngtlib.NGTCore", false, FixRtmCorePlugin::class.java.classLoader)
        } catch (e: ClassNotFoundException) {
            // this should mean NGTLib or RTM version mismatch.
            if (!GraphicsEnvironment.isHeadless()) {
                JOptionPane.showMessageDialog(null,
                        arrayOf(
                                "RTM or NGTLib version mismatch detected!\n" +
                                        "This version of fixRTM requires RTM ${RTMCore.VERSION} and NGTLib ${NGTCore.VERSION}.\n" +
                                        "Use exact requested version of RTM and NGTLib.",
                                "RTMかNGTLibのバージョンの不一致を検出しました！\n" +
                                        "このバージョンのfixRTMはRTM ${RTMCore.VERSION}とNGTLib ${NGTCore.VERSION}を要求します。\n" +
                                        "正確に要求されたバージョンのRTMとNGTLibを使用してください。"
                        ),
                        "fixRTM",
                        JOptionPane.ERROR_MESSAGE)
            }
            throw Exception("RTM or NGTLib Version Mismatch", e)
        }
    }

    override fun getAccessTransformerClass(): String? = null
}
