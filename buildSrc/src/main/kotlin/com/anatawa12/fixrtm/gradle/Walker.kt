/// Copyright (c) 2020 anatawa12 and other contributors
/// This file is part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

package com.anatawa12.fixrtm.gradle


import java.util.*

/**
 * An enumeration to describe possible walk directions.
 * There are two of them: beginning from parents, ending with children,
 * and beginning from children, ending with parents. Both use depth-first search.
 */
enum class WalkDirection {
    /** Depth-first search, directory is visited BEFORE its files */
    TOP_DOWN,

    /** Depth-first search, directory is visited AFTER its files */
    BOTTOM_UP
    // Do we want also breadth-first search?
}

/**
 * This class is intended to implement different file traversal methods.
 * It allows to iterate through all files inside a given directory.
 *
 * Use [Element.walk], [Element.walkTopDown] or [Element.walkBottomUp] extension functions to instantiate a `FileTreeWalk` instance.

 * If the file path given is just a file, walker iterates only it.
 * If the file path given does not exist, walker iterates nothing, i.e. it's equivalent to an empty sequence.
 */
class TreeWalk<Element> internal constructor(
    private val start: Element,
    private val listFiles: Element.() -> Sequence<Element>,
    private val direction: WalkDirection = WalkDirection.TOP_DOWN
) : Sequence<Element> {

    override fun iterator(): Iterator<Element> = FileTreeWalkIterator()

    private abstract inner class DirectoryState(val root: Element) {
        abstract fun step(): Element?
    }

    private inner class FileTreeWalkIterator : AbstractIterator<Element>() {
        // Stack of directory states, beginning from the start directory
        private val state = ArrayDeque<DirectoryState>()

        init {
            state.push(directoryState(start))
        }

        override fun computeNext() {
            val nextFile = gotoNext()
            if (nextFile != null)
                setNext(nextFile)
            else
                done()
        }


        private fun directoryState(root: Element): DirectoryState {
            return when (direction) {
                WalkDirection.TOP_DOWN -> TopDownDirectoryState(root)
                WalkDirection.BOTTOM_UP -> BottomUpDirectoryState(root)
            }
        }

        private tailrec fun gotoNext(): Element? {
            // Take next file from the top of the stack or return if there's nothing left
            val topState = state.peek() ?: return null
            val file = topState.step()
            if (file == null) {
                // There is nothing more on the top of the stack, go back
                state.pop()
                return gotoNext()
            } else {
                // Check that file/directory matches the filter
                if (file == topState.root) {
                    // Proceed to a root directory or a simple file
                    return file
                } else {
                    // Proceed to a sub-directory
                    state.push(directoryState(file))
                    return gotoNext()
                }
            }
        }

        /** Visiting in bottom-up order */
        private inner class BottomUpDirectoryState(rootDir: Element) : DirectoryState(rootDir) {

            private var rootVisited = false

            private var fileList = root.listFiles().iterator()

            /** First all children, then root directory */
            override fun step(): Element? {
                if (fileList.hasNext()) {
                    // First visit all files
                    return fileList.next()
                } else if (!rootVisited) {
                    // Then visit root
                    rootVisited = true
                    return root
                } else {
                    // That's all
                    return null
                }
            }
        }

        /** Visiting in top-down order */
        private inner class TopDownDirectoryState(rootDir: Element) : DirectoryState(rootDir) {

            private var rootVisited = false

            private var fileList: Iterator<Element>? = null

            /** First root directory, then all children */
            override fun step(): Element? {
                if (!rootVisited) {
                    // First visit root
                    rootVisited = true
                    return root
                } else if (fileList == null || fileList!!.hasNext()) {
                    if (fileList == null) {
                        // Then read an array of files, if any
                        fileList = root.listFiles().iterator()
                        if (!fileList!!.hasNext()) {
                            return null
                        }
                    }
                    // Then visit all files
                    return fileList!!.next()
                } else {
                    // That's all
                    return null
                }
            }
        }
    }
}

/**
 * Gets a sequence for visiting this directory and all its content.
 *
 * @param direction walk direction, top-down (by default) or bottom-up.
 */
fun <Element> Element.walk(
    direction: WalkDirection = WalkDirection.TOP_DOWN,
    listFiles: Element.() -> Sequence<Element>
): TreeWalk<Element> = TreeWalk(this, listFiles, direction)

/**
 * Gets a sequence for visiting this directory and all its content in top-down order.
 * Depth-first search is used and directories are visited before all their files.
 */
fun <Element> Element.walkTopDown(listFiles: Element.() -> Sequence<Element>): TreeWalk<Element> =
    walk(WalkDirection.TOP_DOWN, listFiles)

/**
 * Gets a sequence for visiting this directory and all its content in bottom-up order.
 * Depth-first search is used and directories are visited after all their files.
 */
fun <Element> Element.walkBottomUp(listFiles: Element.() -> Sequence<Element>): TreeWalk<Element> =
    walk(WalkDirection.BOTTOM_UP, listFiles)
