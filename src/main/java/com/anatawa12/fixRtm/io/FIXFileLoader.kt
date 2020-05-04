package com.anatawa12.fixRtm.io

import com.anatawa12.fixRtm.directoryDigestBaseStream
import com.anatawa12.fixRtm.minecraftDir
import net.minecraft.util.ResourceLocation
import org.apache.commons.codec.digest.DigestUtils
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.util.*
import java.util.zip.ZipFile

object FIXFileLoader {
    val allModelPacks: Set<FIXModelPack>
    private val packs: Map<String, Set<FIXModelPack>>

    init {
        val packs = HashMap<String, MutableSet<FIXModelPack>>()
        val mods = minecraftDir.resolve("mods")

        for (file in mods.listFiles()!!) {
            val pack = loadModelPack(file)

            for (domain in pack.domains) {
                packs.computeIfAbsent(domain) { HashSet() }.add(pack)
            }
        }

        this.packs = packs

        allModelPacks = packs.flatMapTo(mutableSetOf()) { it.value }
    }

    fun getResource(location: ResourceLocation): FIXResource {
        for (fixModelPack in packs[location.namespace].orEmpty()) {
            fixModelPack.getFile(location)?.let { return it }
        }
        throw FileNotFoundException("$location")
    }

    fun getInputStream(location: ResourceLocation): InputStream = getResource(location).inputStream

    private fun loadModelPack(file: File): FIXModelPack = if (file.isFile) {
        ZipModelPack(file)
    } else {
        DirectoryModelPack(file)
    }

    private class ZipModelPack(override val file: File) : FIXModelPack {
        private val zipFile = ZipFile(file)

        override val sha1Hash: String = DigestUtils.sha1Hex(file.inputStream().buffered())

        override val domains: Set<String>

        init {
            val domains = mutableSetOf<String>()
            for (entry in zipFile.entries()) {
                val parts = entry.name.split("/")
                if (parts[0] == "assets" && parts.size == 2)
                    domains.add(parts[1])
            }
            this.domains = domains
        }

        override fun getFile(location: ResourceLocation): FIXResource? {
            val path = "assets/${location.namespace}/${location.path}"
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
}
