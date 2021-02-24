package com.anatawa12.fixRtm.rtm.entity.vehicle

import jp.ngt.rtm.entity.vehicle.EntityVehicleBase

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
fun EntityVehicleBase<*>.onRemovedFromWorld() {
    println("EntityVehicleBase#onRemovedFromWorld: ${world.isRemote}")
}
