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
