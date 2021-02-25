package com.anatawa12.fixRtm

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import io.netty.buffer.Unpooled
import io.netty.util.ByteProcessor
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.nio.channels.GatheringByteChannel
import java.nio.channels.ScatteringByteChannel
import java.nio.charset.Charset
import java.util.zip.Deflater
import java.util.zip.Deflater.BEST_COMPRESSION

class DeflateByteBuf(private val writeTo: ByteBuf) : ByteBuf() {
    private val base = Unpooled.buffer()!!

    fun writeDeflated() {
        println("real packet size: ${base.readableBytes()}")
        println("real packet: $base")
        val def = Deflater()
        def.setLevel(BEST_COMPRESSION)
        var readBuf: ByteArray? = ByteArray(base.readableBytes())
        base.readBytes(readBuf)
        def.setInput(readBuf)
        println("inpit size: ${readBuf?.size}")
        def.finish()
        readBuf = null
        val buf = ByteArray(1024)
        while (!def.finished()) {
            val len = def.deflate(buf)
            writeTo.writeBytes(buf, 0, len)
        }
        println("send packet: $writeTo")
        def.end()
    }

    override fun readerIndex(): Int {
        return base.readerIndex()
    }

    override fun readerIndex(readerIndex: Int): ByteBuf {
        return base.readerIndex(readerIndex)
    }

    override fun setZero(index: Int, length: Int): ByteBuf {
        return base.setZero(index, length)
    }

    override fun setShortLE(index: Int, value: Int): ByteBuf {
        return base.setShortLE(index, value)
    }

    override fun getUnsignedInt(index: Int): Long {
        return base.getUnsignedInt(index)
    }

    override fun readByte(): Byte {
        return base.readByte()
    }

    override fun arrayOffset(): Int {
        return base.arrayOffset()
    }

    override fun writeCharSequence(sequence: CharSequence?, charset: Charset?): Int {
        return base.writeCharSequence(sequence, charset)
    }

    override fun getMedium(index: Int): Int {
        return base.getMedium(index)
    }

    override fun asReadOnly(): ByteBuf {
        return base.asReadOnly()
    }

    override fun setCharSequence(index: Int, sequence: CharSequence?, charset: Charset?): Int {
        return base.setCharSequence(index, sequence, charset)
    }

    override fun nioBuffer(): ByteBuffer {
        return base.nioBuffer()
    }

    override fun nioBuffer(index: Int, length: Int): ByteBuffer {
        return base.nioBuffer(index, length)
    }

    override fun getBoolean(index: Int): Boolean {
        return base.getBoolean(index)
    }

    override fun writeByte(value: Int): ByteBuf {
        return base.writeByte(value)
    }

    override fun capacity(): Int {
        return base.capacity()
    }

    override fun capacity(newCapacity: Int): ByteBuf {
        return base.capacity(newCapacity)
    }

    override fun writeShortLE(value: Int): ByteBuf {
        return base.writeShortLE(value)
    }

    override fun isReadOnly(): Boolean {
        return base.isReadOnly()
    }

    override fun readShortLE(): Short {
        return base.readShortLE()
    }

    override fun setMedium(index: Int, value: Int): ByteBuf {
        return base.setMedium(index, value)
    }

    override fun setInt(index: Int, value: Int): ByteBuf {
        return base.setInt(index, value)
    }

    override fun getUnsignedShortLE(index: Int): Int {
        return base.getUnsignedShortLE(index)
    }

    override fun maxWritableBytes(): Int {
        return base.maxWritableBytes()
    }

    override fun readDouble(): Double {
        return base.readDouble()
    }

    override fun duplicate(): ByteBuf {
        return base.duplicate()
    }

    override fun writeShort(value: Int): ByteBuf {
        return base.writeShort(value)
    }

    override fun clear(): ByteBuf {
        return base.clear()
    }

    override fun readUnsignedShort(): Int {
        return base.readUnsignedShort()
    }

    override fun readUnsignedByte(): Short {
        return base.readUnsignedByte()
    }

