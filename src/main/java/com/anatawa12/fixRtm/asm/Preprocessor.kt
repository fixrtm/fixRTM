package com.anatawa12.fixRtm.asm

import com.anatawa12.fixRtm.asm.config.MainConfig

object Preprocessor {
    /**
     * keep bytecode below if property named  [name] in [MainConfig] is true
     */
    @JvmStatic fun ifEnabled(name: String): Unit = error("can't call. must be replaced from transformer")
    /**
     * keep bytecode below if property named  [name] in [MainConfig] is false
     */
    @JvmStatic fun ifDisabled(name: String): Unit = error("can't call. must be replaced from transformer")
    /**
     * keep bytecode below whatever.
     */
    @JvmStatic fun whatever(dummy: String): Unit = error("can't call. must be replaced from transformer")
}
