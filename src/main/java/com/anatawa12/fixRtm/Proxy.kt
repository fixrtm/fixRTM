package com.anatawa12.fixRtm

import net.minecraft.client.Minecraft

abstract class CommonProxy {
    open fun tick() {}


}

class ServerProxy : CommonProxy()

class ClientProxy : CommonProxy() {
    override fun tick() {
        if (Minecraft.getMinecraft().world != null)
            VehicleTrackerEntryFix.tick()
    }
}
