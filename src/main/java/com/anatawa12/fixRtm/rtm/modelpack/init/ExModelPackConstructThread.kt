/// Copyright (c) 2020 anatawa12 and other contributors
/// This file is/was part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

package com.anatawa12.fixRtm.rtm.modelpack.init

import com.anatawa12.fixRtm.FixRtm
import com.anatawa12.fixRtm.asm.Preprocessor
import com.anatawa12.fixRtm.asm.config.MainConfig
import com.anatawa12.fixRtm.asm.config.MainConfig.ModelPackLoadSpeed.*
import com.anatawa12.fixRtm.asm.config.MainConfig.modelPackLoadSpeed
import com.anatawa12.fixRtm.rtm.modelpack.ModelState
import com.anatawa12.fixRtm.threadFactoryWithPrefix
import jp.ngt.ngtlib.io.NGTLog
import jp.ngt.ngtlib.util.NGTUtilClient
import jp.ngt.rtm.modelpack.ModelPackManager
import jp.ngt.rtm.modelpack.init.ModelPackConstructThread
import jp.ngt.rtm.modelpack.init.ModelPackLoadThread
import jp.ngt.rtm.modelpack.init.ProgressStateHolder.ProgressState
import jp.ngt.rtm.modelpack.modelset.ResourceSet
import net.minecraft.crash.CrashReport
import net.minecraftforge.fml.relauncher.Side
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicInteger

class ExModelPackConstructThread(val threadSide: Side, val parent: ModelPackLoadThread) :
    ModelPackConstructThread(threadSide, parent) {
    @Volatile
    private var loading = true
    private var index = AtomicInteger(0)

    @Volatile
    private var lastLoadedModelName: String = ""

    private val unConstructSets get() = ModelPackManager.INSTANCE.unconstructSets

    private inline fun runWithCrashReport(block: () -> Unit) {
        try {
            block()
        } catch (throwable: Throwable) {
            var crashReport: CrashReport = CrashReport.makeCrashReport(throwable, "Constructing RTM ModelPack")
            crashReport.makeCategory("Initialization")
            if (threadSide == Side.CLIENT) {
                crashReport = NGTUtilClient.getMinecraft().addGraphicsAndWorldToCrashReport(crashReport)
                NGTUtilClient.getMinecraft().displayCrashReport(crashReport)
            } else {
                FixRtm.reportCrash(crashReport)
            }
        }
    }

    override fun run() {
        if (modelPackLoadSpeed != UseOriginal) {
            runWithCrashReport {
                runThread()
            }
        } else {
            super.run()
        }
    }

    @Throws(InterruptedException::class)
    private fun runThread() {

        val guiUpdateThread = Thread {
            var barStateChanged = false
            while (this.loading) {
                if (this.parent.loadFinished) {
                    if (!barStateChanged) {
                        this.parent.setBarValue(0, ProgressState.CONSTRUCTING_MODEL)
                        val size = unConstructSets.size
                        this.parent.setBarMaxValue(1, size, "")
                        barStateChanged = true
                    }
                    this.parent.setBarValue(1, this.index.get(), lastLoadedModelName)
                }
                Thread.sleep(50L)
            }
        }
        guiUpdateThread.start()

        val exec = when (modelPackLoadSpeed) {
            UseOriginal -> error("must be erased")
            SingleThreaded -> Executors.newSingleThreadExecutor(
                threadFactoryWithPrefix("fixrtm-ModelPackConstruct-pool"))
            MultiThreaded -> Executors.newFixedThreadPool(
                (Runtime.getRuntime().availableProcessors() / 3).coerceAtLeast(1),
                threadFactoryWithPrefix("fixrtm-ModelPackConstruct-pool"))
            WorkStealing -> Executors.newWorkStealingPool()
        }

        val futures = mutableListOf<Future<*>>()

        while (this.loading) {
            while (true) {
                val resourceSet = unConstructSets.poll() ?: break
                futures += exec.submit {
                    runWithCrashReport {
                        construct(resourceSet)
                    }
                }
            }
            Thread.sleep(500L)
        }

        for (future in futures) {
            future.get()
        }

        exec.shutdown()
    }

    private fun construct(set: ResourceSet<*>) {
        try {
            if (threadSide == Side.SERVER) {
                set.constructOnServer()
            } else {
                set.constructOnClient()
                set.finishConstruct()
            }
            set.state = ModelState.CONSTRUCTED
            val index = index.incrementAndGet()
            lastLoadedModelName = set.config.name
            if (!MainConfig.reduceConstructModelLog) {
                NGTLog.debug("Construct Model : %s (%d / %d)", set.config.name, index, unConstructSets.size)
            } else {
                NGTLog.trace("Construct Model : %s (%d / %d)", set.config.name, index, unConstructSets.size)
            }
        } catch (throwable: Throwable) {
            if (set.config.file == null) {
                throw ModelConstructingException("constructing resource: ${set.config.name} (unknown source file)",
                    throwable)
            } else {
                throw ModelConstructingException("constructing resource: ${set.config.name} (source file: ${set.config.file})",
                    throwable)
            }
        }
    }


    override fun setFinish(): Boolean {
        if (modelPackLoadSpeed == UseOriginal) return super.setFinish()
        return if (ModelPackManager.INSTANCE.unconstructSets.size == this.index.get()) {
            ModelPackManager.INSTANCE.unconstructSets.clear()
            ModelPackManager.INSTANCE.clearCache()
            this.loading = false
            true
        } else {
            false
        }
    }
}
