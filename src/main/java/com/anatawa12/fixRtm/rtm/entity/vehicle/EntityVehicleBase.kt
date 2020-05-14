@file:JvmName("EntityVehicleBaseKt")

package com.anatawa12.fixRtm.rtm.entity.vehicle

import com.anatawa12.fixRtm.VehicleTrackerEntryFix
import jp.ngt.rtm.entity.vehicle.EntityVehicleBase

fun EntityVehicleBase<*>.constructor() {
    VehicleTrackerEntryFix.addInstance(this)
}
