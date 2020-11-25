package com.anatawa12.fixRtm.ngtlib.item.craft

import net.minecraft.item.ItemStack
import net.minecraftforge.oredict.OreDictionary


object OreDictionaryUtil {
    @JvmStatic
    fun getOreIDs(stack: ItemStack): IntArray {
        return if (stack.isEmpty) intArrayOf() else OreDictionary.getOreIDs(stack)
    }
}
