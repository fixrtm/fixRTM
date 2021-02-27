package com.anatawa12.fixRtm.rtm.entity.vehicle

import jp.ngt.rtm.entity.train.EntityBogie
import jp.ngt.rtm.entity.vehicle.EntityVehicleBase
import jp.ngt.rtm.entity.vehicle.VehicleTrackerEntry
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityTrackerEntry

fun newEntityTrackerEntry(
    entityIn: Entity,
    rangeIn: Int,
    maxRangeIn: Int,
    updateFrequencyIn: Int,
    sendVelocityUpdatesIn: Boolean,
): EntityTrackerEntry {
    if (entityIn is EntityBogie)
        return VehicleTrackerEntry(entityIn)
    if (entityIn is EntityVehicleBase<*>)
        return VehicleTrackerEntry(entityIn)
    return EntityTrackerEntry(entityIn, rangeIn, maxRangeIn, updateFrequencyIn, sendVelocityUpdatesIn)
}
