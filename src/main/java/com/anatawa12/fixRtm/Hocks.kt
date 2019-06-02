@file:Suppress("unused") // Used with Transform

package com.anatawa12.fixRtm

import jp.ngt.ngtlib.event.TickProcessEntry
import jp.ngt.rtm.modelpack.ResourceType
import jp.ngt.rtm.modelpack.modelset.ResourceSet
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.PrintStream
import java.lang.Exception
import java.nio.channels.InterruptedByTimeoutException
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.zip.GZIPOutputStream
import kotlin.concurrent.thread
import kotlin.math.log

object ExModelPackManager {
    var dummyMap: Map<String, ResourceSet<*>>
        get() = error("impl in gen")
        set(v) = error("impl in gen")

    @Suppress("UNCHECKED_CAST")
    val allModelSetMap: MutableMap<ResourceType<*, *>, MutableMap<String, ResourceSet<*>>>
            = jp.ngt.rtm.modelpack.ModelPackManager::class.java.getDeclaredField("allModelSetMap")
            .apply { isAccessible = true }
            .get(ModelPackManager) as MutableMap<ResourceType<*, *>, MutableMap<String, ResourceSet<*>>>
}

fun eraseNullForModelSet(inSet: ResourceSet<*>?, type: ResourceType<*, *>): ResourceSet<*> {
    if (inSet != null) return inSet
    if (type.hasSubType) {
        return ExModelPackManager.dummyMap[type.name]
                ?: (ExModelPackManager.dummyMap[type.subType])
                ?: error("ResourceType(${type.name}) and ResourceType(${type.subType}) don't have dummy ResourceSet")
    } else {
        return ExModelPackManager.dummyMap[type.name]
                ?: error("ResourceType(${type.name}) don't have dummyMap")
    }
}

private fun Any?.defaultToString(): String = if (this == null) {
    "null"
} else {
    this.javaClass.name + "@" + Integer.toHexString(System.identityHashCode(this))
}

fun preProcess(entry: TickProcessEntry?) {
}

fun postProcess(isEnd: Boolean) {
}

fun postProcess() {
}

fun preProcess() {
}
fun eraseNullForAddTickProcessEntry(addEntry: TickProcessEntry?, inEntry: TickProcessEntry?) {

    requireNotNull(inEntry) {
        "TickProcessQueue.add's first argument is null. fixRtm (made by anataqa12) found a bug! this is a bug from RTM and anatawa12 think this is good trace for fix bug."
    }
}
