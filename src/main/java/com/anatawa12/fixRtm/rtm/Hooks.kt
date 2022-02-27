/// Copyright (c) 2020 anatawa12 and other contributors
/// This file is part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

package com.anatawa12.fixRtm.rtm

import jp.ngt.ngtlib.io.NGTLog
import jp.ngt.ngtlib.renderer.model.ModelFormatException
import jp.ngt.rtm.modelpack.ModelPackManager
import jp.ngt.rtm.modelpack.ResourceType
import jp.ngt.rtm.modelpack.modelset.ResourceSet
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.text.TextComponentTranslation

@Suppress("unused") // Used with Transform
fun eraseNullForModelSet(inSet: ResourceSet<*>?, type: ResourceType<*, *>): ResourceSet<*> {
    if (inSet != null) return inSet
    if (type.hasSubType) {
        return ModelPackManager.INSTANCE.dummyMap[type.name]
            ?: (ModelPackManager.INSTANCE.dummyMap[type.subType])
            ?: error("ResourceType(${type.name}) and ResourceType(${type.subType}) don't have dummy ResourceSet")
    } else {
        return ModelPackManager.INSTANCE.dummyMap[type.name]
            ?: error("ResourceType(${type.name}) don't have dummyMap")
    }
}

@Suppress("unused") // Used with Transform
fun <K, V> MovingSoundMaker_loadSoundJson_nullCheck(map: Map<K, V>?, domain: String): Map<K, V> {
    if (map == null)
        throw ModelFormatException("sound.json for $domain is invalid.")
    return map
}
