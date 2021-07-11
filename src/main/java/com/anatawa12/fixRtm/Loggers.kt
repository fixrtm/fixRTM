/// Copyright (c) 2020 anatawa12 and other contributors
/// This file is/was part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

package com.anatawa12.fixRtm

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object Loggers {
    fun getLogger(name: String): Logger = LogManager.getLogger("fixrtm/$name")
}
