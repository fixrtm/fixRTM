/// Copyright (c) 2023 anatawa12 and other contributors
/// This file is part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

package com.anatawa12.fixRtm.rtm.modelpack

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import jp.ngt.ngtlib.io.NGTFileLoader
import net.minecraft.client.resources.IResourcePack
import net.minecraft.client.resources.data.IMetadataSection
import net.minecraft.client.resources.data.MetadataSerializer
import net.minecraft.util.ResourceLocation
import java.awt.image.BufferedImage
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

abstract class FixRTMResourcePack(
        @JvmField protected val domain: String,
        protected val source: File,
) : IResourcePack {
    companion object {
        @JvmStatic fun createPack(domain: String, source: File): FixRTMResourcePack {
            val path = source.absolutePath
            val suffix = NGTFileLoader.getArchiveSuffix(path)
            if (suffix.isNotEmpty()) {
                val archive = NGTFileLoader.getArchivePath(path, suffix)
                return ArchivePack(domain, source, archive)
            } else {
                return FolderPack(domain, source)
            }
        }
    }

    class ArchivePack(domain: String, source: File, private val sourceZip: String) : FixRTMResourcePack(domain, source) {
        private var zipCache: ZipFile? = null
        private val entryCache = HashMap<String, ZipEntry?>()

        private val asZipFile: ZipFile get() {
            val zipCache = zipCache
            if (zipCache != null) return zipCache

            @Throws(IOException::class)
            fun tryOpenZipFile(zipPath: String, encoding: String): ZipFile {
                val zip = NGTFileLoader.getArchive(File(zipPath), encoding)
                // trace to end to check charset
                zip.entries().asSequence().count()
                return zip
            }

            val zipFile = try {
                tryOpenZipFile(sourceZip, "UTF-8")
            } catch (ignored: IllegalArgumentException) {
                tryOpenZipFile(sourceZip, "MS932")
            }
            this.zipCache = zipFile
            entryCache.clear()
            return zipFile
        }

        @Throws(IOException::class)
        fun findZipEntry(par1: ResourceLocation): ZipEntry? {
            if (par1.namespace != domain) return null

            val path = par1.path
            entryCache[path]?.let { return it }
            if (path in entryCache) return null;

            val entry = asZipFile.entries()
                    .asSequence()
                    .filter { !it.isDirectory }
                    .filter { path.contains(it.name.substringAfterLast('/')) }
                    .firstOrNull()

            entryCache[path] = entry
            return entry
        }

        override fun getInputStream(location: ResourceLocation): InputStream {
            val entry = findZipEntry(location) ?: throw FileNotFoundException(location.path)
            return asZipFile.getInputStream(entry)
        }

        override fun resourceExists(location: ResourceLocation) = findZipEntry(location) != null
    }

    class FolderPack(domain: String, source: File) : FixRTMResourcePack(domain, source) {
        override fun getInputStream(location: ResourceLocation): InputStream {
            return FileInputStream(File(source, location.path))
        }

        override fun resourceExists(location: ResourceLocation): Boolean {
            return File(source, location.path).exists()
        }
    }

    override fun <T : IMetadataSection?> getPackMetadata(metadataSerializer: MetadataSerializer, metadataSectionName: String): T? {
        try {
            return metadataSerializer.parseMetadataSection<T>(metadataSectionName, JsonObject().apply {
                add("pack", JsonObject().apply {
                    add("pack_format", JsonPrimitive(3))
                    add("description", JsonPrimitive("wrapper pack of $source by anatawa12"))
                })
            })
        } catch (var4: RuntimeException) {
            return null
        } catch (var5: FileNotFoundException) {
            return null
        }
    }

    override fun getPackImage(): BufferedImage {
        TODO("Not yet implemented")
    }

    override fun getPackName(): String = "RTMCustom"

    override fun getResourceDomains(): Set<String> = setOf(domain)
}
