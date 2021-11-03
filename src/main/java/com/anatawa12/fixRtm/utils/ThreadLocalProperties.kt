package com.anatawa12.fixRtm.utils

import java.io.*
import java.util.*
import java.util.function.BiConsumer
import java.util.function.BiFunction
import java.util.function.Function

class ThreadLocalProperties : Properties() {
    // this class original implementations

    @Synchronized
    fun markThreadLocal(key: Any) = getEntry(key).setLocal()

    companion object {
        fun markLocalSystemProperty(key: String) {
            (System.getProperties() as? ThreadLocalProperties)
                ?.markThreadLocal(key)
        }

        fun setLocalSystemProperty(key: String, value: String) {
            val props = (System.getProperties() as? ThreadLocalProperties) ?: return
            props.markThreadLocal(key)
            props[key] = value
        }

        fun removeLocalSystemProperty(key: String) {
            val props = (System.getProperties() as? ThreadLocalProperties) ?: return
            props.markThreadLocal(key)
            props.remove(key)
        }
    }

    // Hashtable and Properties implementations

    private val backed = HashMap<Any, Entry>()

    private fun getEntry(key: Any) = backed.computeIfAbsent(key, ::Entry)

    // TODO
    @Synchronized
    override fun equals(other: Any?): Boolean {
        if (other === this) return true

        if (other !is Map<*, *>) return false
        if (other.size != size) return false

        try {
            for ((key, value) in other){
                if (value == null) return false
                val thisValue = backed[key]?.get() ?: return false
                if (thisValue != value) return false
            }
        } catch (ignored: ClassCastException) {
            return false
        } catch (ignored: NullPointerException) {
            return false
        }

        return true
    }

    // TODO
    @Synchronized
    override fun hashCode(): Int {
        val iter = Enumerator { it }
        var sum = 0
        while (true) sum += (iter.next0() ?: break).hashCode()
        return sum
    }

    // TODO
    @Synchronized
    override fun toString(): String {
        val max: Int = size - 1
        if (max == -1) return "{}"
        return buildString {
            append('{')
            val iter = Enumerator { it }
            var first = true
            while (true) {
                val (key, value) = iter.next0() ?: break
                if (!first) append(", ")
                first = false
                append(if (key === this) "(this Map)" else key.toString())
                append('=')
                append(if (value === this) "(this Map)" else value.toString())
            }
            append('}')
        }
    }

    @Synchronized
    override fun isEmpty(): Boolean = size == 0

    @Synchronized
    override fun keys(): Enumeration<Any> = Enumerator(Entry::key)

    @Synchronized
    override fun elements(): Enumeration<Any> = Enumerator(Entry::value)

    @Synchronized
    override fun get(key: Any): Any? = backed[key]?.get()

    @Synchronized
    override fun put(key: Any, value: Any): Any? = getEntry(key).set(value)

    @Synchronized
    override fun remove(key: Any): Any? = backed[key]?.delete()

    @Synchronized
    override fun remove(key: Any, value: Any): Boolean {
        val f = backed[key] ?: return false
        if (f.get() != value) return false
        f.delete()
        return true
    }

    @Synchronized
    override fun clear() = backed.clear()

    @Synchronized
    override fun putAll(from: Map<out Any, Any>) {
        for ((k, v) in from) put(k, v)
    }

    @Synchronized
    override fun containsKey(key: Any): Boolean = backed[key]?.get() != null

    @Synchronized
    override fun containsValue(value: Any): Boolean {
        return elements().asSequence().any { it == value }
    }

    @Synchronized
    override fun getOrDefault(key: Any, defaultValue: Any?): Any? = this[key] ?: defaultValue

    @Synchronized
    override fun forEach(action: BiConsumer<in Any, in Any>) {
        for ((k, v) in this)
            action.accept(k, v)
    }

    @Synchronized
    override fun replaceAll(function: BiFunction<in Any, in Any, out Any>) {
        for (entry in this)
            entry.setValue(function.apply(entry.key, entry.value))
    }

    @Synchronized
    override fun putIfAbsent(key: Any, value: Any): Any? {
        val entry = getEntry(key)
        entry.get()?.let { return it }
        return super.putIfAbsent(key, value)
    }

