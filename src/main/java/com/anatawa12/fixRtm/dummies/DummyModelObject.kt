package com.anatawa12.fixRtm.dummies

import com.anatawa12.fixRtm.DummyModelPackManager
import com.anatawa12.fixRtm.drawCenterString
import jp.ngt.ngtlib.io.FileType
import jp.ngt.ngtlib.renderer.model.GroupObject
import jp.ngt.ngtlib.renderer.model.IModelNGT
import jp.ngt.ngtlib.renderer.model.Material
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.Vec3d
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL12
import java.awt.image.BufferedImage
import java.nio.ByteBuffer
import java.nio.IntBuffer
import java.nio.ShortBuffer
import javax.imageio.ImageIO

class DummyModelObject(val aabb: AxisAlignedBB,
                       val name: String,
                       val drawFaces: Set<EnumFacing>,
                       val rotate: Vec3d = Vec3d.ZERO,
                       val rotate1: Double = 0.0,
                       val nameOnly: Boolean = false) : IModelNGT {
    override fun getMaterials() = Companion.materials

    override fun renderAll(smoothing: Boolean) {
        GL11.glPushMatrix()
        if (smoothing) {
            GL11.glShadeModel(GL11.GL_SMOOTH)
        }
        GL11.glRotated(rotate1, rotate.x, rotate.y, rotate.z)

        val renderer = Minecraft.getMinecraft().fontRenderer

        aabb.apply {
            Minecraft.getMinecraft().renderEngine.bindTexture(resourceLocation)

            if (!nameOnly) {
                GL11.glBegin(GL11.GL_QUADS)

                GL11.glTexCoord2d(1.0, 0.0); GL11.glVertex3d(aabb.maxX, aabb.minY, aabb.minZ)
                GL11.glTexCoord2d(1.0, 1.0); GL11.glVertex3d(aabb.maxX, aabb.minY, aabb.maxZ)
                GL11.glTexCoord2d(0.0, 1.0); GL11.glVertex3d(aabb.minX, aabb.minY, aabb.maxZ)
                GL11.glTexCoord2d(0.0, 0.0); GL11.glVertex3d(aabb.minX, aabb.minY, aabb.minZ)

                GL11.glTexCoord2d(1.0, 1.0); GL11.glVertex3d(aabb.minX, aabb.maxY, aabb.maxZ)
                GL11.glTexCoord2d(1.0, 0.0); GL11.glVertex3d(aabb.minX, aabb.maxY, aabb.minZ)
                GL11.glTexCoord2d(0.0, 0.0); GL11.glVertex3d(aabb.minX, aabb.minY, aabb.minZ)
                GL11.glTexCoord2d(0.0, 1.0); GL11.glVertex3d(aabb.minX, aabb.minY, aabb.maxZ)

                GL11.glTexCoord2d(1.0, 1.0); GL11.glVertex3d(aabb.maxX, aabb.maxY, aabb.minZ)
                GL11.glTexCoord2d(1.0, 0.0); GL11.glVertex3d(aabb.maxX, aabb.minY, aabb.minZ)
                GL11.glTexCoord2d(0.0, 0.0); GL11.glVertex3d(aabb.minX, aabb.minY, aabb.minZ)
                GL11.glTexCoord2d(0.0, 1.0); GL11.glVertex3d(aabb.minX, aabb.maxY, aabb.minZ)

                GL11.glTexCoord2d(0.0, 0.0); GL11.glVertex3d(aabb.minX, aabb.maxY, aabb.minZ)
                GL11.glTexCoord2d(0.0, 1.0); GL11.glVertex3d(aabb.minX, aabb.maxY, aabb.maxZ)
                GL11.glTexCoord2d(1.0, 1.0); GL11.glVertex3d(aabb.maxX, aabb.maxY, aabb.maxZ)
                GL11.glTexCoord2d(1.0, 0.0); GL11.glVertex3d(aabb.maxX, aabb.maxY, aabb.minZ)

                GL11.glTexCoord2d(1.0, 0.0); GL11.glVertex3d(aabb.maxX, aabb.maxY, aabb.minZ)
                GL11.glTexCoord2d(1.0, 1.0); GL11.glVertex3d(aabb.maxX, aabb.maxY, aabb.maxZ)
                GL11.glTexCoord2d(0.0, 1.0); GL11.glVertex3d(aabb.maxX, aabb.minY, aabb.maxZ)
                GL11.glTexCoord2d(0.0, 0.0); GL11.glVertex3d(aabb.maxX, aabb.minY, aabb.minZ)

                GL11.glTexCoord2d(1.0, 0.0); GL11.glVertex3d(aabb.maxX, aabb.minY, aabb.maxZ)
                GL11.glTexCoord2d(1.0, 1.0); GL11.glVertex3d(aabb.maxX, aabb.maxY, aabb.maxZ)
                GL11.glTexCoord2d(0.0, 1.0); GL11.glVertex3d(aabb.minX, aabb.maxY, aabb.maxZ)
                GL11.glTexCoord2d(0.0, 0.0); GL11.glVertex3d(aabb.minX, aabb.minY, aabb.maxZ)

                GL11.glEnd()
            }

            if (EnumFacing.DOWN in drawFaces) {
                GL11.glPushMatrix()
                GL11.glTranslated(center.x, minY - 0.01, center.z)
                GL11.glRotated(-90.0, 1.0, 0.0, 0.0)
                GL11.glScaled(0.01, 0.01, 0.01)
                renderer.drawCenterString(name, 0, 0, 0xFFFFFF)
                GL11.glPopMatrix()
            }
            if (EnumFacing.UP in drawFaces) {
                GL11.glPushMatrix()
                GL11.glTranslated(center.x, maxY + 0.01, center.z)
                GL11.glRotated(90.0, 1.0, 0.0, 0.0)
                GL11.glScaled(0.01, 0.01, 0.01)
                renderer.drawCenterString(name, 0, 0, 0xFFFFFF)
                GL11.glPopMatrix()
            }
            if (EnumFacing.SOUTH in drawFaces) {
                GL11.glPushMatrix()
                GL11.glTranslated(center.x, center.y, maxZ + 0.01)
                GL11.glRotated(180.0, 0.0, 1.0, 0.0)
                GL11.glRotated(180.0, 0.0, 0.0, 1.0)
                GL11.glScaled(0.01, 0.01, 0.01)
                renderer.drawCenterString(name, 0, 0, 0xFFFFFF)
                GL11.glPopMatrix()
            }
            if (EnumFacing.NORTH in drawFaces) {
                GL11.glPushMatrix()
                GL11.glTranslated(center.x, center.y, minZ - 0.01)
                GL11.glRotated(0.0, 0.0, 1.0, 0.0)
                GL11.glRotated(180.0, 0.0, 0.0, 1.0)
                GL11.glScaled(0.01, 0.01, 0.01)
                renderer.drawCenterString(name, 0, 0, 0xFFFFFF)
                GL11.glPopMatrix()
            }
            if (EnumFacing.EAST in drawFaces) {
                GL11.glPushMatrix()
                GL11.glTranslated(maxX + 0.01, center.y, center.z)
                GL11.glRotated(270.0, 0.0, 1.0, 0.0)
                GL11.glRotated(180.0, 0.0, 0.0, 1.0)
                GL11.glScaled(0.01, 0.01, 0.01)
                renderer.drawCenterString(name, 0, 0, 0xFFFFFF)
                GL11.glPopMatrix()
            }
            if (EnumFacing.WEST in drawFaces) {
                GL11.glPushMatrix()
                GL11.glTranslated(minX - 0.01, center.y, center.z)
                GL11.glRotated(90.0, 0.0, 1.0, 0.0)
                GL11.glRotated(180.0, 0.0, 0.0, 1.0)
                GL11.glScaled(0.01, 0.01, 0.01)
                renderer.drawCenterString(name, 0, 0, 0xFFFFFF)
                GL11.glPopMatrix()
            }
        }

        if (smoothing) {
            GL11.glShadeModel(GL11.GL_SMOOTH)
        }
        GL11.glPopMatrix()
    }

    override fun renderPart(smoothing: Boolean, partName: String) {
        renderAll(smoothing)
    }

    override fun renderOnly(smoothing: Boolean, vararg groupNames: String) {
        renderAll(smoothing)
    }

    override fun getGroupObjects(): List<GroupObject> = listOf()

    private val type = FileType("dummy_model_type", "dummy model made by anatawa12")

    override fun getType(): FileType = type

    override fun getDrawMode(): Int = 4

    override fun getSize(): FloatArray = floatArrayOf(
        aabb.minX.toFloat(),
        aabb.minY.toFloat(),
        aabb.minZ.toFloat(),
        aabb.maxX.toFloat(),
        aabb.maxY.toFloat(),
        aabb.maxZ.toFloat()
    )

    companion object {
        val resourceLocation = ResourceLocation("fix-rtm", "textures/ajwm5.png")
        lateinit var baseImage: BufferedImage
        fun init() {
            baseImage = ImageIO.read(Minecraft.getMinecraft().resourceManager.getResource(resourceLocation).inputStream)
        }
        val material = Material(0, resourceLocation)
        val materials = mapOf(DummyModelPackManager.getDummyName("") to Material(0, null))
    }
}
