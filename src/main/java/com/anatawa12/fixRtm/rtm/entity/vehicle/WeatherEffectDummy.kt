@file:JvmName("WeatherEffectDummyKt")

package com.anatawa12.fixRtm.rtm.entity.vehicle

import jp.ngt.rtm.entity.vehicle.EntityVehicleBase
import jp.ngt.rtm.entity.vehicle.WeatherEffectDummy

fun WeatherEffectDummy.shouldDead(parent: EntityVehicleBase<*>): Boolean {
    if (this.parent.isDead) return true
    if (world.getEntityByID(parent.entityId) == null) {
        println("WeatherEffectDummy.shouldDead return true with world.getEntityByID(parent.entityId) == null")
        return true
    }
    return false
}
