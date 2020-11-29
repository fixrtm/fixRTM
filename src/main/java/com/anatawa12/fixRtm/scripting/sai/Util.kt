package com.anatawa12.fixRtm.scripting.sai

import com.anatawa12.sai.Context

inline fun <T> usingContext(script: (Context) -> T): T{
    val context = Context.enter()
    return try {
        script(context)
    } finally {
        Context.exit()
    }
}
