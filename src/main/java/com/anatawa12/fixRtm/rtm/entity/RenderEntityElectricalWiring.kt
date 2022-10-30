/// Copyright (c) 2022 anatawa12 and other contributors
/// This file is part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

package com.anatawa12.fixRtm.rtm.entity

import jp.ngt.rtm.electric.RenderElectricalWiring
import jp.ngt.rtm.entity.EntityElectricalWiring
import net.minecraft.client.renderer.entity.Render
import net.minecraft.client.renderer.entity.RenderManager
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.MinecraftForgeClient
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL12

class RenderEntityElectricalWiring(renderManager: RenderManager) : Render<EntityElectricalWiring>(renderManager) {
    override fun doRender(entity: EntityElectricalWiring, par2: Double, par4: Double, par6: Double, par8: Float, par9: Float) {
        val tile = entity.tileEW
        val pass = MinecraftForgeClient.getRenderPass()
        if (tile.shouldRenderInPass(pass)) {
            GL11.glPushMatrix()
            GL11.glEnable(GL12.GL_RESCALE_NORMAL)
            GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
            RenderElectricalWiring.INSTANCE.renderAllWire(tile, par2, par4, par6, par9, pass)
            GL11.glPopMatrix()
        }

        GL11.glPushMatrix()
        GL11.glEnable(32826)
        GL11.glTranslatef(par2.toFloat(), par4.toFloat(), par6.toFloat())
        GL11.glRotatef(entity.rotationYaw, 0.0f, 1.0f, 0.0f)
        val modelsetmachine = entity.resourceState.resourceSet
        val machineconfig = modelsetmachine.config
        if (machineconfig.followRailAngle) {
            GL11.glRotatef(entity.rotationPitch, 1.0f, 0.0f, 0.0f)
            GL11.glRotatef(entity.rotationRoll, 0.0f, 0.0f, 1.0f)
        }

        val i = MinecraftForgeClient.getRenderPass()
        modelsetmachine.modelObj.render(entity, machineconfig, i, par9)
        GL11.glPopMatrix()
    }

    override fun getEntityTexture(entity: EntityElectricalWiring?): ResourceLocation? {
        return null
    }
}