    override fun readIntLE(): Int {
        return base.readIntLE()
    }

    override fun unwrap(): ByteBuf {
        return base.unwrap()
    }

    override fun getShortLE(index: Int): Short {
        return base.getShortLE(index)
    }

    override fun writeChar(value: Int): ByteBuf {
        return base.writeChar(value)
    }

    override fun getCharSequence(index: Int, length: Int, charset: Charset?): CharSequence {
        return base.getCharSequence(index, length, charset)
    }

    override fun getFloat(index: Int): Float {
        return base.getFloat(index)
    }

    override fun writeBoolean(value: Boolean): ByteBuf {
        return base.writeBoolean(value)
    }

    override fun nioBuffers(): Array<ByteBuffer> {
        return base.nioBuffers()
    }

    override fun nioBuffers(index: Int, length: Int): Array<ByteBuffer> {
        return base.nioBuffers(index, length)
    }

    override fun maxCapacity(): Int {
        return base.maxCapacity()
    }

    override fun writeInt(value: Int): ByteBuf {
        return base.writeInt(value)
    }

    override fun getUnsignedByte(index: Int): Short {
        return base.getUnsignedByte(index)
    }

    override fun readSlice(length: Int): ByteBuf {
        return base.readSlice(length)
    }

    override fun readBytes(length: Int): ByteBuf {
        return base.readBytes(length)
    }

    override fun readBytes(dst: ByteBuf?): ByteBuf {
        return base.readBytes(dst)
    }

    override fun readBytes(dst: ByteBuf?, length: Int): ByteBuf {
        return base.readBytes(dst, length)
    }

    override fun readBytes(dst: ByteBuf?, dstIndex: Int, length: Int): ByteBuf {
        return base.readBytes(dst, dstIndex, length)
    }

    override fun readBytes(dst: ByteArray?): ByteBuf {
        return base.readBytes(dst)
    }

    override fun readBytes(dst: ByteArray?, dstIndex: Int, length: Int): ByteBuf {
        return base.readBytes(dst, dstIndex, length)
    }

    override fun readBytes(dst: ByteBuffer?): ByteBuf {
        return base.readBytes(dst)
    }

    override fun readBytes(out: OutputStream?, length: Int): ByteBuf {
        return base.readBytes(out, length)
    }

    override fun readBytes(out: GatheringByteChannel?, length: Int): Int {
        return base.readBytes(out, length)
    }

    override fun readBytes(out: FileChannel?, position: Long, length: Int): Int {
        return base.readBytes(out, position, length)
    }

    override fun writeLong(value: Long): ByteBuf {
        return base.writeLong(value)
    }

    override fun indexOf(fromIndex: Int, toIndex: Int, value: Byte): Int {
        return base.indexOf(fromIndex, toIndex, value)
    }

    override fun markWriterIndex(): ByteBuf {
        return base.markWriterIndex()
    }

    override fun equals(other: Any?): Boolean {
        return base == (other as? DeflateByteBuf)?.base
    }

    override fun readChar(): Char {
        return base.readChar()
    }

    override fun compareTo(other: ByteBuf?): Int {
        return base.compareTo(other)
    }

    override fun writeFloat(value: Float): ByteBuf {
        return base.writeFloat(value)
    }

    override fun getUnsignedShort(index: Int): Int {
        return base.getUnsignedShort(index)
    }

    override fun readCharSequence(length: Int, charset: Charset?): CharSequence {
        return base.readCharSequence(length, charset)
    }

    override fun forEachByte(processor: ByteProcessor?): Int {
        return base.forEachByte(processor)
    }

    override fun forEachByte(index: Int, length: Int, processor: ByteProcessor?): Int {
        return base.forEachByte(index, length, processor)
    }

    override fun isWritable(): Boolean {
        return base.isWritable()
    }

    override fun isWritable(size: Int): Boolean {
        return base.isWritable(size)
    }

    override fun readableBytes(): Int {
        return base.readableBytes()
    }

    override fun setShort(index: Int, value: Int): ByteBuf {
        return base.setShort(index, value)
    }

