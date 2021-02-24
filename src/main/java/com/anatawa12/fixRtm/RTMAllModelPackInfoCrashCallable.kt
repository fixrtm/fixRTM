package com.anatawa12.fixRtm

import com.anatawa12.fixRtm.rtm.modelpack.modelset.dummies.IFixRTMDummyModelSet
import com.anatawa12.fixRtm.utils.Base64OutputStream
import com.anatawa12.fixRtm.utils.ToStringOutputStream
import com.google.gson.GsonBuilder
import jp.ngt.ngtlib.io.NGTFileLoader
import jp.ngt.ngtlib.util.NGTUtil
import jp.ngt.rtm.modelpack.ModelPackManager
import jp.ngt.rtm.modelpack.modelset.ResourceSet
import net.minecraftforge.fml.common.ICrashCallable
import java.util.zip.GZIPOutputStream
import kotlin.concurrent.withLock

object RTMAllModelPackInfoCrashCallable : ICrashCallable {
    override fun call(): String = ModelPackManager.INSTANCE.modelSetMapLock.readLock().withLock {
        buildString {
            append("Initialized ")
                .append(ModelPackManager.INSTANCE.allModelSetMap.values.sumBy { it.size })
                .append(" models")
            if (NGTUtil.isSMP()) {
                append(", Using ")
                    .append(ModelPackManager.INSTANCE.smpModelSetMap.values.sumBy { it.size })
                    .append(" models")
            }
            append("\n" +
                    "\tThe data below included the data about all models. the data is pom-like(but returned per 128 chars)\n" +
                    "\tand body is gzip-ed json. If you want not to include this data to crash-report, \n" +
                    "\tyou can disable from 'better_rtm.addModelPackInformationInAllCrashReports' in config/fix-rtm.cfg\n" +
                    "\tThis data can be decoded at https://fixrtm.github.io/crash_report_model_info_parser.html\n" +
                    "\tIf you want to know which file the model is included in, You can analyze with it.\n" +
                    "\n" +
                    "-----BEGIN REAL TRAIN MOD MODEL PACK INFORMATION-----\n")

            val map = printModelStates()
            val json = gson.toJson(listOf(1, map))
            ToStringOutputStream(this)
                .let { Base64OutputStream(it, addPadding = true, addNewLinePer64 = true, addNewLineAtEnd = true) }
                .let { GZIPOutputStream(it) }
                .writer()
                .use { it.write(json) }
            append("-----END REAL TRAIN MOD MODEL PACK INFORMATION-----\n")
        }
    }

    override fun getLabel(): String = "RTM Model Status"

    private val gson = GsonBuilder().create()
    private val cwd = java.io.File(".").absoluteFile

    private fun resourceSourcePackPath(model: ResourceSet<*>): String {
        if (model.isDummy) return "dummy-model"
        if (model is IFixRTMDummyModelSet) return "fixrtm-dummy-model"
        val file = model.config.file?.relativeToOrSelf(cwd) ?: return "no-source"
        val path = file.path
        val basePath = kotlin.run basePath@{
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

    private class ResourceSetInfo(set: ResourceSet<*>, val isIncludedInSMP: Boolean) {
        val state = set.state
        val id = simplifyModelId(set.config.name)
        val sourcePackPath = resourceSourcePackPath(set)
        val sourceFile = resourceSourceFile(set)
    }

    private fun collectAllModels(checkSMP: Boolean): Sequence<ResourceSetInfo> =
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

    private fun printModelStates(): Map<String, Any> {
        val map = mutableMapOf<String, List<List<Any>>>()

        val smp = NGTUtil.isSMP()

        val allModels = collectAllModels(smp)
        for ((sourcePackPath, models) in allModels.groupBy { it.sourcePackPath }) {
            map[sourcePackPath] = models.map { model ->
                listOf(
                    model.state.toString(),
                    model.id,
                    model.sourceFile,
                    model.isIncludedInSMP,
                )
            }
        }
        return map
    }
    /*
    the json formant
    [version, value]
    // v1.0 format
    [1,Map<model_pack:String,List<ModelPack>>]
    ModelPack=[state:string,id:string,sourceName:string,includedInSMP:boolean]
     */
}
