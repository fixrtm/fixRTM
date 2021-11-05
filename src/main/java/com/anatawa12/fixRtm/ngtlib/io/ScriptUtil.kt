/// Copyright (c) 2021 anatawa12 and other contributors
/// This file is/was part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

package com.anatawa12.fixRtm.ngtlib.io

import com.anatawa12.fixRtm.utils.ThreadLocalProperties.Companion.removeLocalSystemProperty

object ScriptUtil {
    fun prepareSystemProperty() {
        removeLocalSystemProperty("nashorn.args.prepend")
        removeLocalSystemProperty("nashorn.args")
    }
}
