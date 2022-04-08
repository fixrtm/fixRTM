package com.anatawa12.fixRtm.rtm.item

import jp.ngt.ngtlib.block.TileEntityPlaceable
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound


object ItemWithModelEx {
    @JvmStatic
    fun hasOffset(itemStack: ItemStack): Boolean {
        return itemStack.hasTagCompound() && itemStack.tagCompound!!.hasKey("yaw")
    }

    private fun getOffset(itemStack: ItemStack): FloatArray = itemStack.tagCompound?.let { nbt ->
        val offsetX = nbt.getFloat("offsetX")
        val offsetY = nbt.getFloat("offsetY")
        val offsetZ = nbt.getFloat("offsetZ")
        floatArrayOf(offsetX, offsetY, offsetZ)
    } ?: FloatArray(3)

    private fun getRotation(itemStack: ItemStack): Float {
        return itemStack.tagCompound?.getFloat("yaw") ?: 0f
    }

    private fun setOffset(itemStack: ItemStack, offsetX: Float, offsetY: Float, offsetZ: Float) {
        if (!itemStack.hasTagCompound()) {
            itemStack.tagCompound = NBTTagCompound()
        }
        val nbt = itemStack.tagCompound!!
        nbt.setFloat("offsetX", offsetX)
        nbt.setFloat("offsetY", offsetY)
        nbt.setFloat("offsetZ", offsetZ)
    }

    private fun setRotation(itemStack: ItemStack, rotation: Float) {
        if (!itemStack.hasTagCompound()) {
            itemStack.tagCompound = NBTTagCompound()
        }
        val nbt = itemStack.tagCompound!!
        nbt.setFloat("yaw", rotation)
    }

    @JvmStatic
    fun copyOffsetToItemStack(tileEntity: TileEntityPlaceable, itemStack: ItemStack) {
        setOffset(itemStack, tileEntity.offsetX, tileEntity.offsetY, tileEntity.offsetZ)
        setRotation(itemStack, tileEntity.rotation)
    }

    @JvmStatic
    fun applyOffsetToTileEntity(itemStack: ItemStack, tile: TileEntityPlaceable) {
        val offset: FloatArray = getOffset(itemStack)
        tile.setOffset(offset[0], offset[1], offset[2], true)
        tile.setRotation(getRotation(itemStack), true)
    }
}
