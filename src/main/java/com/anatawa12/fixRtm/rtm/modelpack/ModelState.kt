/// Copyright (c) 2021 anatawa12 and other contributors
/// This file is part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

package com.anatawa12.fixRtm.rtm.modelpack

enum class ModelState(private val label: String, val marker: String) {
    INITIALIZED("Initialized", "I"),
    CONSTRUCTED("Constructed", "C"),

    ;

    override fun toString(): String {
        return label
    }
}
