/// Copyright (c) 2021 anatawa12 and other contributors
/// This file is part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

package com.anatawa12.fixRtm.gui

import com.anatawa12.fixRtm.KeyboardUtil
import jp.ngt.ngtlib.block.TileEntityPlaceable
import jp.ngt.ngtlib.gui.GuiScreenCustom
import jp.ngt.ngtlib.gui.GuiTextFieldCustom
import jp.ngt.ngtlib.network.PacketNBT
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.resources.I18n
import org.lwjgl.input.Keyboard

class GuiChangeOffset(private var tileEntity: TileEntityPlaceable) : GuiScreenCustom() {
    private lateinit var fieldOffsetX: GuiTextFieldCustom
    private lateinit var fieldOffsetY: GuiTextFieldCustom
    private lateinit var fieldOffsetZ: GuiTextFieldCustom
    private lateinit var fieldRotationYaw: GuiTextFieldCustom

    override fun initGui() {
        super.initGui()
        buttonList.clear()
        buttonList.add(GuiButton(0, width / 2 - 105, height - 28, 100, 20, I18n.format("gui.done")))
        buttonList.add(GuiButton(1, width / 2 + 5, height - 28, 100, 20, I18n.format("gui.cancel")))
        fieldOffsetX = setTextField(width - 70, 20, 60, 20, tileEntity.offsetX.toString())
        fieldOffsetY = setTextField(width - 70, 50, 60, 20, tileEntity.offsetY.toString())
        fieldOffsetZ = setTextField(width - 70, 80, 60, 20, tileEntity.offsetZ.toString())
        fieldRotationYaw = setTextField(width - 70, 110, 60, 20, tileEntity.rotation.toString())
    }

    override fun drawScreen(par1: Int, par2: Int, par3: Float) {
        drawDefaultBackground()
        super.drawScreen(par1, par2, par3)
        drawCenteredString(this.fontRenderer, "Offset X", width - 70, 10, 0xFFFFFF)
        drawCenteredString(this.fontRenderer, "Offset Y", width - 70, 40, 0xFFFFFF)
        drawCenteredString(this.fontRenderer, "Offset Z", width - 70, 70, 0xFFFFFF)
        drawCenteredString(this.fontRenderer, "Rotation Yaw", width - 70, 100, 0xFFFFFF)
    }

    override fun actionPerformed(button: GuiButton) {
        if (button.id == 0) {
            mc.displayGuiScreen(null)
            sendPacket()
        } else if (button.id == 1) {
            mc.displayGuiScreen(null)
        }
        super.actionPerformed(button)
    }

    override fun keyTyped(par1: Char, par2: Int) {
        if (par2 == Keyboard.KEY_ESCAPE || par2 == mc.gameSettings.keyBindInventory.keyCode) {
            mc.player.closeScreen()
        }
        if (currentTextField != null) {
            if (KeyboardUtil.isDecimalNumberKey(par2)) {
                currentTextField.textboxKeyTyped(par1, par2)
            }
        }
    }

    private fun updateValues() {
        val offsetX: Float = fieldOffsetX.text.toFloatOrNull() ?: tileEntity.offsetX
        val offsetY: Float = fieldOffsetY.text.toFloatOrNull() ?: tileEntity.offsetY
        val offsetZ: Float = fieldOffsetZ.text.toFloatOrNull() ?: tileEntity.offsetZ
        val rotation: Float = fieldRotationYaw.text.toFloatOrNull() ?: tileEntity.rotation
        tileEntity.setOffset(offsetX, offsetY, offsetZ, false)
        tileEntity.setRotation(rotation, false)
    }

    private fun sendPacket() {
        updateValues()
        PacketNBT.sendToServer(tileEntity)
    }
}
