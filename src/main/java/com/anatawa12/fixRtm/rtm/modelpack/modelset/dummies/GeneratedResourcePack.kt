package com.anatawa12.fixRtm.rtm.modelpack.modelset.dummies

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import net.minecraft.client.resources.IResourcePack
import net.minecraft.client.resources.data.IMetadataSection
import net.minecraft.client.resources.data.MetadataSerializer
import net.minecraft.util.ResourceLocation
import java.awt.image.BufferedImage
import java.io.*
import javax.imageio.ImageIO

object GeneratedResourcePack : IResourcePack {
    private val DOMEIN = "fix-rtm"
    private var nextId = 1;
    private val resourceGenerators = mutableMapOf<String, () -> ResourceFile>()
    private val resources = mutableMapOf<String, ResourceFile>()

    override fun resourceExists(location: ResourceLocation): Boolean
            = location.namespace == DOMEIN && location.path in resourceGenerators

    override fun getPackImage(): BufferedImage {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : IMetadataSection?> getPackMetadata(metadataSerializer: MetadataSerializer, metadataSectionName: String): T? {
        try {
            return metadataSerializer.parseMetadataSection<T>(metadataSectionName, JsonObject().apply {
                add("pack", JsonObject().apply {
                    add("pack_format", JsonPrimitive(3))
                    add("description", JsonPrimitive("anatawa12 fix rtm button resource virtual pack"))
                })
            })
        } catch (var4: RuntimeException) {
            return null
        } catch (var5: FileNotFoundException) {
            return null
        }
    }

    override fun getInputStream(location: ResourceLocation): InputStream {
        if (location.namespace != DOMEIN) throw FileNotFoundException(location.path)
        var resource = resources[location.path]
        if (resource == null) {
            val generator = resourceGenerators[location.path]
                    ?: throw FileNotFoundException(location.path)
            resource = generator()
            resources[location.path]= resource
        }
        return resource.byteArray.inputStream()
    }

    override fun getPackName(): String = "anatawa12 fix rtm button resource virtual pack"

    override fun getResourceDomains(): Set<String> = setOf(DOMEIN)

    fun addImage(img: BufferedImage): ResourceLocation = addImage("%x08.png".format(nextId++), img)

    fun addImage(name: String, img: BufferedImage): ResourceLocation = addImageGenerator(name) { img }

    fun addImageGenerator(name: String, generator: () -> BufferedImage): ResourceLocation = addFileGenerator(name) {
        val byteArrayOutputStream = ByteArrayOutputStream()
        ImageIO.write(generator(), "PNG", byteArrayOutputStream)
        byteArrayOutputStream.toByteArray()
    }

    fun addFile(name: String, data: ByteArray): ResourceLocation = addFileGenerator(name) { data }

    fun addFileGenerator(name: String, generator: () -> ByteArray): ResourceLocation {
        resourceGenerators[name] = { ResourceFile(generator()) }
        return ResourceLocation(DOMEIN, name)
    }

    private data class ResourceFile(val byteArray: ByteArray)
}
