@file:Suppress("unused") // Used with Transform

package com.anatawa12.fixRtm

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import jp.ngt.ngtlib.event.TickProcessEntry
import jp.ngt.ngtlib.io.NGTLog
import jp.ngt.ngtlib.renderer.model.ModelFormatException
import jp.ngt.rtm.modelpack.ResourceType
import jp.ngt.rtm.modelpack.modelset.ResourceSet
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.text.TextComponentTranslation
import java.util.zip.Inflater

object ExModelPackManager {
    var dummyMap: Map<String, ResourceSet<*>>
        get() = error("impl in gen")
        set(v) = error("impl in gen")

    @Suppress("UNCHECKED_CAST")
    val allModelSetMap: MutableMap<ResourceType<*, *>, MutableMap<String, ResourceSet<*>>>
            = jp.ngt.rtm.modelpack.ModelPackManager::class.java.getDeclaredField("allModelSetMap")
            .apply { isAccessible = true }
            .get(ModelPackManager) as MutableMap<ResourceType<*, *>, MutableMap<String, ResourceSet<*>>>
}

fun eraseNullForModelSet(inSet: ResourceSet<*>?, type: ResourceType<*, *>): ResourceSet<*> {
    if (inSet != null) return inSet
    if (type.hasSubType) {
        return ExModelPackManager.dummyMap[type.name]
                ?: (ExModelPackManager.dummyMap[type.subType])
                ?: error("ResourceType(${type.name}) and ResourceType(${type.subType}) don't have dummy ResourceSet")
    } else {
        return ExModelPackManager.dummyMap[type.name]
                ?: error("ResourceType(${type.name}) don't have dummyMap")
    }
}

private fun Any?.defaultToString(): String = if (this == null) {
    "null"
} else {
    this.javaClass.name + "@" + Integer.toHexString(System.identityHashCode(this))
}

fun preProcess(entry: TickProcessEntry?) {
}

fun postProcess(isEnd: Boolean) {
}

fun postProcess() {
}

fun preProcess() {
}
fun eraseNullForAddTickProcessEntry(addEntry: TickProcessEntry?, inEntry: TickProcessEntry?) {

    requireNotNull(inEntry) {
        "TickProcessQueue.add's first argument is null. fixRtm (made by anataqa12) found a bug! this is a bug from RTM and anatawa12 think this is good trace for fix bug."
    }
}

fun wrapWithDeflate(byteBuf: ByteBuf): ByteBuf {
    return DeflateByteBuf(byteBuf)
}

fun writeToDeflate(byteBuf: ByteBuf) {
    (byteBuf as DeflateByteBuf).writeDeflated()
}

fun readFromDeflate(byteBuf: ByteBuf): ByteBuf {
    val inf = Inflater()

    var readBuf: ByteArray? = ByteArray(byteBuf.readableBytes())
    byteBuf.readBytes(readBuf)
    inf.setInput(readBuf)
    println("received packet size: ${readBuf?.size}")
    readBuf = null

    val result = Unpooled.buffer()
    val buf = ByteArray(1024)
    while (true) {
        val len = inf.inflate(buf)
        if (len == 0) break
        result.writeBytes(buf, 0, len)
    }
    println("real packet size: ${result.readableBytes()}")
    println("real packet: $result")
    return result
}

fun <K, V>MovingSoundMaker_loadSoundJson_nullCheck(map: Map<K, V>?, domain: String): Map<K, V> {
    if (map == null)
        throw ModelFormatException("sound.json for $domain is invalid.")
    return map
}


var BlockMarker_onMarkerActivated_player: EntityPlayer? = null
fun BlockMarker_onMarkerActivated(player: EntityPlayer) {
    if (!player.world.isRemote)
        BlockMarker_onMarkerActivated_player = player
}

fun sendSwitchTypeError(message: String, vararg objects: Any?) {
    val player = BlockMarker_onMarkerActivated_player
    if (player == null) {
        Exception("fixRTM Bug!!! BlockMarker_onMarkerActivated_player is not init-ed").printStackTrace()
        NGTLog.sendChatMessageToAll(message, objects)
        return
    }
    player.sendMessage(TextComponentTranslation(message, *objects))
    BlockMarker_onMarkerActivated_player = null
}
