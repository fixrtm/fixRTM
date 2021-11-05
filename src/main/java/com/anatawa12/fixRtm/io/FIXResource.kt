/// Copyright (c) 2020 anatawa12 and other contributors
/// This file is part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

package com.anatawa12.fixRtm.io

import java.io.InputStream

class FIXResource(
    val pack: FIXModelPack,
    val inputStream: InputStream,
)