    @Synchronized
    override fun replace(key: Any, oldValue: Any, newValue: Any): Boolean {
        val entry = backed[key] ?: return false
        if (entry.get() == oldValue) return false
        entry.set(newValue)
        return true
    }

    @Synchronized
    override fun replace(key: Any, value: Any): Any? {
        val entry = backed[key]
        val curValue: Any? = entry?.get()
        if (curValue != null)
            entry.set(value)
        return curValue
    }

    @Synchronized
    override fun computeIfAbsent(key: Any, mappingFunction: Function<in Any, out Any>): Any {
        val entry = getEntry(key)
        val v = entry.get()
        if (v != null)
            return v
        val newValue = mappingFunction.apply(key)
        entry.set(newValue)
        return newValue
    }

    @Synchronized
    override fun computeIfPresent(key: Any, remappingFunction: BiFunction<in Any, in Any, out Any?>): Any? {
        val entry = backed[key]
        val v = entry?.get() ?: return null
        val newValue = remappingFunction.apply(key, v)
        if (newValue == null)
            entry.delete()
        else
            entry.set(newValue)
        return newValue
    }

    @Synchronized
    override fun compute(key: Any, remappingFunction: BiFunction<in Any, in Any?, out Any?>): Any? {
        val entry = backed[key]
        val oldValue = entry?.get()
        val newValue = remappingFunction.apply(key, oldValue)
        return if (newValue == null) {
            if (oldValue != null) {
                entry.delete()
                null
            } else {
                null
            }
        } else {
            getEntry(key).set(newValue)
            newValue
        }
    }

    @Synchronized
    override fun merge(key: Any, value: Any, remappingFunction: BiFunction<in Any, in Any, out Any?>): Any? {
        val entry = backed[key]
        val oldValue = entry?.get()
        val newValue = if (oldValue == null) value else remappingFunction.apply(oldValue, value)
        if (newValue == null) {
            entry?.delete()
        } else {
            getEntry(key).set(newValue)
        }
        return newValue
    }

    @Synchronized
    @Throws(CloneNotSupportedException::class)
    override fun clone(): Any {
        throw CloneNotSupportedException()
    }

    @Synchronized
    override fun contains(value: Any?): Boolean = values.any { it == value }

    override fun rehash() {
        throw IllegalStateException("the implementations on Properties Hashtable should not be used")
    }

    private fun writeObject(@Suppress("UNUSED_PARAMETER") s: ObjectOutputStream?) {
        throw NotSerializableException("ThreadLocalProperties")
    }

    private fun readObject(@Suppress("UNUSED_PARAMETER") s: ObjectInputStream?) {
        throw NotSerializableException("ThreadLocalProperties")
    }

    override fun setProperty(key: String, value: String): Any? {
        return put(key, value)
    }

    private inline fun load(block: Properties.() -> Unit) {
        putAll(Properties().apply(block))
    }

    private inline fun save(block: Properties.() -> Unit) {
        Properties().also { it.putAll(this) }.block()
    }

    @Synchronized
    override fun load(reader: Reader?) =
        load { load(reader) }

    @Synchronized
    override fun load(inStream: InputStream?) =
        load { load(inStream) }

    @Suppress("DEPRECATION")
    @Synchronized
    override fun save(out: OutputStream?, comments: String?) =
        save { save(out, comments) }

    @Synchronized
    override fun store(writer: Writer?, comments: String?) =
        save { store(writer, comments) }

    @Synchronized
    override fun store(out: OutputStream?, comments: String?) =
        save { store(out, comments) }

    @Synchronized
    override fun loadFromXML(`in`: InputStream?) =
        load { loadFromXML(`in`) }

    @Synchronized
    override fun storeToXML(os: OutputStream?, comment: String?) =
        save { storeToXML(os, comment) }

    @Synchronized
    override fun storeToXML(os: OutputStream?, comment: String?, encoding: String?) =
        save { storeToXML(os, comment, encoding) }

    override fun getProperty(key: String?): String? = this[key] as? String

    override fun getProperty(key: String?, defaultValue: String?): String? = getProperty(key) ?: defaultValue

