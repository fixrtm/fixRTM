package com.anatawa12.fixRtm

import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

fun getThreadGroup() = System.getSecurityManager()?.threadGroup ?: Thread.currentThread().threadGroup!!

fun threadFactoryWithPrefix(prefix: String, group: ThreadGroup = getThreadGroup()) = object : ThreadFactory {
    private val threadNumber = AtomicInteger(1)

    override fun newThread(r: Runnable?): Thread? {
        val t = Thread(group, r,
                "$prefix-" + threadNumber.getAndIncrement(),
                0)
        if (t.isDaemon) t.isDaemon = false
        if (t.priority != Thread.NORM_PRIORITY) t.priority = Thread.NORM_PRIORITY
        return t
    }
}

fun <E> List<E?>.isAllNotNull(): Boolean = all { it != null }
fun <E> Array<E?>.isAllNotNull(): Boolean = all { it != null }
