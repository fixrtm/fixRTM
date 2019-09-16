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

    private val _size by lazy {
        var nx: Float = Float.POSITIVE_INFINITY
        var ny: Float = Float.POSITIVE_INFINITY
        var nz: Float = Float.POSITIVE_INFINITY
        var px: Float = Float.NEGATIVE_INFINITY
        var py: Float = Float.NEGATIVE_INFINITY
        var pz: Float = Float.NEGATIVE_INFINITY

        for (size in bases.map { it.size }) {
            nx = minOf(nx, size[0])
            ny = minOf(ny, size[1])
            nz = minOf(nz, size[2])
            px = maxOf(px, size[3])
            py = maxOf(py, size[4])
            pz = maxOf(pz, size[5])
        }

        floatArrayOf(nx, ny, nz, px, py, pz)
    }
    override fun getSize(): FloatArray = _size
}
