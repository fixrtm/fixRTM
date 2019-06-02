package com.anatawa12.fixRtm.asm

import com.anatawa12.fixRtm.dummies.DummyModelObject
import jp.ngt.ngtlib.io.FileType
import jp.ngt.ngtlib.renderer.model.GroupObject
import jp.ngt.ngtlib.renderer.model.IModelNGT
import jp.ngt.ngtlib.renderer.model.Material

class MultiModelObject(vararg val bases: IModelNGT) : IModelNGT {
    override fun getMaterials(): Map<String, Material> = DummyModelObject.materials

    override fun renderAll(smoothing: Boolean) {
        for (base in bases) {
            base.renderAll(smoothing)
        }
    }

    override fun renderPart(smoothing: Boolean, p1: String?) {
        renderAll(smoothing)
    }

    override fun renderOnly(smoothing: Boolean, vararg p1: String?) {
        renderAll(smoothing)
    }

    override fun getGroupObjects(): List<GroupObject> = listOf()

    private val type = FileType("multi_model_type", "multi model made by anatawa12")

    override fun getType(): FileType = type

    override fun getDrawMode(): Int = 4
}