/// Copyright (c) 2022 anatawa12 and other contributors
/// This file is part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

package com.anatawa12.fixRtm.rtm.block

import com.anatawa12.fixRtm.gui.GuiId
import com.anatawa12.fixRtm.openGui
import jp.ngt.ngtlib.block.BlockArgHolder
import jp.ngt.rtm.RTMCore
import jp.ngt.rtm.RTMItem
import jp.ngt.rtm.block.tileentity.TileEntityOrnament
import jp.ngt.rtm.item.ItemInstalledObject
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

object BlockOrnamentMain {
    @JvmStatic
    fun onBlockActivated(holder: BlockArgHolder): Boolean {
        if (holder.player.inventory.getCurrentItem().item == RTMItem.crowbar) {
            if (holder.world.isRemote) holder.player.openGui(GuiId.ChangeOffset, holder.player.world, holder.blockPos)
            return true
        }
        if (holder.player.isSneaking) {
            if (holder.world.isRemote)
                holder.player.openGui(RTMCore.instance, RTMCore.guiIdSelectTileEntityModel.toInt(),
                    holder.player.world, holder.blockPos.x, holder.blockPos.y, holder.blockPos.z)
            return true
        }
        return false
    }

    @JvmStatic
    fun getPickBlock(world: World, pos: BlockPos, type: ItemInstalledObject.IstlObjType): ItemStack {
        val tileEntity = world.getTileEntity(pos)
        if (tileEntity is TileEntityOrnament) {
            val itemStack = ItemStack(RTMItem.installedObject)
            itemStack.itemDamage = type.id.toInt()
            (RTMItem.installedObject as ItemInstalledObject).setModelState(itemStack, tileEntity.resourceState)
            return itemStack
        }
        return ItemStack.EMPTY
    }
}