    @Synchronized
    override fun propertyNames(): Enumeration<*> =
        Collections.enumeration(keys.map { it as String })

    @Synchronized
    override fun stringPropertyNames(): Set<String> {
        return backed.values.mapNotNullTo(mutableSetOf()) { entry ->
            if (entry.get() !is String) return@mapNotNullTo null
            entry.key as? String
        }
    }

    @Synchronized
    override fun list(out: PrintStream?) =
        save { list(out) }

    @Synchronized
    override fun list(out: PrintWriter?) =
        save { list(out) }

    override val values: MutableCollection<Any> by lazy(this) { ValuesCollection() }
    override val entries: MutableSet<MutableMap.MutableEntry<Any, Any>> by lazy(this) { EntrySet() }
    override val keys: MutableSet<Any> by lazy(this) { KeysSet() }
    @get:Synchronized
    override val size: Int get() = this.backed.values.count { it.get() != null }

    private inner class ValuesCollection : AbstractMutableCollection<Any>() {
        override val size: Int get() = this@ThreadLocalProperties.size

        override fun contains(element: Any): Boolean =
            synchronized(this@ThreadLocalProperties) { super.contains(element) }

        override fun containsAll(elements: Collection<Any>): Boolean =
            synchronized(this@ThreadLocalProperties) { super.containsAll(elements) }

        override fun isEmpty(): Boolean = this@ThreadLocalProperties.isEmpty

        override fun add(element: Any): Boolean = throw UnsupportedOperationException("add")

        override fun clear() = this@ThreadLocalProperties.clear()

        override fun iterator(): MutableIterator<Any> = Enumerator(Entry::get)

        override fun remove(element: Any): Boolean =
            synchronized(this@ThreadLocalProperties){ super.remove(element) }

        @Suppress("ConvertArgumentToSet")
        override fun removeAll(elements: Collection<Any>): Boolean =
            synchronized(this@ThreadLocalProperties) { super.removeAll(elements) }

        @Suppress("ConvertArgumentToSet")
        override fun retainAll(elements: Collection<Any>): Boolean =
            synchronized(this@ThreadLocalProperties) { super.retainAll(elements) }
    }

    private inner class EntrySet : AbstractMutableSet<MutableMap.MutableEntry<Any, Any>>() {
        override val size: Int get() = this@ThreadLocalProperties.size

        override fun contains(element: MutableMap.MutableEntry<Any, Any>): Boolean =
            synchronized(this@ThreadLocalProperties) { this@ThreadLocalProperties[element.key] == element.value }

        override fun containsAll(elements: Collection<MutableMap.MutableEntry<Any, Any>>): Boolean =
            synchronized(this@ThreadLocalProperties) { super.containsAll(elements) }

        override fun isEmpty(): Boolean = this@ThreadLocalProperties.isEmpty

        override fun add(element: MutableMap.MutableEntry<Any, Any>): Boolean = throw UnsupportedOperationException("add")

        override fun clear() = this@ThreadLocalProperties.clear()

        override fun iterator(): MutableIterator<MutableMap.MutableEntry<Any, Any>> = Enumerator { it }

        override fun remove(element: MutableMap.MutableEntry<Any, Any>): Boolean = synchronized(this@ThreadLocalProperties) {
            val entry = this@ThreadLocalProperties.backed[element.key]
            if (entry?.get() != element.value) return false
            return entry.delete() != null
        }

        @Suppress("ConvertArgumentToSet")
        override fun removeAll(elements: Collection<MutableMap.MutableEntry<Any, Any>>): Boolean =
            synchronized(this@ThreadLocalProperties) { super.removeAll(elements) }

        @Suppress("ConvertArgumentToSet")
        override fun retainAll(elements: Collection<MutableMap.MutableEntry<Any, Any>>): Boolean =
            synchronized(this@ThreadLocalProperties) { super.retainAll(elements) }

        override fun hashCode(): Int =
            synchronized(this@ThreadLocalProperties) { super.hashCode() }

        override fun equals(other: Any?): Boolean =
            synchronized(this@ThreadLocalProperties) { super.equals(other) }
    }

