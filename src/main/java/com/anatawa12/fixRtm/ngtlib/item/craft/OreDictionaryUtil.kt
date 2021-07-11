/// Copyright (c) 2020 anatawa12 and other contributors
/// This file is/was part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

package com.anatawa12.fixRtm.ngtlib.item.craft

import net.minecraft.item.ItemStack
import net.minecraftforge.oredict.OreDictionary


object OreDictionaryUtil {
    @JvmStatic
    fun getOreIDs(stack: ItemStack): IntArray {
        return if (stack.isEmpty) intArrayOf() else OreDictionary.getOreIDs(stack)
    }
}
