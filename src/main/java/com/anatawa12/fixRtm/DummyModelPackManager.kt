package com.anatawa12.fixRtm

import com.anatawa12.fixRtm.asm.config.MainConfig.dummyModelPackEnabled
import jp.ngt.ngtlib.util.NGTUtil
import jp.ngt.rtm.modelpack.ModelPackManager
import jp.ngt.rtm.modelpack.ResourceType
import jp.ngt.rtm.modelpack.cfg.ResourceConfig
import jp.ngt.rtm.modelpack.modelset.ResourceSet
import net.minecraftforge.fml.common.FMLCommonHandler
import java.lang.reflect.Constructor
import java.util.*

object DummyModelPackManager {
    private val modelSetMap = WeakHashMap<ResourceType<*, *>, MutableMap<String, ResourceSet<*>>>()
    private val dummyClassMap = mutableMapOf<Class<out ResourceSet<*>>, ModelPackCreator<*, *>>()

    @Suppress("UNCHECKED_CAST")
    private fun <S : ResourceSet<C>, C : ResourceConfig> getModelSetMap(type: ResourceType<C, S>): MutableMap<String, S> {
        return modelSetMap.getOrPut(type, ::mutableMapOf) as MutableMap<String, S>
    }

    fun <S : ResourceSet<C>, C : ResourceConfig> registerResourceSet(type: ResourceType<C, S>, name: String): S {
        @Suppress("UNCHECKED_CAST")
        val creator = requireNotNull(dummyClassMap[type.setClass] as? ModelPackCreator<S, C>) {
            "there is not dummy class for ${type.setClass}"
        }
        val dummySet = creator.make(name, type)
        require(type.setClass.isInstance(dummySet)) { "dummy class is not valid instance. type mismatch" }
        getModelSetMap(type)[name] = dummySet
        if (FMLCommonHandler.instance().side.isClient) {
            dummySet.constructOnClient()
        } else {
            dummySet.constructOnServer()
        }
        return dummySet
    }

    @JvmStatic
    fun <S : ResourceSet<C>, C : ResourceConfig> getSet(type: ResourceType<C, S>, name: String): S {
        if (!useDummyModelSet()) return ModelPackManager.INSTANCE.dummyMap[type.subType ?: type.name]!! as S
        return getModelSetMap(type)[name] ?: registerResourceSet(type, name)
    }

    fun <S : ResourceSet<C>, C : ResourceConfig> registerDummyClass(creator: ModelPackCreator<S, C>) {
        @Suppress("UNCHECKED_CAST")
        dummyClassMap[creator.dummyFor] = creator
    }

    fun <S : ResourceSet<C>, C : ResourceConfig> registerDummyClass(dummyClass: Class<S>) {
        @Suppress("UNCHECKED_CAST")
        val dummyFor = dummyClass.superclass as Class<out ResourceSet<C>>
        require(ResourceSet::class.java.isAssignableFrom(dummyFor)) { "dummyClass is not dummy ModelSet class" }

        val constructor = dummyClass.getConstructor(String::class.java) as Constructor<out S>
        dummyClassMap[dummyFor] = object : ModelPackCreator<S, C> {
            override val dummyFor: Class<out ResourceSet<C>> get() = dummyFor

            override fun make(name: String, type: ResourceType<C, S>): S = constructor.newInstance(name)
        }
    }

    fun getDummyName(id: String) = "dummy_${id}_dummy_anatawa12"

    interface ModelPackCreator<S : ResourceSet<C>, C : ResourceConfig> {
        val dummyFor: Class<out ResourceSet<C>>
        fun make(name: String, type: ResourceType<C, S>): S
    }

    // gotAllModels

    var gotAllModels = true

    fun useDummyModelSet(): Boolean {
        if (!dummyModelPackEnabled) return false // if disabled
        if (NGTUtil.isServer()) return true // if server
        if (!NGTUtil.isSMP()) return true // if single player
        if (!FixRtm.serverHasFixRTM) return true // if server does not use fixrtm
        if (gotAllModels) return true // if got all model name
        return false
    }
}
