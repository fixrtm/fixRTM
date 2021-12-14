package com.anatawa12.fixRtm.rtm.gui.camera

import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import org.lwjgl.opengl.*

private object GlobalConstants {
    @JvmField val GL43: Boolean
    @JvmField val frameBufferSupport: Boolean

    @JvmStatic var frameBuffer: Int = -1
        get() {
            if (field == -1)
                field = OpenGlHelper.glGenFramebuffers()
            return field
        }

    init {
        val capabilities = GLContext.getCapabilities()
        GL43 = capabilities.OpenGL43
        frameBufferSupport = capabilities.OpenGL14 && (capabilities.OpenGL30 || capabilities.GL_ARB_framebuffer_object)
        OpenGlHelper.glGenFramebuffers()
    }
}

fun copyTexture(src: Int, dst: Int, width: Int, height: Int) {
    if (GlobalConstants.GL43) {
        // this is NGT's original impl
        GL43.glCopyImageSubData(
            src, GL11.GL_TEXTURE_2D, 0, 0, 0, 0,
            dst, GL11.GL_TEXTURE_2D, 0, 0, 0, 0,
            width, height, 1)
    } else if (GlobalConstants.frameBufferSupport) {
        val prev = GlStateManager.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING) // also be ARBFramebufferObject.GL_FRAMEBUFFER_BINDING
        GlStateManager.bindTexture(0)
        OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, GlobalConstants.frameBuffer)
        OpenGlHelper.glFramebufferTexture2D(
            GL30.GL_READ_FRAMEBUFFER, // also be ARBFramebufferObject.GL_READ_FRAMEBUFFER
            GL30.GL_COLOR_ATTACHMENT0, // also be ARBFramebufferObject.GL_COLOR_ATTACHMENT0
            GL11.GL_TEXTURE_2D,
            src,
            0
        )
        OpenGlHelper.glFramebufferTexture2D(
            GL30.GL_DRAW_FRAMEBUFFER, // also be ARBFramebufferObject.GL_DRAW_FRAMEBUFFER
            GL30.GL_COLOR_ATTACHMENT1, // also be ARBFramebufferObject.GL_COLOR_ATTACHMENT1
            GL11.GL_TEXTURE_2D,
            dst,
            0
        )
        GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT1) // also be ARBFramebufferObject.GL_COLOR_ATTACHMENT1
        // VV also be ARBFramebufferObject.glBlitFramebuffer()
        GL30.glBlitFramebuffer(
            0, 0, height, width,
            0, 0, height, width,
            GL11.GL_COLOR_BUFFER_BIT,
            GL11.GL_NEAREST,
        )
        // restore
        OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, prev)
    } else {
        error("no way to copy texture is supported. please upgrade your OS or graphic board (driver).")
    }
}
