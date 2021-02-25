package com.anatawa12.fixRtm.rtm.entity.vehicle

import com.anatawa12.fixRtm.FixRtm
import com.anatawa12.fixRtm.network.NetworkHandler
import com.anatawa12.fixRtm.network.NotifyUntracked
import jp.ngt.rtm.entity.vehicle.EntityVehicleBase

@Suppress("EXTENSION_SHADOWED_BY_MEMBER", "unused")
fun EntityVehicleBase<*>.onRemovedFromWorld() {
    if (FixRtm.serverHasFixRTM)
        NetworkHandler.sendPacketServer(NotifyUntracked(entityId))
}