    private inner class KeysSet() : AbstractMutableSet<Any>() {
        override val size: Int get() = this@ThreadLocalProperties.size

        override fun contains(element: Any): Boolean = this@ThreadLocalProperties.containsKey(element)

        override fun containsAll(elements: Collection<Any>): Boolean =
            synchronized(this@ThreadLocalProperties) { super.containsAll(elements) }

        override fun isEmpty(): Boolean = this@ThreadLocalProperties.isEmpty

        override fun add(element: Any): Boolean = throw UnsupportedOperationException("add")

        override fun clear() = this@ThreadLocalProperties.clear()

        override fun iterator(): MutableIterator<Any> = Enumerator(Entry::key)

        override fun remove(element: Any): Boolean = this@ThreadLocalProperties.remove(element) != null

        @Suppress("ConvertArgumentToSet")
        override fun removeAll(elements: Collection<Any>): Boolean =
            synchronized(this@ThreadLocalProperties) { super.removeAll(elements) }

        @Suppress("ConvertArgumentToSet")
        override fun retainAll(elements: Collection<Any>): Boolean =
            synchronized(this@ThreadLocalProperties) { super.retainAll(elements) }

        override fun hashCode(): Int =
            synchronized(this@ThreadLocalProperties) { super.hashCode() }

        override fun equals(other: Any?): Boolean =
            synchronized(this@ThreadLocalProperties) { super.equals(other) }
    }

    private inner class Enumerator<T: Any>(val transform: (Entry) -> T?) : MutableIterator<T>, Enumeration<T> {
        private val backed = this@ThreadLocalProperties.backed.values.iterator()
        private var entry: Entry? = null
        private var returned: Entry? = null

        private tailrec fun computeNext(): T? {
            val entry: Entry? = this.entry
            if (entry?.get() != null) {
                transform(entry)?.let { return it }
            }
            // current value is not valid
            this.entry = null
            if (!backed.hasNext())
                return null
            this.entry = backed.next()
            return computeNext()
        }

        override fun hasNext(): Boolean = computeNext() != null

        fun next0(): T? {
            val value = computeNext() ?: return null
            returned = this.entry!!
            this.entry = null
            return value
        }

        override fun next(): T = next0() ?: throw NoSuchElementException()

        fun remove0(): Any? {
            val removed = (entry ?: throw IllegalStateException()).delete()
            this.entry = null
            return removed
        }

        override fun remove() {
            remove0()
        }

        override fun hasMoreElements(): Boolean = hasNext()

        override fun nextElement(): T = next()
    }

    private class Entry(override val key: Any) : MutableMap.MutableEntry<Any, Any> {
        @Volatile
        var global: Any? = null
        private val local = InheritableThreadLocal<Any>()

        override val value: Any
            get() = get() ?: throw IllegalStateException("getting value for removed value")

        override fun setValue(newValue: Any): Any =
            set(value) {
                throw IllegalStateException("setting value for removed value")
            }!!

        fun get(): Any? = when (val localValue = local.get()) {
            GLOBAL_MARKER -> global
            UNSET_MARKER -> null
            else -> localValue
        }

        fun delete(): Any? {
            val localValue = local.get()
            return if (localValue === GLOBAL_MARKER) {
                val old = global
                global = null
                old
            } else if (localValue === UNSET_MARKER) {
                null
            } else {
                local.set(UNSET_MARKER)
                localValue
            }
        }

        fun set(value: Any): Any? = set(value) {}

        private inline fun set(value: Any, onNotFound: () -> Unit): Any? {
            val localValue = local.get()!!
            return if (localValue === GLOBAL_MARKER) {
                val old = global
                if (old == null) onNotFound()
                global = value
                old
            } else if (localValue === UNSET_MARKER) {
                onNotFound()
                local.set(value)
                null
            } else {
                local.set(value)
                localValue
            }
        }

        override fun equals(other: Any?): Boolean =
            (other is Map.Entry<*, *>)
                    && other.key == this.key
                    && other.value == this.get()

        override fun hashCode(): Int = key.hashCode() xor get().hashCode()

        fun setLocal() {
            if (local.get() == GLOBAL_MARKER)
                local.set(global ?: UNSET_MARKER)
        }

        companion object {
            private val GLOBAL_MARKER = Any()
            private val UNSET_MARKER = Any()
        }
    }
}
