/// Copyright (c) 2020 anatawa12 and other contributors
/// This file is/was part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

package com.anatawa12.fixRtm.asm

import com.anatawa12.fixRtm.asm.config.MainConfig

@Suppress("UNUSED_PARAMETER")
object Preprocessor {
    /**
     * keep bytecode below if property named  [name] in [MainConfig] is true
     */
    @JvmStatic
    fun ifEnabled(name: String): Unit = error("can't call. must be replaced from transformer")

    /**
     * keep bytecode below if property named  [name] in [MainConfig] is false
     */
    @JvmStatic
    fun ifDisabled(name: String): Unit = error("can't call. must be replaced from transformer")

    /**
     * keep bytecode below whatever.
     */
    @JvmStatic
    fun whatever(dummy: String): Unit = error("can't call. must be replaced from transformer")

    /**
     * never keep bytecode.
     */
    @JvmStatic
    fun never(dummy: String): Unit = error("can't call. must be replaced from transformer")
}
