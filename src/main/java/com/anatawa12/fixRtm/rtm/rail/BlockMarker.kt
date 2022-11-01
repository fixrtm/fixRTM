/// Copyright (c) 2022 anatawa12 and other contributors
/// This file is part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

package com.anatawa12.fixRtm.rtm.rail

import jp.ngt.ngtlib.block.BlockUtil
import jp.ngt.rtm.rail.BlockMarker
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import kotlin.math.floor

// Block metadata:
// 0b0100: is diagonal flag
// 0b0011: direction id
object BlockMarker {
    const val MERGED_DAMAGE = 8

    @Suppress("UNUSED_PARAMETER")
    @JvmStatic
    fun onBlockPlacedBy(
        block: BlockMarker,
        world: World,
        pos: BlockPos,
        state: IBlockState,
        placer: EntityLivingBase,
        stack: ItemStack,
    ) {
        BlockUtil.setBlock(world, pos, block, getFaceMeta(placer), 2)
    }

    // NGTMath.floor(NGTMath.normalizeAngle((double)placer.rotationYaw + 180.0D) / 90.0D) & 3
    private val FACE_MAPPING = intArrayOf(
        2, 6, 3, 7, 0, 4, 1, 5
    )
    private fun getFaceMeta(placer: EntityLivingBase): Int {
        val angle = floor(placer.rotationYaw / 45.0 + 0.5).toInt() and 7
        return FACE_MAPPING[angle]
    }
}
