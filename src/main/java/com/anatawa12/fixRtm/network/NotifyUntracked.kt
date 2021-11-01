/// Copyright (c) 2021 anatawa12 and other contributors
/// This file is/was part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

package com.anatawa12.fixRtm.network

import com.anatawa12.fixRtm.Loggers
import com.anatawa12.fixRtm.ThreadUtil
import io.netty.buffer.ByteBuf
import net.minecraft.world.WorldServer
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext

/**
 * The packet to notify the entity was unloaded on client.
 */
class NotifyUntracked : IMessage {
    var entityId: Int = 0

    @Deprecated("", level = DeprecationLevel.HIDDEN)
    constructor()

    constructor(entityId: Int) {
        this.entityId = entityId
    }

    override fun fromBytes(buf: ByteBuf) {
        entityId = buf.readInt()
    }

    override fun toBytes(buf: ByteBuf) {
        buf.writeInt(entityId)
    }

    companion object : IMessageHandler<NotifyUntracked, Nothing?> {
        override fun onMessage(message: NotifyUntracked, ctx: MessageContext): Nothing? {
            val world = ctx.serverHandler.player.world as WorldServer
            ThreadUtil.runOnServerThread {
                val entry = world
                    .entityTracker
                    .trackedEntityHashTable
                    .lookup(message.entityId)
                    ?: return@runOnServerThread logger.error("entry for #${message.entityId} not found")

                entry.trackingPlayers.remove(ctx.serverHandler.player)
            }

            return null
        }

        private val logger = Loggers.getLogger(NotifyUntracked::class.simpleName!!)
    }
}
