/// Copyright (c) 2021 anatawa12 and other contributors
/// This file is/was part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

package com.anatawa12.fixRtm.gui

import com.anatawa12.fixRtm.gui.GuiId.ChangeOffset
import com.anatawa12.fixRtm.gui.GuiId.values
import jp.ngt.ngtlib.block.TileEntityPlaceable
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.fml.common.network.IGuiHandler

class GuiHandler : IGuiHandler {
    override fun getServerGuiElement(ID: Int, player: EntityPlayer?, world: World?, x: Int, y: Int, z: Int): Any? {
        return when (values()[ID]) {
            ChangeOffset -> null
        }
    }

    override fun getClientGuiElement(ID: Int, player: EntityPlayer?, world: World?, x: Int, y: Int, z: Int): Any? {
        return when (values()[ID]) {
            ChangeOffset -> GuiChangeOffset(world!!.getTileEntity(BlockPos(x, y, z)) as TileEntityPlaceable)
        }
    }
}
