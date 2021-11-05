/// Copyright (c) 2021 anatawa12 and other contributors
/// This file is part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

package com.anatawa12.fixRtm.rtm

import jp.ngt.ngtlib.io.NGTFileLoader
import net.minecraft.util.ResourceLocation
import java.io.File
import java.io.IOException
import java.io.InputStream

@Throws(IOException::class)
fun getInputStreamFromZip(zipPath: String, par1: ResourceLocation): InputStream? {
    return try {
        getInputStreamFromZip(zipPath, par1, "UTF-8")
    } catch (ignored: IllegalArgumentException) {
        getInputStreamFromZip(zipPath, par1, "MS932")
    }
}

@Throws(IOException::class)
private fun getInputStreamFromZip(zipPath: String, par1: ResourceLocation, encoding: String): InputStream? {
    var stream: InputStream? = null
    val zip = NGTFileLoader.getArchive(File(zipPath), encoding)
    val enu = zip.entries()
    while (enu.hasMoreElements()) {
        val ze = enu.nextElement()
        if (!ze.isDirectory) {
            val fileInZip = File(zipPath, ze.name)
            if (par1.path.contains(fileInZip.name)) {
                stream = zip.getInputStream(ze)
            }
        }
    }
    return stream
}
