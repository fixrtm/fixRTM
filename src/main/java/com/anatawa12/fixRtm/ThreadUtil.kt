/// Copyright (c) 2021 anatawa12 and other contributors
/// This file is/was part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

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
