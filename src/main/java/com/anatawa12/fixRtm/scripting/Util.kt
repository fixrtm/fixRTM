package com.anatawa12.fixRtm.scripting

import org.mozilla.javascript.Context


inline fun <T> usingContext(script: (Context) -> T): T{
    val context = Context.enter()
    return try {
        script(context)
    } finally {
        Context.exit()
    }
}
