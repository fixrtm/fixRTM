package com.anatawa12.fixRtm.ngtlib.renderer.model

import com.anatawa12.fixRtm.asm.Preprocessor
import com.anatawa12.fixRtm.asm.config.KVSConfig
import com.anatawa12.fixRtm.asm.config.MainConfig
import com.anatawa12.fixRtm.minecraftDir
import com.anatawa12.fixRtm.readUTFNullable
import com.anatawa12.fixRtm.threadFactoryWithPrefix
import com.anatawa12.fixRtm.writeUTFNullable
import com.google.common.collect.Iterators
import jp.ngt.ngtlib.io.FileType
import jp.ngt.ngtlib.renderer.model.*
import net.minecraft.util.ResourceLocation
import org.apache.commons.codec.digest.DigestUtils
import org.apache.logging.log4j.LogManager
import java.io.*
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

object CachedPolygonModel {
    val type = FileType("fixrtm-cached-polygon-model-file", "fixrtm cached polygon model file.")
    private val logger = LogManager.getLogger("jasm-model-cache")
    private val executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(),
            threadFactoryWithPrefix("jasm-model-cache-creating"))

    internal fun init() {
        // first, static initializer
        checkCache()
    }

    //private var loadedCache: SoftReference<ConcurrentHashMap<String, CachedModel>>? = null
    private val cache = ConcurrentHashMap<String, CachedModel>()
    private var writings = Collections.newSetFromMap<String>(ConcurrentHashMap())
    private val baseDir = minecraftDir.resolve("fixrtm-model-cache")
    private val checkLock = ReentrantLock()
    private var checked = false

    private fun checkCache() {
        if (checked) return
        checkLock.withLock {
            if (checked) return
            baseDir.mkdirs()
            val digest = DigestUtils.sha1Hex(SequenceInputStream(
                    Iterators.asEnumeration(
                            minecraftDir.resolve("mods")
                                    .walkBottomUp()
                                    .filter { it.isFile }
                                    .map { it.canonicalFile }
                                    .sorted()
                                    .flatMap {
                                        sequenceOf(
                                                it.path.toByteArray(Charsets.UTF_16).inputStream(),
                                                it.inputStream().buffered())
                                    }
                                    .iterator()
                    )
            ))
            val modsDigest = baseDir.resolve("mods-digest")
                    .also { it.appendText("") }
                    .readText()
            if (digest != modsDigest) {
                logger.warn("mods dir may changed so discord cache")
                baseDir.deleteRecursively()
                baseDir.mkdirs()
                baseDir.resolve("mods-digest").writeText(digest)
            } else {
                val hex2 = "[0-9a-fA-F]{2}".toRegex()
                val hex40 = "[0-9a-fA-F]{40}".toRegex()
                baseDir.listFiles()!!
                        .asSequence()
                        .filter { it.isDirectory }
                        .filter { hex2.matches(it.name) }
                        .flatMap { it.listFiles()!!.asSequence() }
                        .filter { it.isFile }
                        .filter { hex40.matches(it.name) }
                        .forEach { file ->
                            executor.submit {
                                CachedModel(file.inputStream().buffered())
                                        .also { cache[file.name] = it }
                                CachedModel(file.inputStream().buffered())
                                        .also { cache[file.name] = it }
                            }
                        }
            }
            checked = true
        }
    }

    private fun getCacheValue(sha1: String) = cache[sha1]

    fun getCachedModel(sha1: String): PolygonModel? {
        getCacheValue(sha1)?.let { return it }
        if (sha1 in writings) return null
        val file = getFile(sha1)
        if (!file.exists()) return null

        try {
            return CachedModel(file.inputStream().buffered())
                    .also { cache[sha1] = it }
        } catch (e: IOException) {
            file.delete()
            logger.error("reading cache", e)
            return null
        } finally {
        }
    }

    fun putCachedModel(sha1: String, model: PolygonModel) {
        executor.submit {
            val file = getFile(sha1)
            if (file.exists())
                logger.warn("sha butting: $sha1")
            writings.add(sha1)
            try {
                val bas = ByteArrayOutputStream()
                CachedModelWriter.writeCachedModel(DataOutputStream(bas), model)
                file.outputStream().buffered().use { bas.writeTo(it) }
            } catch (e: IOException) {
                file.delete()
                logger.error("putting cache", e)
            } finally {
                writings.remove(sha1)
            }
        }
    }

    private fun getFile(sha1: String) = baseDir
            .resolve(sha1.substring(0, 2))
            .also { it.mkdirs() }
            .resolve(sha1)

    private class CachedModel(file: InputStream): PolygonModel() {
        private val materials = mutableMapOf<String, Material>()

        init {
            val reader = DataInputStream(file) as DataInput
            drawMode = reader.readInt()
            accuracy = readVecAccuracy(reader)
            readSizeBox(reader)
            repeat(reader.readInt()) {
                groupObjects.add(readGroupObject(reader))
            }
            repeat(reader.readInt()) {
                materials[reader.readUTF()] = readMaterial(reader)
            }
        }

        private fun readVecAccuracy(reader: DataInput) = when (val value = reader.readUnsignedByte()) {
            0 -> VecAccuracy.LOW
            1 -> VecAccuracy.MEDIUM
            else -> error("invalid VecAccuracy: $value")
        }

        private fun readSizeBox(reader: DataInput) {
            sizeBox[0] = reader.readFloat()
            sizeBox[1] = reader.readFloat()
            sizeBox[2] = reader.readFloat()
            sizeBox[3] = reader.readFloat()
            sizeBox[4] = reader.readFloat()
            sizeBox[5] = reader.readFloat()
        }

        private fun readGroupObject(reader: DataInput): GroupObject {
            val obj = GroupObject(reader.readUTF(), reader.readByte().toInt())
            obj.smoothingAngle = reader.readFloat()
            repeat(reader.readInt()) {
                obj.faces.add(readFace(reader))
            }
            return obj
        }

        private fun readFace(reader: DataInput): Face {
            val material = reader.readByte().toInt()
            val size = reader.readInt()
            val face = Face(size, material)
            face.vertexNormals = arrayOfNulls(size)
            repeat(size) { i ->
                face.vertices[i] = readVertex(reader)
                face.textureCoordinates[i] = readTextureCoordinate(reader)
                face.vertexNormals[i] = readVertex(reader)
            }
            face.faceNormal = readVertex(reader)

            return face
        }

        private fun readVertex(reader: DataInput): Vertex {
            return Vertex.create(
                    reader.readFloat(),
                    reader.readFloat(),
                    reader.readFloat(),
                    accuracy
            )
        }

        private fun readTextureCoordinate(reader: DataInput): TextureCoordinate {
            return TextureCoordinate.create(
                    reader.readFloat(),
                    reader.readFloat(),
                    accuracy
            )
        }

        private fun readMaterial(reader: DataInput): Material {
            return Material(reader.readByte(), reader.readUTFNullable()?.let { ResourceLocation(it) })
        }

        override fun getMaterials(): Map<String, Material> = materials
        override fun getType(): FileType = CachedPolygonModel.type

        override fun parseLine(p0: String?, p1: Int) = TODO("Not yet implemented")
        override fun postInit() = TODO("Not yet implemented")
    }

    private object CachedModelWriter {

        fun writeCachedModel(writer: DataOutput, value: PolygonModel) {
            writer.writeInt(value.drawMode)
            writeVecAccuracy(writer, value.accuracy)
            writeSizeBox(writer, value.size)
            writer.writeInt(value.groupObjects.size)
            for (groupObject in value.groupObjects) {
                writeGroupObject(writer, groupObject)
            }
            writer.writeInt(value.materials.size)
            for ((name, material) in value.materials) {
                writer.writeUTF(name)
                writeMaterial(writer, material)
            }
        }


        private fun writeVecAccuracy(writer: DataOutput, value: VecAccuracy) = when (value) {
            VecAccuracy.LOW -> writer.writeByte(0)
            VecAccuracy.MEDIUM -> writer.writeByte(1)
            else -> error("invalid VecAccuracy: $value")
        }

        private fun writeSizeBox(writer: DataOutput, value: FloatArray) {
            writer.writeFloat(value[0])
            writer.writeFloat(value[1])
            writer.writeFloat(value[2])
            writer.writeFloat(value[3])
            writer.writeFloat(value[4])
            writer.writeFloat(value[5])
        }

        private fun writeGroupObject(writer: DataOutput, value: GroupObject) {
            writer.writeUTF(value.name)
            writer.writeByte(value.drawMode.toInt())
            writer.writeFloat(value.smoothingAngle)
            writer.writeInt(value.faces.size)
            for (face in value.faces) {
                writeFace(writer, face)
            }
        }

        private fun writeFace(writer: DataOutput, value: Face) {
            writer.writeByte(value.materialId.toInt())
            writer.writeInt(value.vertices.size)
            for (i in value.vertices.indices) {
                writeVertex(writer, value.vertices[i])
                writeTextureCoordinate(writer, value.textureCoordinates[i])
                writeVertex(writer, value.vertexNormals[i])
            }

            writeVertex(writer, value.faceNormal)
        }

        private fun writeVertex(writer: DataOutput, value: Vertex) {
            writer.writeFloat(value.x)
            writer.writeFloat(value.y)
            writer.writeFloat(value.z)
        }

        private fun writeTextureCoordinate(writer: DataOutput, value: TextureCoordinate) {
            writer.writeFloat(value.u)
            writer.writeFloat(value.v)
        }

        private fun writeMaterial(writer: DataOutput, value: Material) {
            writer.writeByte(value.id.toInt())
            writer.writeUTFNullable(value.texture?.toString())
        }
    }
}
