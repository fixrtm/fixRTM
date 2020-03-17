package com.anatawa12.fixRtm.network

import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext

interface IMessageAndHandler<This: IMessageAndHandler<This>> : IMessage, IMessageHandler<This, IMessage?> {
    override fun onMessage(message: This, ctx: MessageContext?): IMessage? {
        return message.onMessage(ctx)
    }
    fun onMessage(ctx: MessageContext?): IMessage?
}