    override fun writeZero(length: Int): ByteBuf {
        return base.writeZero(length)
    }

    override fun refCnt(): Int {
        return base.refCnt()
    }

    override fun writerIndex(): Int {
        return base.writerIndex()
    }

    override fun writerIndex(writerIndex: Int): ByteBuf {
        return base.writerIndex(writerIndex)
    }

    override fun skipBytes(length: Int): ByteBuf {
        return base.skipBytes(length)
    }

    override fun bytesBefore(value: Byte): Int {
        return base.bytesBefore(value)
    }

    override fun bytesBefore(length: Int, value: Byte): Int {
        return base.bytesBefore(length, value)
    }

    override fun bytesBefore(index: Int, length: Int, value: Byte): Int {
        return base.bytesBefore(index, length, value)
    }

    override fun getByte(index: Int): Byte {
        return base.getByte(index)
    }

    override fun readUnsignedMedium(): Int {
        return base.readUnsignedMedium()
    }

    override fun getMediumLE(index: Int): Int {
        return base.getMediumLE(index)
    }

    override fun resetReaderIndex(): ByteBuf {
        return base.resetReaderIndex()
    }

    override fun setBoolean(index: Int, value: Boolean): ByteBuf {
        return base.setBoolean(index, value)
    }

    override fun setByte(index: Int, value: Int): ByteBuf {
        return base.setByte(index, value)
    }

    override fun readRetainedSlice(length: Int): ByteBuf {
        return base.readRetainedSlice(length)
    }

    override fun readLongLE(): Long {
        return base.readLongLE()
    }

    override fun discardSomeReadBytes(): ByteBuf {
        return base.discardSomeReadBytes()
    }

    override fun forEachByteDesc(processor: ByteProcessor?): Int {
        return base.forEachByteDesc(processor)
    }

    override fun forEachByteDesc(index: Int, length: Int, processor: ByteProcessor?): Int {
        return base.forEachByteDesc(index, length, processor)
    }

    override fun discardReadBytes(): ByteBuf {
        return base.discardReadBytes()
    }

    override fun nioBufferCount(): Int {
        return base.nioBufferCount()
    }

    override fun copy(): ByteBuf {
        return base.copy()
    }

    override fun copy(index: Int, length: Int): ByteBuf {
        return base.copy(index, length)
    }

    override fun getLong(index: Int): Long {
        return base.getLong(index)
    }

    override fun setBytes(index: Int, src: ByteBuf?): ByteBuf {
        return base.setBytes(index, src)
    }

    override fun setBytes(index: Int, src: ByteBuf?, length: Int): ByteBuf {
        return base.setBytes(index, src, length)
    }

    override fun setBytes(index: Int, src: ByteBuf?, srcIndex: Int, length: Int): ByteBuf {
        return base.setBytes(index, src, srcIndex, length)
    }

    override fun setBytes(index: Int, src: ByteArray?): ByteBuf {
        return base.setBytes(index, src)
    }

    override fun setBytes(index: Int, src: ByteArray?, srcIndex: Int, length: Int): ByteBuf {
        return base.setBytes(index, src, srcIndex, length)
    }

    override fun setBytes(index: Int, src: ByteBuffer?): ByteBuf {
        return base.setBytes(index, src)
    }

    override fun setBytes(index: Int, `in`: InputStream?, length: Int): Int {
        return base.setBytes(index, `in`, length)
    }

    override fun setBytes(index: Int, `in`: ScatteringByteChannel?, length: Int): Int {
        return base.setBytes(index, `in`, length)
    }

    override fun setBytes(index: Int, `in`: FileChannel?, position: Long, length: Int): Int {
        return base.setBytes(index, `in`, position, length)
    }

    override fun readUnsignedShortLE(): Int {
        return base.readUnsignedShortLE()
    }

    override fun setLong(index: Int, value: Long): ByteBuf {
        return base.setLong(index, value)
    }

    override fun internalNioBuffer(index: Int, length: Int): ByteBuffer {
        return base.internalNioBuffer(index, length)
    }

