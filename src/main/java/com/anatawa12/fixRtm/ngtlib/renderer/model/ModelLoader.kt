/// Copyright (c) 2020 anatawa12 and other contributors
/// This file is part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

@file:JvmName("ModelLoaderKt")

package com.anatawa12.fixRtm.ngtlib.renderer.model

import com.anatawa12.fixRtm.asm.config.MainConfig
import com.anatawa12.fixRtm.io.FIXFileLoader
import com.anatawa12.fixRtm.io.FIXModelPack
import jp.ngt.ngtlib.io.FileType
import jp.ngt.ngtlib.renderer.model.ModelFormatException
import jp.ngt.ngtlib.renderer.model.ModelLoader
import jp.ngt.ngtlib.renderer.model.PolygonModel
import jp.ngt.ngtlib.renderer.model.VecAccuracy
import net.minecraft.util.ResourceLocation
import java.io.IOException
import java.io.InputStream


fun loadModel(resource: ResourceLocation, par1: VecAccuracy, vararg args: Any?): PolygonModel? {
    if (!MainConfig.cachedPolygonModel)
        return ModelLoader.loadModel__NGTLIB(resource, par1, *args)
    val fileName = resource.toString()
    try {
        val (pack, streams) = inputStreams(resource)

        CachedPolygonModel.getCachedModel(pack, resource, par1)?.let { return it }

        val model = ModelLoader.loadModel(streams, fileName, par1, *args)

        CachedPolygonModel.putCachedModel(pack, resource, par1, model)

        return model
    } catch (var10: IOException) {
        throw ModelFormatException("Failed to load model : $fileName", var10)
    }
}

private fun inputStreams(resource: ResourceLocation): Pair<FIXModelPack, Array<InputStream?>> {
    val mainResource = FIXFileLoader.getResource(resource)
    val mainStream: InputStream? = mainResource.inputStream
    if (FileType.OBJ.match(resource.path)) {
        val mtlFileName = resource.path.replace(".obj", ".mtl")
        val mtlFile = ResourceLocation(resource.namespace, mtlFileName)
        var is2: InputStream? = null
        try {
            is2 = FIXFileLoader.getInputStream(mtlFile)
        } catch (var9: IOException) {
        }
        return mainResource.pack to arrayOf(mainStream, is2)
    } else {
        return mainResource.pack to arrayOf(mainStream)
    }
}
