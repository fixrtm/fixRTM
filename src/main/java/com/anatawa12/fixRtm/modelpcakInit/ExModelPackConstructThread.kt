package com.anatawa12.fixRtm.modelpcakInit

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

class ExModelPackConstructThread(val threadSide: Side, val parent: ModelPackLoadThread) : ModelPackConstructThread(threadSide, parent) {
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
            if (threadSide == Side.CLIENT) {
                var crashReport: CrashReport = CrashReport.makeCrashReport(throwable, "Constructing RTM ModelPack")
                crashReport.makeCategory("Initialization")
                crashReport = NGTUtilClient.getMinecraft().addGraphicsAndWorldToCrashReport(crashReport)
                NGTUtilClient.getMinecraft().displayCrashReport(crashReport)
            } else {
                throw throwable
            }
        }
    }

    override fun run() {
        runWithCrashReport {
            runThread()
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

        val exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())

        val futures = mutableListOf<Future<*>>()

        var index = 0
        while (this.loading) {
            while (index < unConstructSets.size) {
                val indexNew = index
                futures += exec.submit {
                    runWithCrashReport {
                        construct(unConstructSets[indexNew])
                    }
                }
                index++
            }
            Thread.sleep(500L)
        }

        for (future in futures) {
            future.get()
        }
    }

    private fun construct(set: ResourceSet<*>) {
        if (threadSide == Side.SERVER) {
            set.constructOnServer()
        } else {
            set.constructOnClient()
            set.finishConstruct()
        }
        val index = index.incrementAndGet()
        lastLoadedModelName = set.config.name
        NGTLog.debug("Construct Model : %s (%d / %d)", set.config.name, index, unConstructSets.size)
    }


    override fun setFinish(): Boolean {
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
