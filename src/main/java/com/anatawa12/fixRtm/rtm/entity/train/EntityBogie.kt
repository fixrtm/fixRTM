package com.anatawa12.fixRtm.rtm.entity.train

import com.anatawa12.fixRtm.FixRtm
import com.anatawa12.fixRtm.network.NetworkHandler
import com.anatawa12.fixRtm.network.NotifyUntracked
import jp.ngt.rtm.entity.train.EntityBogie
import net.minecraft.crash.CrashReportCategory

@Suppress("EXTENSION_SHADOWED_BY_MEMBER", "unused")
fun EntityBogie.onRemovedFromWorld() {
    if (FixRtm.serverHasFixRTM)
        NetworkHandler.sendPacketServer(NotifyUntracked(entityId))
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER", "unused")
fun EntityBogie.addEntityCrashInfo(category: CrashReportCategory) = try {
    val cfg = train?.resourceState?.resourceSet?.config
    category.addCrashSection("Parent Train ModelSet Name",
        cfg?.name ?: "no train on client")
    category.addCrashSection("Parent Train ModelSet Source JSON Path",
        cfg?.run { file ?: "no source" } ?: "no train on client")
} catch (t: Throwable) {
    category.addCrashSectionThrowable("Error Getting ModelSet", t)
}
