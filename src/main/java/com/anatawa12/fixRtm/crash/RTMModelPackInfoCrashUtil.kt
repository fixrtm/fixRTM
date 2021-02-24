package com.anatawa12.fixRtm.crash

import com.anatawa12.fixRtm.rtm.modelpack.ModelState
import com.anatawa12.fixRtm.rtm.modelpack.modelset.dummies.IFixRTMDummyModelSet
import jp.ngt.ngtlib.io.NGTFileLoader
import jp.ngt.rtm.modelpack.ModelPackManager
import jp.ngt.rtm.modelpack.modelset.ResourceSet

private val cwd = java.io.File(".").absoluteFile

private fun resourceSourcePackPath(model: ResourceSet<*>): String {
    if (model.isDummy) return "dummy-model"
    if (model is IFixRTMDummyModelSet) return "fixrtm-dummy-model"
    val file = model.config.file?.relativeToOrSelf(cwd) ?: return "no-source"
    val path = file.path
    val basePath = run basePath@{
        val suffix = NGTFileLoader.getArchiveSuffix(path)
        if (suffix.isNotEmpty()) return@basePath NGTFileLoader.getArchivePath(path, suffix)

        // after mods
        val firstSlash = path.indexOf('/')
        // after asset name
        val secondSlash = path.indexOf('/', firstSlash + 1)

        path.indexOf("/assets/", secondSlash).takeUnless { it == -1 }?.also { index ->
            // /mods/**/<modelname>/assets/**/*
            // -> /mods/**/<modelname>
            return@basePath path.substring(0, index)
        }
        path.indexOf("/mod", secondSlash).takeUnless { it == -1 }?.also { index ->
            // /mods/**/<modelname>/mods?/**/*
            // -> /mods/**/<modelname>
            return@basePath path.substring(0, index)
        }
        return@basePath file.parent
    }
    return basePath.removePrefix("mods/")
}

private fun resourceSourceFile(model: ResourceSet<*>): String {
    if (model.isDummy) return "dummy-model"
    if (model is IFixRTMDummyModelSet) return "fixrtm-dummy-model"
    val file = model.config.file?.relativeToOrSelf(cwd) ?: return "no-source"
    return file.name
}

private fun simplifyModelId(name: String): String = name
    .removePrefix("textures/rrs/")
    .removePrefix("textures/signboard/")
    .removePrefix("textures/flag/")

class ResourceSetInfo(set: ResourceSet<*>, val isIncludedInSMP: Boolean) {
    val state: ModelState = set.state
    val id = simplifyModelId(set.config.name)
    val sourcePackPath = resourceSourcePackPath(set)
    val sourceFile = resourceSourceFile(set)
}

fun collectAllModels(checkSMP: Boolean): Sequence<ResourceSetInfo> =
    ModelPackManager.INSTANCE.allModelSetMap.asSequence()
        .flatMap { (type, allModels) ->
            if (checkSMP) {
                val smpMap = ModelPackManager.INSTANCE.smpModelSetMap.getValue(type)
                allModels.values.asSequence()
                    .map { ResourceSetInfo(it, smpMap.containsKey(it.config.name)) }
            } else {
                allModels.values.asSequence().map { ResourceSetInfo(it, false) }
            }
        }
