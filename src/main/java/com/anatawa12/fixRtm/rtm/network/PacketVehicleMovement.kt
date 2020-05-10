@file:JvmName("PacketVehicleMovementKt")

package com.anatawa12.fixRtm.rtm.network

import com.anatawa12.fixRtm.Loggers
import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity

fun addEntityIfNotExits(entity: Entity) {
    val world = Minecraft.getMinecraft().world
    if ((!entity.addedToChunk || !entity.isAddedToWorld)
            && world.isBlockLoaded(entity.position, false)) {
        Minecraft.getMinecraft().addScheduledTask {
            logger.info("the entity: $entity is not added")
            world.spawnEntity(entity)
        }
    }
}

private val logger = Loggers.getLogger("PacketVehicleMovementKt")
