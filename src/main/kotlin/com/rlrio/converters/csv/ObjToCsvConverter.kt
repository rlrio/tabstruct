package com.rlrio.converters.csv

import com.rlrio.annotations.Column
import com.rlrio.converters.ObjToSpreadsheetConverter

import com.rlrio.common.util.CommonUtil.format

class ObjToCsvConverter(private val delimiter: String = ",",
                        private val lineSeparator: String = System.lineSeparator()
): ObjToSpreadsheetConverter {

    override fun <T : Any> convert(list: List<T>?): ByteArray? {
        if (list.isNullOrEmpty()) return null

        val clazz = list.first()::class.java
        val fields = clazz.declaredFields

        val headers = fields.map { field ->
            field.isAccessible = true
            var headerName = field.getAnnotation(Column::class.java)?.name
            if (headerName.isNullOrBlank()) {
                headerName = field.name
            }
            headerName!!
        }

        val sb = StringBuilder()
        sb.append(headers.joinToString(delimiter)).append(lineSeparator)

        for (item in list) {
            val values = fields.map { field ->
                field.isAccessible = true
                format(field, item)
            }
            sb.append(values.joinToString(delimiter)).append(System.lineSeparator())
        }

        return sb.toString().toByteArray()
    }
}