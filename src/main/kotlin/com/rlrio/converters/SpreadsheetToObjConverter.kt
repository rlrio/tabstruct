package com.rlrio.converters

interface SpreadsheetToObjConverter {
    fun <T : Any> convert(source: ByteArray?, clazz: Class<T>): List<T>
}