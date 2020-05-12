package com.anatawa12.fixRtm

import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import java.util.*

object VehicleTrackerEntryFix {
    private val instances = Collections.newSetFromMap<Entity>(WeakHashMap())

    fun addInstance(instance: Entity) {
        instances.add(instance)
    }

    fun tick() {
        val mc = Minecraft.getMinecraft()
        val iter = instances.iterator()
        while (iter.hasNext()) {
            val instance = iter.next() ?: continue
            if (instance.world != mc.world) {
                iter.remove()
                continue
            }
            if (instance.isDead) continue
            if ((!instance.isAddedToWorld || !instance.addedToChunk)
                    && mc.world.isBlockLoaded(instance.position, false)) {
                logger.trace("the entity: $instance is not added")
                mc.world.spawnEntity(instance)
            }
        }
    }

    private val logger = Loggers.getLogger("VehicleTrackerEntryFix")
}
