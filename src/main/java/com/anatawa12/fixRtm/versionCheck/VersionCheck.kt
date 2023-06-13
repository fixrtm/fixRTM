/// Copyright (c) 2023 anatawa12 and other contributors
/// This file is part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

@file:JvmName("VersionCheck")

package com.anatawa12.fixRtm.versionCheck

import jp.ngt.ngtlib.NGTCore
import jp.ngt.rtm.RTMCore
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.resources.I18n
import net.minecraftforge.fml.client.GuiErrorBase
import net.minecraftforge.fml.client.IDisplayableError
import net.minecraftforge.fml.common.FMLModContainer
import net.minecraftforge.fml.common.LoaderException
import net.minecraftforge.fml.common.event.FMLConstructionEvent

private val checkClasses = hashMapOf(RTMCore.MODID to "jp.ngt.rtm.RTMCore", NGTCore.MODID to "jp.ngt.ngtlib.NGTCore");

@Suppress("unused") // called by transformer
fun onConstructMod(container: FMLModContainer, event: FMLConstructionEvent) {
    val className = checkClasses[container.modId] ?: return
    try {
        Class.forName(className, false, event.modClassLoader)
    } catch (e: ClassNotFoundException) {
        // class load error should mean version mismatch
        throw RtmOrNgtLibVersionMismatchException(e)
    }
}

class RtmOrNgtLibVersionMismatchException(cause: Throwable?) : LoaderException(ERROR_MESSAGE, cause), IDisplayableError {
    override fun createGui(): GuiScreen = RtmOrNgtLibVersionMismatchErrorScreen()
}

val ERROR_MESSAGE = "RTM or NGTLib version mismatch detected!\n" +
        "This version of fixRTM requires RTM ${RTMCore.VERSION} and NGTLib ${NGTCore.VERSION}.\n" +
        "Use exact requested version of RTM and NGTLib."

class RtmOrNgtLibVersionMismatchErrorScreen : GuiErrorBase() {
    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawDefaultBackground()

        super.drawScreen(mouseX, mouseY, partialTicks)

        val key = "fix-rtm.launch-error.error.rtm-ngtlib-mismatch"

        val messageLines = if (I18n.hasKey(key)) {
            I18n.format(key, RTMCore.VERSION, NGTCore.VERSION).split("\\n")
        } else {
            ERROR_MESSAGE.lines()
        }

        var offset = (85 - messageLines.size * 10).coerceAtLeast(10)
        offset += 5
        for (message in messageLines) {
            offset += 10
            drawCenteredString(fontRenderer, message, width / 2, offset, 0xEEEEEE)
        }
    }
}

