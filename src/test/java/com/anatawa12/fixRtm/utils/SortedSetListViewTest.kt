/// Copyright (c) 2021 anatawa12 and other contributors
/// This file is part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

package com.anatawa12.fixRtm.utils

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

class SortedSetListViewTest {
    @Test
    fun test() {
        val sortedSet = TreeSet(setOf(
            "abc",
            "def",
            "ghi",
            "jkl",
        ))
        val sortedList = SortedSetListView(sortedSet)
        assertEquals("abc", sortedList[0])
        assertEquals("def", sortedList[1])
        assertEquals("ghi", sortedList[2])
        assertEquals("jkl", sortedList[3])

        val subList = sortedList.subList(1, 3)
        assertEquals("def", subList[0])
        assertEquals("ghi", subList[1])

        val listIter = sortedList.listIterator(2)
        assertEquals("ghi", listIter.next())
        assertEquals("jkl", listIter.next())
        assertEquals(4, listIter.nextIndex())
        assertEquals("jkl", listIter.previous())
        assertEquals("ghi", listIter.previous())
        assertEquals("def", listIter.previous())
        assertEquals("abc", listIter.previous())
        assertEquals(0, listIter.nextIndex())
        assertEquals(-1, listIter.previousIndex())
    }
}
