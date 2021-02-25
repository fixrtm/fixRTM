package com.anatawa12.fixRtm.rtm.entity.train

import com.anatawa12.fixRtm.FixRtm
import com.anatawa12.fixRtm.network.NetworkHandler
import com.anatawa12.fixRtm.network.NotifyUntracked
import jp.ngt.rtm.entity.train.EntityBogie

@Suppress("EXTENSION_SHADOWED_BY_MEMBER", "unused")
fun EntityBogie.onRemovedFromWorld() {
    if (FixRtm.serverHasFixRTM)
        NetworkHandler.sendPacketServer(NotifyUntracked(entityId))
}
