package com.anatawa12.fixRtm

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object Loggers {
    fun getLogger(name: String): Logger = LogManager.getLogger("fixrtm/$name")
}
