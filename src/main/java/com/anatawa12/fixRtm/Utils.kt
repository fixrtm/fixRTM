package com.anatawa12.fixRtm


fun <E> List<E?>.isAllNotNull(): Boolean = all { it != null }
fun <E> Array<E?>.isAllNotNull(): Boolean = all { it != null }
