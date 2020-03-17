package com.anatawa12.fixRtm.network

import net.minecraftforge.fml.common.network.NetworkRegistry
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler





object NetworkHandler {
    private val INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel("AegisSystemMod")

    internal fun init() {

    }

    private fun <REQ : IMessage, REPLY : IMessage> registerMessage(messageHandler: Class<out IMessageHandler<REQ, REPLY>>, requestMessageType: Class<REQ>, discriminator: Int, sendTo: Side) {
        INSTANCE.registerMessage(messageHandler, requestMessageType, discriminator, sendTo)
    }

    fun sendPacketServer(message: IMessage) {
        INSTANCE.sendToServer(message)
    }

    fun sendPacketAll(message: IMessage) {
        INSTANCE.sendToAll(message)
    }

    fun sendPacketEPM(message: IMessage, EPM: EntityPlayerMP) {
        INSTANCE.sendTo(message, EPM)
    }

    fun sendPacketAround(message: IMessage, dimension: Int, x: Double, y: Double, z: Double, range: Double) {
        INSTANCE.sendToAllAround(message, NetworkRegistry.TargetPoint(dimension, x, y, z, range))
    }
}