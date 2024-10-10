package com.rlrio.converters

interface ObjToSpreadsheetConverter {
    fun <T : Any> convert(list: List<T>?): ByteArray?
}