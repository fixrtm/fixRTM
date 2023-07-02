/// Copyright (c) 2023 anatawa12 and other contributors
/// This file is part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

package com.anatawa12.fixRtm.rtm.block

import jp.ngt.rtm.RTMItem
import jp.ngt.rtm.block.tileentity.TileEntityDecoration
import jp.ngt.rtm.item.ItemDecoration
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

fun getPickBlock(world: World, pos: BlockPos): ItemStack {
    val tileEntity = world.getTileEntity(pos)
    if (tileEntity is TileEntityDecoration) {
        val itemStack = ItemStack(RTMItem.decoration_block)
        ItemDecoration.setModel(itemStack, tileEntity.modelName)
        return itemStack
    }
    return ItemStack.EMPTY
}
