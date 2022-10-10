package com.anatawa12.fixRtm.utils

import java.util.*
import kotlin.NoSuchElementException

/**
 * A wrapper class of SortedSet.
 */
class SortedSetListView<T>(val sortedSet: SortedSet<T>) : MutableList<T> {
    // MutableCollection methods: simple wrapper
    override val size: Int get() = sortedSet.size
    override fun clear() = sortedSet.clear()
    override fun addAll(elements: Collection<T>): Boolean = sortedSet.addAll(elements)
    override fun add(element: T): Boolean = sortedSet.add(element)
    override fun isEmpty(): Boolean = sortedSet.isEmpty()
    override fun iterator(): MutableIterator<T> = sortedSet.iterator()
    override fun retainAll(elements: Collection<T>): Boolean = sortedSet.retainAll(elements)
    override fun removeAll(elements: Collection<T>): Boolean = sortedSet.retainAll(elements)
    override fun remove(element: T): Boolean = sortedSet.remove(element)
    override fun containsAll(elements: Collection<T>): Boolean = sortedSet.containsAll(elements)
    override fun contains(element: T): Boolean = sortedSet.contains(element)

    // indexed modifications: not supported

    @Suppress("NOTHING_TO_INLINE")
    private inline fun indexedValueModify(): Nothing = throw UnsupportedOperationException("indexed modification is not allowed")
    override fun addAll(index: Int, elements: Collection<T>): Boolean = indexedValueModify()
    override fun add(index: Int, element: T) = indexedValueModify()
    override fun set(index: Int, element: T): T = indexedValueModify()

    // list operations: O(n) implementation

    override fun get(index: Int): T {
        val iter = sortedSet.iterator()
        repeat(index) {
            if (!iter.hasNext()) throw IndexOutOfBoundsException("idx: $index, size: $size")
            iter.next()
        }
        if (!iter.hasNext()) throw IndexOutOfBoundsException("idx: $index, size: $size")
        return iter.next()
    }

    override fun removeAt(index: Int): T {
        val iter = sortedSet.iterator()
        repeat(index) {
            if (!iter.hasNext()) throw IndexOutOfBoundsException("idx: $index, size: $size")
            iter.next()
        }
        if (!iter.hasNext()) throw IndexOutOfBoundsException("idx: $index, size: $size")
        val value = iter.next()
        iter.remove()
        return value
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<T> {
        if (fromIndex < 0) throw IndexOutOfBoundsException("fromIndex = $fromIndex")
        if (toIndex > size) throw IndexOutOfBoundsException("toIndex = $toIndex")
        require(fromIndex <= toIndex) { "fromIndex($fromIndex) > toIndex($toIndex)" }


        val startAtHead = fromIndex == 0
        val endAtTail = toIndex == size

        if (startAtHead) {
            if (endAtTail) {
                // 0 until size
                return this
            } else {
                // 0 until ?
                return SortedSetListView(sortedSet.headSet(get(toIndex)))
            }
        } else {
            if (endAtTail) {
                // ? until size
                return SortedSetListView(sortedSet.tailSet(get(fromIndex)))
            } else if (fromIndex == toIndex) {
                // ? until ? which both have same
                val value = get(fromIndex)
                return SortedSetListView(sortedSet.subSet(value, value))
            } else {
                // ? until ?
                val iter = sortedSet.iterator()
                repeat(fromIndex) { iter.next() }
                val fromElementInclusive = iter.next()
                repeat(toIndex - fromIndex - 1) { iter.next() }
                val toElementExclusive = iter.next()
                return SortedSetListView(sortedSet.subSet(fromElementInclusive, toElementExclusive))
            }
        }
    }

    // this list is unique so lastIndexOf must be same as indexOf
    override fun lastIndexOf(element: T): Int = indexOf(element)

    override fun indexOf(element: T): Int {
        if (element !in sortedSet) return -1
        return sortedSet.headSet(element).size
    }

    override fun listIterator(): MutableListIterator<T> = listIterator(0)

    override fun listIterator(index: Int): MutableListIterator<T> = MutableListIteratorImpl(get(index))

    inner class MutableListIteratorImpl(
        /** this value must be either T or LAST_MARKER */
        var cursor: Any?
    ) : MutableListIterator<T> {
        var headSet: SortedSet<T>? = null
        /** this value must be either T or LAST_MARKER */
        var returned: Any? = LAST_MARKER

        private fun headSet(): SortedSet<T> {
            headSet?.let { return it }
            if (cursor === LAST_MARKER) return sortedSet
            @Suppress("UNCHECKED_CAST")
            return sortedSet.headSet(cursor as T).also { headSet = it }
        }

        override fun add(element: T) = indexedValueModify()
        override fun set(element: T)  = indexedValueModify()

        override fun hasNext(): Boolean = cursor !== LAST_MARKER

        override fun hasPrevious(): Boolean = headSet().isNotEmpty()

        override fun next(): T {
            if (cursor === LAST_MARKER) throw NoSuchElementException()
            @Suppress("UNCHECKED_CAST")
            val result: T = cursor as T
            cursor = sortedSet.tailSet(result).iterator().let {
                it.next() // skip result
                if (it.hasNext()) it.next() else LAST_MARKER
            }
            headSet = null
            returned = result
            return result
        }

        @Suppress("UNCHECKED_CAST")
        override fun nextIndex(): Int = if (cursor === LAST_MARKER) size else this@SortedSetListView.indexOf(cursor as T)

        override fun previous(): T = headSet().last().also {
            cursor = it
            headSet = null
            returned = it
        }

        override fun previousIndex(): Int = nextIndex() - 1

        override fun remove() {
            if (returned === LAST_MARKER)
                error("invalid status")
            @Suppress("UNCHECKED_CAST")
            remove(returned as T)
            returned = LAST_MARKER
        }
    }
}

private val LAST_MARKER = object {
    override fun toString(): String = "LAST_MARKER"
}
