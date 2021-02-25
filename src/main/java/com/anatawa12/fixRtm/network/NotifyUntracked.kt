package com.anatawa12.fixRtm.network

import com.anatawa12.fixRtm.Loggers
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
            val entry = ctx.serverHandler.player.world
                .let { it as WorldServer }
                .entityTracker
                .trackedEntityHashTable
                .lookup(message.entityId)
                ?: return null.also { logger.error("entry for #${message.entityId} not found") }

            entry.trackingPlayers.remove(ctx.serverHandler.player)

            return null
        }

        private val logger = Loggers.getLogger(NotifyUntracked::class.simpleName!!)
    }
}
