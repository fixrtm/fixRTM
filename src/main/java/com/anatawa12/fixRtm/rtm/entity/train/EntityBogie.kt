package com.anatawa12.fixRtm.rtm.entity.train

import jp.ngt.rtm.entity.train.EntityBogie

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
fun EntityBogie.onRemovedFromWorld() {
    println("EntityBogie#onRemovedFromWorld: ${world.isRemote}")
}
