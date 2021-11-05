/// Copyright (c) 2019 anatawa12 and other contributors
/// This file is part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

package com.anatawa12.fixRtm

import com.anatawa12.fixRtm.asm.config.MainConfig
import com.anatawa12.fixRtm.asm.config.MainConfig.changeTestTrainTextureEnabled
import com.anatawa12.fixRtm.crash.RTMAllModelPackInfoCrashCallable
import com.anatawa12.fixRtm.crash.RTMSmallModelPackInfoCrashCallable
import com.anatawa12.fixRtm.gui.GuiHandler
import com.anatawa12.fixRtm.io.FIXFileLoader
import com.anatawa12.fixRtm.network.NetworkHandler
import com.anatawa12.fixRtm.rtm.modelpack.modelset.dummies.*
import com.anatawa12.fixRtm.scripting.loadFIXScriptUtil
import com.anatawa12.fixRtm.scripting.nashorn.CompiledImportedScriptCache
import com.anatawa12.fixRtm.scripting.sai.ExecutedScriptCache
import com.anatawa12.fixRtm.utils.ThreadLocalProperties
import jp.ngt.ngtlib.NGTCore
import jp.ngt.rtm.RTMCore
import net.minecraft.block.Block
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.IReloadableResourceManager
import net.minecraft.crash.CrashReport
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.item.Item
import net.minecraft.launchwrapper.Launch
import net.minecraft.util.ReportedException
import net.minecraft.util.ResourceLocation
import net.minecraft.util.text.Style
import net.minecraft.util.text.TextComponentString
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.client.event.ModelRegistryEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.client.registry.ClientRegistry
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.ModMetadata
import net.minecraftforge.fml.common.event.FMLConstructionEvent
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.PlayerEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.network.NetworkRegistry
import net.minecraftforge.fml.relauncher.Side
import paulscode.sound.SoundSystemConfig
import java.awt.Color
import java.awt.image.BufferedImage
import java.util.concurrent.atomic.AtomicReference
import javax.imageio.ImageIO
import kotlin.math.max

@Mod(modid = FixRtm.MODID,
    dependencies = "required:${RTMCore.MODID}@${RTMCore.VERSION};required:${NGTCore.MODID}@${NGTCore.VERSION};")
object FixRtm {
    const val MODID = "fix-rtm"
    lateinit var modMetadata: ModMetadata
        private set

    lateinit var metadata: ModMetadata
        private set
    val VERSION
        get() = metadata.version!!

    @Mod.EventHandler
    fun construct(e: FMLConstructionEvent) {
        FIXFileLoader.load() // init
        ImageIO.scanForPlugins() // load webp-imageio
        if (MainConfig.addModelPackInformationInAllCrashReports)
            FMLCommonHandler.instance().registerCrashCallable(RTMAllModelPackInfoCrashCallable)
        else
            FMLCommonHandler.instance().registerCrashCallable(RTMSmallModelPackInfoCrashCallable)
        if (MainConfig.useThreadLocalProperties)
            System.setProperties(ThreadLocalProperties().apply { putAll(System.getProperties()) })
        Launch.classLoader.addClassLoaderExclusion("jdk.nashorn.")
        when (MainConfig.scriptingMode) {
            MainConfig.ScriptingMode.CacheWithSai -> {
                loadFIXScriptUtil()// init
                ExecutedScriptCache.load()// init
            }
            MainConfig.ScriptingMode.BetterWithNashorn -> {
                CompiledImportedScriptCache.load() // load
            }
            MainConfig.ScriptingMode.UseRtmNormal -> {
                // nop
            }
        }
        if (e.side == Side.CLIENT) registerGenerators()
        println(Any::class)
        //FixRtm::class.members.isEmpty()
    }

