package com.anatawa12.fixRtm.rtm.entity.npc

import com.anatawa12.fixRtm.addEntityCrashInfoAboutModelSet
import jp.ngt.rtm.entity.npc.EntityNPC
import net.minecraft.crash.CrashReportCategory

@Suppress("EXTENSION_SHADOWED_BY_MEMBER", "unused")
fun EntityNPC.addEntityCrashInfo(category: CrashReportCategory) =
    addEntityCrashInfoAboutModelSet(category) { resourceState?.resourceSet?.config }
