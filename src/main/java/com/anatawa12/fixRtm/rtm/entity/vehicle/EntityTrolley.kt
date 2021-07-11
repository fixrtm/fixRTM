/// Copyright (c) 2021 anatawa12 and other contributors
/// This file is/was part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

package com.anatawa12.fixRtm.rtm.entity.vehicle

import com.anatawa12.fixRtm.addEntityCrashInfoAboutModelSet
import jp.ngt.rtm.entity.vehicle.EntityTrolley
import net.minecraft.crash.CrashReportCategory

@Suppress("EXTENSION_SHADOWED_BY_MEMBER", "unused")
fun EntityTrolley.addEntityCrashInfo(category: CrashReportCategory) =
    addEntityCrashInfoAboutModelSet(category) { resourceState?.resourceSet?.config }
