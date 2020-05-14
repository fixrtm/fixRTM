@file:JvmName("EntityBogieKt")

package com.anatawa12.fixRtm.rtm.entity.train

import com.anatawa12.fixRtm.VehicleTrackerEntryFix
import jp.ngt.rtm.entity.train.EntityBogie

fun EntityBogie.constructor() {
    VehicleTrackerEntryFix.addInstance(this)
}
