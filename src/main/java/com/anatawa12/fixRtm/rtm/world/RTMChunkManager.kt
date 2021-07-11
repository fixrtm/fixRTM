/// Copyright (c) 2020 anatawa12 and other contributors
/// This file is/was part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

@file:JvmName("RTMChunkManagerKt")

package com.anatawa12.fixRtm.rtm.world

import net.minecraft.util.math.ChunkPos
import kotlin.math.abs
import kotlin.math.max


fun getChunksAround(set: MutableSet<ChunkPos>, xChunk: Int, zChunk: Int, radius: Int) {
    set.clear()
    val list = mutableListOf<ChunkPos>()

    for (xx in xChunk - radius..xChunk + radius) {
        for (zz in zChunk - radius..zChunk + radius) {
            list.add(ChunkPos(xx, zz))
        }
    }

    list.sortByDescending { max(abs(it.x - xChunk), abs(it.z - zChunk)) }

    set.addAll(list)

}
