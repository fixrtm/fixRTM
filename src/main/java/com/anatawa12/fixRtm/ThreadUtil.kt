package com.anatawa12.fixRtm

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.util.concurrent.ConcurrentLinkedQueue

object ThreadUtil {
    fun runOnClientThread(block: () -> Unit) {
        client.add(block)
    }

    fun runOnServerThread(block: () -> Unit) {
        server.add(block)
    }

    @SubscribeEvent
    fun onClientTick(e: TickEvent.ClientTickEvent) {
        while (true) (client.poll() ?: return)()
    }

    @SubscribeEvent
    fun onServerTick(e: TickEvent.ServerTickEvent) {
        while (true) (server.poll() ?: return)()
    }

    private val client = ConcurrentLinkedQueue<() -> Unit>()
    private val server = ConcurrentLinkedQueue<() -> Unit>()
}
