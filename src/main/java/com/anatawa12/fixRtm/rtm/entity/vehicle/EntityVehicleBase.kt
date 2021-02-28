package com.anatawa12.fixRtm.rtm.entity.vehicle

import com.anatawa12.fixRtm.FixRtm
import com.anatawa12.fixRtm.network.NetworkHandler
import com.anatawa12.fixRtm.network.NotifyUntracked
import jp.ngt.rtm.entity.vehicle.EntityVehicleBase
import net.minecraft.crash.CrashReportCategory

@Suppress("EXTENSION_SHADOWED_BY_MEMBER", "unused")
fun EntityVehicleBase<*>.onRemovedFromWorld() {
    if (FixRtm.serverHasFixRTM)
        NetworkHandler.sendPacketServer(NotifyUntracked(entityId))
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER", "unused")
fun EntityVehicleBase<*>.addEntityCrashInfo(category: CrashReportCategory) = try {
    val cfg = resourceState.resourceSet.config
    category.addCrashSection("ModelSet Name", cfg.name)
    category.addCrashSection("ModelSet Source JSON Path", cfg.file ?: "no source")
} catch (t: Throwable) {
    category.addCrashSectionThrowable("Error Getting ModelSet", t)
}
