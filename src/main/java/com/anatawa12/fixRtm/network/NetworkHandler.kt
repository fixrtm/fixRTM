/// Copyright (c) 2019 anatawa12 and other contributors
/// This file is part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

package com.anatawa12.fixRtm.network

import net.minecraft.entity.player.EntityPlayerMP
import net.minecraftforge.fml.common.network.NetworkRegistry
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler
import net.minecraftforge.fml.relauncher.Side


object NetworkHandler {
    @JvmStatic
    private val INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel("fix-rtm")

    @JvmStatic
    internal fun init() {
        registerMessage(SentAllModels, SentAllModels::class.java, 0x01, Side.CLIENT)
        registerMessage(NotifyUntracked, NotifyUntracked::class.java, 0x02, Side.SERVER)
        registerMessage(RequestFormation, RequestFormation::class.java, 0x03, Side.SERVER)
    }

    @JvmStatic
    private fun <REQ : IMessage, REPLY : IMessage?> registerMessage(
        messageHandler: IMessageHandler<REQ, REPLY>,
        requestMessageType: Class<REQ>,
        discriminator: Int,
        sendTo: Side,
    ) {
        INSTANCE.registerMessage(messageHandler, requestMessageType, discriminator, sendTo)
    }

    @JvmStatic
    fun sendPacketServer(message: IMessage) {
        INSTANCE.sendToServer(message)
    }

    @JvmStatic
    fun sendPacketAll(message: IMessage) {
        INSTANCE.sendToAll(message)
    }

    @JvmStatic
    fun sendPacketEPM(message: IMessage, EPM: EntityPlayerMP) {
        INSTANCE.sendTo(message, EPM)
    }

    @JvmStatic
    fun sendPacketAround(message: IMessage, dimension: Int, x: Double, y: Double, z: Double, range: Double) {
        INSTANCE.sendToAllAround(message, NetworkRegistry.TargetPoint(dimension, x, y, z, range))
    }
}
