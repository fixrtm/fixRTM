package com.anatawa12.fixRtm

import com.anatawa12.fixRtm.asm.config.MainConfig
import com.anatawa12.fixRtm.asm.config.MainConfig.changeTestTrainTextureEnabled
import com.anatawa12.fixRtm.io.FIXFileLoader
import com.anatawa12.fixRtm.network.NetworkHandler
import com.anatawa12.fixRtm.ngtlib.renderer.model.CachedPolygonModel
import com.anatawa12.fixRtm.rtm.modelpack.modelset.dummies.*
import com.anatawa12.fixRtm.scripting.ExecutedScriptCache
import com.anatawa12.fixRtm.scripting.RhinoHooks
import jp.ngt.ngtlib.NGTCore
import jp.ngt.rtm.RTMCore
import net.minecraft.block.Block
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.IReloadableResourceManager
import net.minecraft.item.Item
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.ModelRegistryEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.client.registry.ClientRegistry
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.ModMetadata
import net.minecraftforge.fml.common.SidedProxy
import net.minecraftforge.fml.common.event.FMLConstructionEvent
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.network.NetworkCheckHandler
import net.minecraftforge.fml.relauncher.Side
import org.mozilla.javascript.NativeJavaObject
import java.awt.Color
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import kotlin.math.max

@Mod(modid = FixRtm.MODID, dependencies = "required:${RTMCore.MODID}@${RTMCore.VERSION};required:${NGTCore.MODID}@${NGTCore.VERSION};")
object FixRtm {
    const val MODID = "fix-rtm"
    lateinit var modMetadata: ModMetadata
            private set

    @Mod.EventHandler
    fun construct(e: FMLConstructionEvent) {
        NativeJavaObject.NOT_FOUND// load
        RhinoHooks// load
        FIXFileLoader // init
        if (!MainConfig.cachedPolygonModel) CachedPolygonModel // init
        if (!MainConfig.cachedScripts) ExecutedScriptCache // init
        if (e.side == Side.CLIENT) registerGenerators()
    }

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

    @SubscribeEvent
    fun tick(e: TickEvent.ClientTickEvent) {
        if (e.phase == TickEvent.Phase.END)
            proxy.tick()
    }

    @field:SidedProxy(clientSide = "com.anatawa12.fixRtm.ClientProxy", serverSide = "com.anatawa12.fixRtm.ServerProxy")
    lateinit var proxy: CommonProxy

    @Mod.InstanceFactory
    @JvmStatic
    fun get(): FixRtm {
        return FixRtm
    }
}
