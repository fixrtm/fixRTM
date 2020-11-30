package com.anatawa12.fixRtm.scripting.sai

import com.anatawa12.sai.Context
import com.anatawa12.sai.ContextFactory

inline fun <T> usingContext(script: (Context) -> T): T{
    var oldContext: Context? = null
    return try {
        oldContext = Context.getCurrentContext()
        if (oldContext != null && oldContext.factory != FixRTMContextFactory)
            Context.exit()
        val context = FixRTMContextFactory.enterContext()
        script(context)
    } finally {
        Context.exit()
        if (oldContext != null && oldContext.factory != FixRTMContextFactory)
            oldContext.factory.enterContext(oldContext)
    }
}

object FixRTMContextFactory : ContextFactory() {
    override fun hasFeature(cx: Context?, featureIndex: Int): Boolean {
        return when (featureIndex) {
            Context.FEATURE_NATIVE_PRIMITIVES_HAVE_JAVA_METHODS -> true
            Context.FEATURE_FUNCTION_TO_STRING_RETURN_REAL_SOURCE -> true
            Context.FEATURE_E4X -> false
            else -> super.hasFeature(cx, featureIndex)
        }
    }
}
