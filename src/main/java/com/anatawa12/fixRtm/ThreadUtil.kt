/// Copyright (c) 2021 anatawa12 and other contributors
/// This file is part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

package com.anatawa12.fixRtm

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.relauncher.Side
import java.util.concurrent.ConcurrentLinkedQueue

object ThreadUtil {
    @JvmStatic
    fun runOnClientThread(block: Action) {
        client.add(block)
    }

    @JvmStatic
    fun runOnServerThread(block: Action) {
        server.add(block)
    }

    @JvmStatic
    fun runOnMainThread(side: Side, block: Action) {
        if (side.isClient) runOnClientThread(block)
        else runOnServerThread(block)
    }

    fun interface Action {
        operator fun invoke()
    }

    @SubscribeEvent
    fun onClientTick(@Suppress("UNUSED_PARAMETER") e: TickEvent.ClientTickEvent) {
        while (true) (client.poll() ?: return)()
    }

    @SubscribeEvent
    fun onServerTick(@Suppress("UNUSED_PARAMETER") e: TickEvent.ServerTickEvent) {
        while (true) (server.poll() ?: return)()
    }

    private val client = ConcurrentLinkedQueue<Action>()
    private val server = ConcurrentLinkedQueue<Action>()
}
