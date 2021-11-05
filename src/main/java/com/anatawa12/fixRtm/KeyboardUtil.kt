/// Copyright (c) 2021 anatawa12 and other contributors
/// This file is part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

package com.anatawa12.fixRtm

import org.lwjgl.input.Keyboard

object KeyboardUtil {
    fun isIntegerKey(par: Int): Boolean {
        return when (par) {
            Keyboard.KEY_1,
            Keyboard.KEY_2,
            Keyboard.KEY_3,
            Keyboard.KEY_4,
            Keyboard.KEY_5,
            Keyboard.KEY_6,
            Keyboard.KEY_7,
            Keyboard.KEY_8,
            Keyboard.KEY_9,
            Keyboard.KEY_0,

            Keyboard.KEY_NUMPAD1,
            Keyboard.KEY_NUMPAD2,
            Keyboard.KEY_NUMPAD3,
            Keyboard.KEY_NUMPAD4,
            Keyboard.KEY_NUMPAD5,
            Keyboard.KEY_NUMPAD6,
            Keyboard.KEY_NUMPAD7,
            Keyboard.KEY_NUMPAD8,
            Keyboard.KEY_NUMPAD9,
            Keyboard.KEY_NUMPAD0,

            Keyboard.KEY_UP,
            Keyboard.KEY_PRIOR,
            Keyboard.KEY_LEFT,
            Keyboard.KEY_RIGHT,

            Keyboard.KEY_MINUS,
            Keyboard.KEY_SUBTRACT,
            Keyboard.KEY_BACK,
            Keyboard.KEY_DELETE,
            -> true
            else -> false
        }
    }

    fun isDecimalNumberKey(par: Int): Boolean {
        return isIntegerKey(par) || par == Keyboard.KEY_PERIOD || par == Keyboard.KEY_DECIMAL
    }
}
