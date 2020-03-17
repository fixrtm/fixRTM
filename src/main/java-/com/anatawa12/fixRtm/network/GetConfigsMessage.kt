package com.anatawa12.fixRtm.network

import com.anatawa12.fixRtm.ExModelPackManager
import io.netty.buffer.ByteBuf
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext

class GetConfigsMessage() : IMessageAndHandler<GetConfigsMessage> {
    override fun onMessage(ctx: MessageContext?): IMessage? {
        val map = ExModelPackManager.allModelSetMap
        return SendConfigsMessage(map)
    }

    override fun fromBytes(buf: ByteBuf?) {
    }

    override fun toBytes(buf: ByteBuf?) {
    }
}

