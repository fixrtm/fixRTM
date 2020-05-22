package com.anatawa12.fixRtm.io

import com.anatawa12.fixRtm.Loggers
import com.anatawa12.fixRtm.MS932
import com.anatawa12.fixRtm.directoryDigestBaseStream
import com.anatawa12.fixRtm.minecraftDir
import com.anatawa12.fixRtm.utils.DigestUtils
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.relauncher.FMLLaunchHandler
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.net.URI
import java.nio.charset.Charset
import java.util.*
import java.util.zip.ZipFile

object FIXFileLoader {
    val allModelPacks: Set<FIXModelPack>
    private val packs: Map<String, Set<FIXModelPack>>

    private val logger = Loggers.getLogger("FIXFileLoader")

    init {
        val packs = HashMap<String, MutableSet<FIXModelPack>>()

        for (file in getFiles()) {
            val pack = loadModelPack(file) ?: continue

            for (domain in pack.domains) {
                packs.computeIfAbsent(domain) { HashSet() }.add(pack)
            }
        }

        this.packs = packs

        allModelPacks = packs.flatMapTo(mutableSetOf()) { it.value }
        logger.trace("FIXFileLoader loads model packs:")
        for (pack in allModelPacks) {
            logger.trace("${pack.file.name}: ${pack.domains}")
        }
    }

    fun getFiles(): List<File> {
        if (!FMLLaunchHandler.isDeobfuscatedEnvironment())
            return minecraftDir.resolve("mods").listFiles()!!.asList()
        else
            return (minecraftDir.resolve("mods").listFiles()!!.asList()
                    + listOf(File(URI(FIXFileLoader::class.java.protectionDomain.codeSource.location.path.substringBefore('!')))))
    }

    fun getResource(location: ResourceLocation): FIXResource {
        for (fixModelPack in packs[location.namespace].orEmpty()) {
            fixModelPack.getFile(location)?.let { return it }
        }
        throw FileNotFoundException("$location")
    }

    fun getInputStream(location: ResourceLocation): InputStream = getResource(location).inputStream

    private fun loadModelPack(file: File): FIXModelPack? {
        try {
            if (file.isFile) {
                if (file.extension != "jar" && file.extension != "zip") return null
                return ZipModelPack(file)
            } else {
                return DirectoryModelPack(file)
            }
        } catch (e: IllegalArgumentException) {
            if (file.isFile) {
                return ZipModelPack(file, MS932)
            } else {
                throw e
            }
        } catch (e: Throwable) {
            logger.error("trying to construct model pack: ${file.name}", e)
            return null
        }
    }

    private class ZipModelPack(override val file: File, charset: Charset = Charsets.UTF_8) : FIXModelPack {
        private val zipFile = ZipFile(file, charset)

        override val sha1Hash: String = DigestUtils.sha1Hex(file.inputStream().buffered())

        override val domains: Set<String>
        private val ignoreCaseMap: Map<String, String>

        init {
            val domains = mutableSetOf<String>()
            val ignoreCaseMap = mutableMapOf<String, String>()
            for (entry in zipFile.entries()) {
                val parts = entry.name.split("/")
                if (parts[0] == "assets" && parts.size >= 2 && parts[1].isNotEmpty())
                    domains.add(parts[1])
                ignoreCaseMap[entry.name.toLowerCase()] = entry.name
            }
            this.domains = domains
            this.ignoreCaseMap = ignoreCaseMap
        }

        override fun getFile(location: ResourceLocation): FIXResource? {
            var path = "assets/${location.namespace}/${location.path}"
            path = ignoreCaseMap[path] ?: path
            val file = zipFile.getEntry(path) ?: return null
            return FIXResource(this, zipFile.getInputStream(file))
        }
    }

    private class DirectoryModelPack(override val file: File) : FIXModelPack {
        override val sha1Hash: String = DigestUtils.sha1Hex(file.directoryDigestBaseStream())

        override val domains: Set<String>

        init {
            val domains = mutableSetOf<String>()
            file.resolve("assets").listFiles()?.forEach { assetDir ->
                if (assetDir.isDirectory)
                    domains.add(assetDir.name)
            }
            this.domains = domains
        }

        override fun getFile(location: ResourceLocation): FIXResource? {
            val path = "assets/${location.namespace}/${location.path}"
            try {
                return FIXResource(this, file.resolve(path).inputStream())
            } catch (e: FileNotFoundException) {
                return null
            }
        }
    }

    fun load() {}
}
