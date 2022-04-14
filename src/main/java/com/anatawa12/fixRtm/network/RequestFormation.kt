/// Copyright (c) 2021 anatawa12 and other contributors
/// This file is part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

package com.anatawa12.fixRtm.network

import com.anatawa12.fixRtm.Loggers
import io.netty.buffer.ByteBuf
import jp.ngt.rtm.RTMCore
import jp.ngt.rtm.entity.train.util.FormationManager
import jp.ngt.rtm.network.PacketFormation
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext

/**
 * The packet to notify the entity was unloaded on client.
 */
class RequestFormation : IMessage {
    var formationId: Long = 0

    @Deprecated("", level = DeprecationLevel.HIDDEN)
    constructor()

    constructor(entityId: Long) {
        this.formationId = entityId
    }

    override fun fromBytes(buf: ByteBuf) {
        formationId = buf.readLong()
    }

    override fun toBytes(buf: ByteBuf) {
        buf.writeLong(formationId)
    }

    companion object : IMessageHandler<RequestFormation, Nothing?> {
        override fun onMessage(message: RequestFormation, ctx: MessageContext): Nothing? {
            FormationManager.getInstance().getFormation(message.formationId)
                ?.let { RTMCore.NETWORK_WRAPPER.sendTo(PacketFormation(it), ctx.serverHandler.player) }
            return null
        }

        private val logger = Loggers.getLogger(RequestFormation::class.simpleName!!)
    }
}
