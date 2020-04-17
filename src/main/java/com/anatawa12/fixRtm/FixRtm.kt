package com.anatawa12.fixRtm

import com.anatawa12.fixRtm.rtm.modelpack.modelset.dummies.*
import com.anatawa12.fixRtm.network.NetworkHandler
import jp.ngt.ngtlib.NGTCore
import jp.ngt.rtm.RTMCore
import net.minecraft.block.Block
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.IResourcePack
import net.minecraft.item.Item
import net.minecraftforge.client.event.ModelRegistryEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.client.registry.ClientRegistry
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.ModMetadata
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.network.NetworkCheckHandler
import net.minecraftforge.fml.relauncher.Side

@Mod(modid = FixRtm.MODID, dependencies = "required:${RTMCore.MODID}@${RTMCore.VERSION};required:${NGTCore.MODID}@${NGTCore.VERSION};")
object FixRtm {
    const val MODID = "fix-rtm"
    lateinit var modMetadata: ModMetadata
            private set

    @Mod.EventHandler
    fun preInit(e: FMLPreInitializationEvent) {
        DummyModelPackManager.registerDummyClass(DummyModelSetConnector::class.java)
        DummyModelPackManager.registerDummyClass(DummyModelSetContainer::class.java)
        DummyModelPackManager.registerDummyClass(DummyModelSetFirearm::class.java)
        DummyModelPackManager.registerDummyClass(DummyModelSetNPC::class.java)
        DummyModelPackManager.registerDummyClass(DummyModelSetRail::class.java)
        DummyModelPackManager.registerDummyClass(DummyModelSetSignal::class.java)
        DummyModelPackManager.registerDummyClass(DummyModelSetTrain::class.java)
        DummyModelPackManager.registerDummyClass(DummyModelSetVehicle::class.java)
        DummyModelPackManager.registerDummyClass(DummyModelSetWire::class.java)
        DummyModelPackManager.registerDummyClass(DummyTextureSetRRS::class.java)
        DummyModelPackManager.registerDummyClass(DummyTextureSetSignboard::class.java)
        DummyModelPackManager.registerDummyClass(DummyTextureSetFlag::class.java)

        DummyModelPackManager.registerDummyClass(DummyModelSetOrnament)
        DummyModelPackManager.registerDummyClass(DummyModelSetMachine)
        MinecraftForge.EVENT_BUS.register(this)
        modMetadata = e.modMetadata
        NetworkHandler.init()
    }

    @Mod.EventHandler
    fun init(e: FMLInitializationEvent) {
    }

    @SubscribeEvent
    fun registerBlock(e: RegistryEvent.Register<Block>) {
        e.registry.register(TestBlock)
    }

    @SubscribeEvent
    fun registerItem(e: RegistryEvent.Register<Item>) {
        e.registry.register(TestItem)
    }

    @SubscribeEvent
    fun registerModel(e: ModelRegistryEvent) {
        ClientRegistry.registerTileEntity(TestTileEntity::class.java, "test", TestSPRenderer)
        Minecraft.getMinecraft().defaultResourcePacks.add(ButtonResourcePack)
        DummyModelObject.init()
    }

    var serverHasFixRTM = true

    @NetworkCheckHandler
    fun networkCheck(mods: Map<String, String>, remoteSide: Side): Boolean {
        if (mods[RTMCore.MODID] != RTMCore.VERSION)
            return false
        if (remoteSide == Side.SERVER) {
            // on client
            serverHasFixRTM = mods.containsKey(MODID)
            if (serverHasFixRTM)
                DummyModelPackManager.gotAllModels = false
            return true
        } else {
            return true
        }
    }



    @Mod.InstanceFactory
    @JvmStatic
    fun get(): FixRtm {
        return FixRtm
    }
}