    @Mod.EventHandler
    fun preInit(e: FMLPreInitializationEvent) {
        metadata = e.modMetadata
        DummyModelPackManager.registerDummyClass(DummyModelSetConnector::class.java)
        DummyModelPackManager.registerDummyClass(DummyModelSetContainer::class.java)
        DummyModelPackManager.registerDummyClass(DummyModelSetFirearm::class.java)
        DummyModelPackManager.registerDummyClass(DummyModelSetNPC::class.java)
        DummyModelPackManager.registerDummyClass(DummyModelSetRail::class.java)
        DummyModelPackManager.registerDummyClass(DummyModelSetSignal::class.java)
        DummyModelPackManager.registerDummyClass(DummyModelSetTrain::class.java)
        DummyModelPackManager.registerDummyClass(DummyModelSetVehicle::class.java)
        DummyModelPackManager.registerDummyClass(DummyModelSetWire::class.java)
        DummyModelPackManager.registerDummyClass(DummyModelSetMechanism::class.java)
        DummyModelPackManager.registerDummyClass(DummyTextureSetRRS::class.java)
        DummyModelPackManager.registerDummyClass(DummyTextureSetSignboard::class.java)
        DummyModelPackManager.registerDummyClass(DummyTextureSetFlag::class.java)

        DummyModelPackManager.registerDummyClass(DummyModelSetOrnament)
        DummyModelPackManager.registerDummyClass(DummyModelSetMachine)
        MinecraftForge.EVENT_BUS.register(this)
        MinecraftForge.EVENT_BUS.register(ThreadUtil)
        modMetadata = e.modMetadata
        NetworkHandler.init()
        PermissionManager.registerBuiltinPermissions()
        NetworkRegistry.INSTANCE.registerGuiHandler(this, GuiHandler())

        if (e.side == Side.CLIENT && MainConfig.expandPlayableSoundCount) {
            SoundSystemConfig.setNumberNormalChannels(1024)
            SoundSystemConfig.setNumberStreamingChannels(32)
        }
    }

    private val thrownMarker = CrashReport("", Throwable())
    private val crashReportHolder = AtomicReference<CrashReport?>(null)

    internal fun reportCrash(report: CrashReport) {
        // fail if after crashing other thread
        crashReportHolder.compareAndSet(null, report)
    }

    @SubscribeEvent
    fun onServerTick(@Suppress("UNUSED_PARAMETER") e: TickEvent.ServerTickEvent) {
        val report = crashReportHolder.get()
        if (report === null) return
        if (report === thrownMarker) return
        crashReportHolder.set(thrownMarker)
        throw ReportedException(report)
    }

    @Mod.EventHandler
    @Suppress("UNUSED_PARAMETER")
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
    @Suppress("UNUSED_PARAMETER")
    fun registerModel(e: ModelRegistryEvent) {
        ClientRegistry.registerTileEntity(TestTileEntity::class.java, "test", TestSPRenderer)
        DummyModelObject.init()
    }


    fun registerGenerators() {
        Minecraft.getMinecraft().defaultResourcePacks.add(GeneratedResourcePack)
        Minecraft.getMinecraft().resourceManager
            .let { it as IReloadableResourceManager }
            .registerReloadListener(GeneratedResourcePack)

        if (changeTestTrainTextureEnabled) {
            GeneratedResourcePack.addImageGenerator("textures/generated/items/item_test_train.png") {
                try {
                    val baseBackImage = Minecraft.getMinecraft().resourceManager
                        .getResource(ResourceLocation("rtm:textures/items/item_ec.png"))
                        .inputStream.let { ImageIO.read(it) }
                    val testTrainText = Minecraft.getMinecraft().resourceManager
                        .getResource(ResourceLocation(MODID, "textures/template/test_train_text.png"))
                        .inputStream.let { ImageIO.read(it) }
                    val img = BufferedImage(
                        max(baseBackImage.width, testTrainText.width),
                        max(baseBackImage.height, testTrainText.height),
                        BufferedImage.TYPE_INT_ARGB)

                    val graphics = img.createGraphics()

                    graphics.drawImage(baseBackImage, 0, 0, img.width, img.height, Color(0, 0, 0, 0), null)
                    graphics.drawImage(testTrainText, 0, 0, img.width, img.height, Color(0, 0, 0, 0), null)

                    img
                } catch (e: Throwable) {
                    throw e
                }
            }
        } else {
            GeneratedResourcePack.addFileGenerator("textures/generated/items/item_test_train.png") {
                Minecraft.getMinecraft().resourceManager
                    .getResource(ResourceLocation("rtm:textures/items/item_ec.png"))
                    .inputStream.readBytes()
            }
        }
    }
    //assets/rtm/models/item/item_train_127.json
    //                       item_train_fixrtm_test

    @SubscribeEvent
    @Suppress("UNUSED_PARAMETER")
    fun onPlayerLoggedIn(e: PlayerEvent.PlayerLoggedInEvent) {
        if (!(e.player as EntityPlayerMP).hasFixRTM) {
            e.player.sendMessage(TextComponentString("In this server, fixRTM is enabled. " +
                    "It's recommended to use fixRTM even on client.")
                .setStyle(Style().setColor(TextFormatting.YELLOW)))
        }
    }


    @Mod.InstanceFactory
    @JvmStatic
    fun get(): FixRtm {
        return FixRtm
    }
}
