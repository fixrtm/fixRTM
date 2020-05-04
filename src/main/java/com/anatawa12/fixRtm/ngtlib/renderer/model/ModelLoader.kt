@file:JvmName("ModelLoaderKt")

package com.anatawa12.fixRtm.ngtlib.renderer.model

import jp.ngt.ngtlib.io.FileType
import jp.ngt.ngtlib.io.NGTFileLoader
import jp.ngt.ngtlib.renderer.model.ModelFormatException
import jp.ngt.ngtlib.renderer.model.ModelLoader
import jp.ngt.ngtlib.renderer.model.PolygonModel
import jp.ngt.ngtlib.renderer.model.VecAccuracy
import net.minecraft.util.ResourceLocation
import org.apache.commons.codec.digest.DigestUtils
import java.io.IOException
import java.io.InputStream


fun loadModel(resource: ResourceLocation, par1: VecAccuracy, vararg args: Any?): PolygonModel? {
    val fileName = resource.toString()
    try {
        val digest = DigestUtils.sha1Hex("$fileName:$par1")

        CachedPolygonModel.getCachedModel(digest)?.let { return it }

        val model = ModelLoader.loadModel(inputStreams(resource), fileName, par1, *args)

        CachedPolygonModel.putCachedModel(digest, model)

        return model
    } catch (var10: IOException) {
        throw ModelFormatException("Failed to load model : $fileName", var10)
    }
}

private fun inputStreams(resource: ResourceLocation): Array<InputStream?> {
    val mainStream = NGTFileLoader.getInputStream(resource)
    if (FileType.OBJ.match(resource.path)) {
        val mtlFileName = resource.path.replace(".obj", ".mtl")
        val mtlFile = ResourceLocation(resource.namespace, mtlFileName)
        var is2: InputStream? = null
        try {
            is2 = NGTFileLoader.getInputStream(mtlFile)
        } catch (var9: IOException) {
        }
        return arrayOf(mainStream, is2)
    } else {
        return arrayOf(mainStream)
    }
}