    override fun resetWriterIndex(): ByteBuf {
        return base.resetWriterIndex()
    }

    override fun readLong(): Long {
        return base.readLong()
    }

    override fun retainedSlice(): ByteBuf {
        return base.retainedSlice()
    }

    override fun retainedSlice(index: Int, length: Int): ByteBuf {
        return base.retainedSlice(index, length)
    }

    override fun memoryAddress(): Long {
        return base.memoryAddress()
    }

    override fun hashCode(): Int {
        return base.hashCode()
    }

    override fun setFloat(index: Int, value: Float): ByteBuf {
        return base.setFloat(index, value)
    }

    override fun toString(charset: Charset?): String {
        return base.toString(charset)
    }

    override fun toString(index: Int, length: Int, charset: Charset?): String {
        return base.toString(index, length, charset)
    }

    override fun toString(): String {
        return base.toString()
    }

    override fun hasMemoryAddress(): Boolean {
        return base.hasMemoryAddress()
    }

    override fun writeLongLE(value: Long): ByteBuf {
        return base.writeLongLE(value)
    }

    override fun setMediumLE(index: Int, value: Int): ByteBuf {
        return base.setMediumLE(index, value)
    }

    override fun order(): ByteOrder {
        return base.order()
    }

    override fun order(endianness: ByteOrder?): ByteBuf {
        return base.order(endianness)
    }

    override fun readUnsignedInt(): Long {
        return base.readUnsignedInt()
    }

    override fun isDirect(): Boolean {
        return base.isDirect()
    }

    override fun readMedium(): Int {
        return base.readMedium()
    }

    override fun getShort(index: Int): Short {
        return base.getShort(index)
    }

    override fun setDouble(index: Int, value: Double): ByteBuf {
        return base.setDouble(index, value)
    }

    override fun readShort(): Short {
        return base.readShort()
    }

    override fun alloc(): ByteBufAllocator {
        return base.alloc()
    }

    override fun getUnsignedMedium(index: Int): Int {
        return base.getUnsignedMedium(index)
    }

    override fun writeBytes(src: ByteBuf?): ByteBuf {
        return base.writeBytes(src)
    }

    override fun writeBytes(src: ByteBuf?, length: Int): ByteBuf {
        return base.writeBytes(src, length)
    }

    override fun writeBytes(src: ByteBuf?, srcIndex: Int, length: Int): ByteBuf {
        return base.writeBytes(src, srcIndex, length)
    }

    override fun writeBytes(src: ByteArray?): ByteBuf {
        return base.writeBytes(src)
    }

    override fun writeBytes(src: ByteArray?, srcIndex: Int, length: Int): ByteBuf {
        return base.writeBytes(src, srcIndex, length)
    }

    override fun writeBytes(src: ByteBuffer?): ByteBuf {
        return base.writeBytes(src)
    }

    override fun writeBytes(`in`: InputStream?, length: Int): Int {
        return base.writeBytes(`in`, length)
    }

    override fun writeBytes(`in`: ScatteringByteChannel?, length: Int): Int {
        return base.writeBytes(`in`, length)
    }

    override fun writeBytes(`in`: FileChannel?, position: Long, length: Int): Int {
        return base.writeBytes(`in`, position, length)
    }

    override fun setIndex(readerIndex: Int, writerIndex: Int): ByteBuf {
        return base.setIndex(readerIndex, writerIndex)
    }

    override fun array(): ByteArray {
        return base.array()
    }

    override fun getUnsignedMediumLE(index: Int): Int {
        return base.getUnsignedMediumLE(index)
    }

    override fun getIntLE(index: Int): Int {
        return base.getIntLE(index)
    }

    override fun slice(): ByteBuf {
        return base.slice()
    }

    override fun slice(index: Int, length: Int): ByteBuf {
        return base.slice(index, length)
    }

    override fun getUnsignedIntLE(index: Int): Long {
        return base.getUnsignedIntLE(index)
    }

    override fun readFloat(): Float {
        return base.readFloat()
    }

