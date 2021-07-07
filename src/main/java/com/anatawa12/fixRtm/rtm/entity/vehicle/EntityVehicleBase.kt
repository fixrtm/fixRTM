package com.anatawa12.fixRtm.rtm.entity.vehicle

import com.anatawa12.fixRtm.addEntityCrashInfoAboutModelSet
import com.anatawa12.fixRtm.network.NetworkHandler
import com.anatawa12.fixRtm.network.NotifyUntracked
import jp.ngt.rtm.entity.vehicle.EntityVehicleBase
import net.minecraft.crash.CrashReportCategory

@Suppress("EXTENSION_SHADOWED_BY_MEMBER", "unused")
fun EntityVehicleBase<*>.onRemovedFromWorld() {
    NetworkHandler.sendPacketServer(NotifyUntracked(entityId))
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER", "unused")
fun EntityVehicleBase<*>.addEntityCrashInfo(category: CrashReportCategory) =
    addEntityCrashInfoAboutModelSet(category) { resourceState?.resourceSet?.config }
