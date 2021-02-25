package com.anatawa12.fixRtm.scripting.sai

import com.anatawa12.fixRtm.Loggers
import com.anatawa12.fixRtm.fixCacheDir
import com.anatawa12.fixRtm.mkParent
import com.anatawa12.fixRtm.utils.DigestUtils
import com.anatawa12.sai.CompilerEnvirons
import com.anatawa12.sai.Context
import com.anatawa12.sai.Script
import com.anatawa12.sai.optimizer.ClassCompiler
import com.anatawa12.sai.tools.ToolErrorReporter
import java.io.IOException
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object ScriptCompiledClassCache {
    private val compiledClasses = fixCacheDir.resolve("script-compiled-class")

    private val reporter = ToolErrorReporter(true)
    private val compilerEnv = CompilerEnvirons()
    private val compiler = ClassCompiler(compilerEnv)
    private val compileds = ConcurrentHashMap<String, ByteArray>()
    private val basePackage = "com.anatawa12.fixRtm.scripting.compiled.c_"

    init {
        compilerEnv.errorReporter = reporter
        compilerEnv.optimizationLevel = 9
    }

    fun compile(source: String, filename: String): Script {
        val className = getClassName(source, filename)
        try {
            return Loader.loadClass(className).newInstance() as Script
        } catch (e: ClassNotFoundException) {
        } catch (e: NoClassDefFoundError) {
        } catch (e: ClassCastException) {
        }
        processCompiled(compiler.compileToClassFiles(source, filename, 1, className)).also {
            compileds.putAll(it)
            for ((name, classFile) in it) {
                val hashPart = name.replace('.', '/')
                compiledClasses.resolve("$hashPart.class").mkParent().writeBytes(classFile)
            }
        }

        val clazz = Loader.loadClass(className)
        check(Script::class.java.isAssignableFrom(clazz)) { "compiled code is not Script type" }
        return clazz.newInstance() as Script
    }

    private fun processCompiled(compileToClassFiles: Array<Any>) = compileToClassFiles
        .asSequence()
        .chunked(2)
        .map { (name, code) -> name as String to code as ByteArray }
        .toMap()

    private fun getClassName(source: String, filename: String): String {
        return basePackage + DigestUtils.sha1Hex(source)
    }

    fun initContext(it: Context) {
        it.applicationClassLoader = Loader
    }

    object Loader : ClassLoader(Loader::class.java.classLoader) {
        val invalids = Collections.newSetFromMap<String>(ConcurrentHashMap())
        override fun loadClass(name: String?, resolve: Boolean): Class<*> {
            if (name in invalids)
                throw ClassNotFoundException(name)
            val loaded = super.loadClass(name, resolve)
            if (loaded.name != name) {
                invalids.add(name)
                Loggers.getLogger("ScriptCompiledClassCache.Loader").trace("loaded.name != name: $name")
                throw ClassNotFoundException(name)
            }
            return loaded
        }

        override fun findClass(name: String): Class<*> {
            val hashPart = name.replace('.', '/')
            val bytes = compileds[name] ?: try {
                compiledClasses.resolve("$hashPart.class").readBytes()
            } catch (e: IOException) {
                throw ClassNotFoundException(name)
            }
            val clazz = try {
                defineClass(name, bytes, 0, bytes.size)
            } catch (e: ClassFormatError) {
                throw ClassNotFoundException(name)
            }
            if (Script::class.java.isAssignableFrom(clazz)) {
                loadScriptClass(clazz as Class<out Script>)
            }
            return clazz
        }

        private fun loadScriptClass(clazz: Class<out Script>) {
            try {
                val m = clazz.getDeclaredMethod("_reInit", Context::class.java)
                m.isAccessible = true
                usingContext { m.invoke(null, it) }
            } catch (e: NoSuchMethodException) {
                // no method is ignore
            }
        }
    }
}

