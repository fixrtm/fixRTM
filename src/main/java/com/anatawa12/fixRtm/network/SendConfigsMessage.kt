package com.anatawa12.fixRtm.network

import com.anatawa12.fixRtm.ExModelPackManager
import io.netty.buffer.ByteBuf
import jp.ngt.ngtlib.io.NGTJson
import jp.ngt.rtm.modelpack.ResourceType
import jp.ngt.rtm.modelpack.cfg.ResourceConfig
import jp.ngt.rtm.modelpack.modelset.ResourceSet
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext

class SendConfigsMessage : IMessageAndHandler<SendConfigsMessage> {
    @Deprecated("used by reflection")
    constructor() : super()

    constructor(inMap: MutableMap<ResourceType<*, *>, MutableMap<String, ResourceSet<*>>>) : super() {
        this.map = mutableMapOf()
        for ((type, resourceSets) in inMap) {
            for ((name, resourceSet) in resourceSets) {
                map.getOrPut(type, ::mutableListOf).add(resourceSet.config)
            }
        }
    }

    lateinit var map: MutableMap<ResourceType<*, *>, MutableList<ResourceConfig>>

    override fun onMessage(ctx: MessageContext?): IMessage? {

        return null
    }

    override fun fromBytes(buf: ByteBuf) {
        map = mutableMapOf()
        repeat(buf.readInt()) { _ ->
            val typeName = buf.readString()
            val type = ExModelPackManager.allModelSetMap.keys.find { it.name == typeName }!!
            map[type] = mutableListOf()
            repeat(buf.readInt()) {
                val config =  NGTJson.getObjectFromJson(buf.readString(), type.cfgClass)
                map[type]!!.add(config)
            }
        }
    }

    override fun toBytes(buf: ByteBuf) {
        buf.writeInt(map.size)
        for ((type, resourceCfgs) in map) {
            buf.writeString(type.name)
            buf.writeInt(resourceCfgs.size)
            for (resourceCfg in resourceCfgs) {
                buf.writeString(NGTJson.getJsonFromObject(resourceCfg))
            }
        }
    }
}

fun ByteBuf.writeString(str: String) {
    val bytes = str.toByteArray()
    writeInt(str.length)
    writeBytes(bytes)
}

fun ByteBuf.readString() : String {
    val size = readInt()
    val bytes = ByteArray(size)
    readBytes(bytes)
    return bytes.toString(Charsets.UTF_8)
}
