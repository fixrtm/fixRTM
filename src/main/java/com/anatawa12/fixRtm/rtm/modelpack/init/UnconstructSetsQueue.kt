/// Copyright (c) 2021 anatawa12 and other contributors
/// This file is part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

package com.anatawa12.fixRtm.rtm.modelpack.init

import jp.ngt.rtm.modelpack.modelset.ResourceSet
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger

class UnconstructSetsQueue {
    private val count = AtomicInteger()
    private val backed = ConcurrentLinkedQueue<ResourceSet<*>>()

    @get:JvmName("size")
    val size get() = count.get()

    fun add(resourceSet: ResourceSet<*>) {
        count.getAndIncrement()
        backed.add(resourceSet)
    }

    fun poll(): ResourceSet<*>? = backed.poll()

    fun clear() {
    }
}
