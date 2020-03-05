package com.anatawa12.fixRtm.modelpcakInit

import jp.ngt.rtm.modelpack.init.ModelPackLoadThread
import net.minecraftforge.fml.relauncher.Side

open class ModelPackConstructThreadDummy(private val threadSide: Side, private val parent: ModelPackLoadThread) : Thread("RTM ModelPack Construct") {
    override fun run(): Unit = error("")
    open fun setFinish(): Boolean = error("")
}
