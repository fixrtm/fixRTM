package com.anatawa12.fixRtm.io

import net.minecraft.util.ResourceLocation
import java.io.File

interface FIXModelPack {
    val sha1Hash: String
    val file: File
    val domains: Set<String>
    fun getFile(location: ResourceLocation): FIXResource?
}