    override fun getBytes(index: Int, dst: ByteBuf?): ByteBuf {
        return base.getBytes(index, dst)
    }

    override fun getBytes(index: Int, dst: ByteBuf?, length: Int): ByteBuf {
        return base.getBytes(index, dst, length)
    }

    override fun getBytes(index: Int, dst: ByteBuf?, dstIndex: Int, length: Int): ByteBuf {
        return base.getBytes(index, dst, dstIndex, length)
    }

    override fun getBytes(index: Int, dst: ByteArray?): ByteBuf {
        return base.getBytes(index, dst)
    }

    override fun getBytes(index: Int, dst: ByteArray?, dstIndex: Int, length: Int): ByteBuf {
        return base.getBytes(index, dst, dstIndex, length)
    }

    override fun getBytes(index: Int, dst: ByteBuffer?): ByteBuf {
        return base.getBytes(index, dst)
    }

    override fun getBytes(index: Int, out: OutputStream?, length: Int): ByteBuf {
        return base.getBytes(index, out, length)
    }

    override fun getBytes(index: Int, out: GatheringByteChannel?, length: Int): Int {
        return base.getBytes(index, out, length)
    }

    override fun getBytes(index: Int, out: FileChannel?, position: Long, length: Int): Int {
        return base.getBytes(index, out, position, length)
    }

    override fun markReaderIndex(): ByteBuf {
        return base.markReaderIndex()
    }

    override fun getDouble(index: Int): Double {
        return base.getDouble(index)
    }

    override fun readBoolean(): Boolean {
        return base.readBoolean()
    }

    override fun writeIntLE(value: Int): ByteBuf {
        return base.writeIntLE(value)
    }

    override fun readInt(): Int {
        return base.readInt()
    }

    override fun getLongLE(index: Int): Long {
        return base.getLongLE(index)
    }

    override fun writeMedium(value: Int): ByteBuf {
        return base.writeMedium(value)
    }

    override fun touch(): ByteBuf {
        return base.touch()
    }

    override fun touch(hint: Any?): ByteBuf {
        return base.touch(hint)
    }

    override fun writeDouble(value: Double): ByteBuf {
        return base.writeDouble(value)
    }

    override fun readUnsignedMediumLE(): Int {
        return base.readUnsignedMediumLE()
    }

    override fun getInt(index: Int): Int {
        return base.getInt(index)
    }

    override fun setIntLE(index: Int, value: Int): ByteBuf {
        return base.setIntLE(index, value)
    }

    override fun setChar(index: Int, value: Int): ByteBuf {
        return base.setChar(index, value)
    }

    override fun writeMediumLE(value: Int): ByteBuf {
        return base.writeMediumLE(value)
    }

    override fun ensureWritable(minWritableBytes: Int): ByteBuf {
        return base.ensureWritable(minWritableBytes)
    }

    override fun ensureWritable(minWritableBytes: Int, force: Boolean): Int {
        return base.ensureWritable(minWritableBytes, force)
    }

    override fun retain(increment: Int): ByteBuf {
        return base.retain(increment)
    }

    override fun retain(): ByteBuf {
        return base.retain()
    }

    override fun setLongLE(index: Int, value: Long): ByteBuf {
        return base.setLongLE(index, value)
    }

    override fun readMediumLE(): Int {
        return base.readMediumLE()
    }

    override fun readUnsignedIntLE(): Long {
        return base.readUnsignedIntLE()
    }

    override fun hasArray(): Boolean {
        return base.hasArray()
    }

    override fun isReadable(): Boolean {
        return base.isReadable()
    }

    override fun isReadable(size: Int): Boolean {
        return base.isReadable(size)
    }

    override fun getChar(index: Int): Char {
        return base.getChar(index)
    }

    override fun release(): Boolean {
        return base.release()
    }

    override fun release(decrement: Int): Boolean {
        return base.release(decrement)
    }

    override fun retainedDuplicate(): ByteBuf {
        return base.retainedDuplicate()
    }

    override fun writableBytes(): Int {
        return base.writableBytes()
    }
}
