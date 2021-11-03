package com.anatawa12.fixRtm.ngtlib.io

import com.anatawa12.fixRtm.utils.ThreadLocalProperties.Companion.removeLocalSystemProperty

object ScriptUtil {
    fun prepareSystemProperty() {
        removeLocalSystemProperty("nashorn.args.prepend")
        removeLocalSystemProperty("nashorn.args")
    }
}
