/// Copyright (c) 2023 anatawa12 and other contributors
/// This file is part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

package com.anatawa12.fixRtm.rtm.block

import com.anatawa12.fixRtm.rtm.item.ItemWithModelEx
import jp.ngt.rtm.RTMItem
import jp.ngt.rtm.block.tileentity.TileEntityMachineBase
import jp.ngt.rtm.item.ItemInstalledObject
import net.minecraft.client.gui.GuiScreen
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World


object BlockMachineBase {
    @JvmStatic
    fun getPickBlock(world: World, pos: BlockPos): ItemStack {
        val tileEntity = world.getTileEntity(pos) as? TileEntityMachineBase ?: return ItemStack.EMPTY
        val type = ItemInstalledObject.IstlObjType.values().firstOrNull { tileEntity.subType == it.type }
                ?: return ItemStack.EMPTY

        val itemStack = ItemStack(RTMItem.installedObject)
        itemStack.itemDamage = type.id.toInt()
        (RTMItem.installedObject as ItemInstalledObject).setModelState(itemStack, tileEntity.resourceState)

        if (GuiScreen.isCtrlKeyDown()) {
            ItemWithModelEx.copyOffsetToItemStack(tileEntity, itemStack)
        }
        return itemStack
    }
}
