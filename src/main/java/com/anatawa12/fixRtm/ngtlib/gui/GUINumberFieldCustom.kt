/// Copyright (c) 2022 anatawa12 and other contributors
/// This file is part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

package com.anatawa12.fixRtm.ngtlib.gui

import jp.ngt.ngtlib.gui.GuiTextFieldCustom
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.GuiScreen
import net.minecraft.util.ChatAllowedCharacters
import kotlin.math.max
import kotlin.math.min


class GUINumberFieldCustom(id: Int, fr: FontRenderer, x: Int, y: Int, w: Int, h: Int, pScr: GuiScreen?, val floats: Boolean)
    : GuiTextFieldCustom(id, fr, x, y, w, h, pScr) {
    private fun matchNumber(str: String): Boolean {
        if (str.isEmpty()) return true
        var i = 0
        // allow '+' or '-' at first
        if (str[i] == '+' || str[i] == '-') {
            if (str.length == 1) return true
            i++
        }
        // read digits
        while (str[i] in '0'..'9')
            // if digits is end of string, it's ok
            if (++i == str.length) return true
        // for integer, digits must be the last char of number
        if (!floats) return false
        // the char just after digits must be '.'
        if (str[i++] != '.') return false
        if (i == str.length) return true
        while (str[i] in '0'..'9')
            if (++i == str.length) return true
        return false
    }

    override fun setText(par1: String) {
        if (matchNumber(par1))
            super.setText(par1)
    }

    override fun writeText(textToWrite: String) {
        val filteredText: String = ChatAllowedCharacters.filterAllowedCharacters(textToWrite)
        val selectionRangeBegin = min(cursorPosition, selectionEnd)
        val selectionRangeEnd = max(cursorPosition, selectionEnd)
        val insertableLength = maxStringLength - text.length - (selectionRangeBegin - selectionEnd)
        var finalString = ""
        if (text.isNotEmpty()) {
            finalString += text.substring(0, selectionRangeBegin)
        }
        val insertingLength: Int
        if (insertableLength < filteredText.length) {
            finalString += filteredText.substring(0, insertableLength)
            insertingLength = insertableLength
        } else {
            finalString += filteredText
            insertingLength = filteredText.length
        }
        if (text.isNotEmpty() && selectionRangeEnd < text.length) {
            finalString += text.substring(selectionRangeEnd)
        }
        if (matchNumber(finalString)) {
            text = finalString
            moveCursorBy(selectionRangeBegin - selectionEnd + insertingLength)
        }
    }

}
