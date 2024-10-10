package com.rlrio.common.util

import com.rlrio.annotations.DecimalFormatter
import java.lang.reflect.Field
import java.math.BigDecimal
import java.text.DecimalFormat
import org.slf4j.LoggerFactory

object NumericFormatUtil {
    const val DECIMAL_DEFAULT_FORMAT = "#.##"

    private val log = LoggerFactory.getLogger(NumericFormatUtil::class.java)

    fun formatIntValue(value: Int, field: Field): String? {
        val annotation = field.getAnnotation(DecimalFormatter::class.java)
        if (annotation != null) {
            return formatDecimalValue(value.toDouble(), field)
        }
        return value.toString()
    }

    fun formatDecimalValue(value: Any, field: Field): String? {
        val annotation = field.getAnnotation(DecimalFormatter::class.java)
        val format = annotation?.format ?: DECIMAL_DEFAULT_FORMAT
        if (field.type != Double::class.java
            && field.type != Float::class.java
            && field.type != BigDecimal::class.java
            && field.type != Int::class.java
            && field.type != Integer::class.java
            && field.type != Long::class.java) {
            log.warn("Unsupported field type '${field.type.kotlin.simpleName}' for value '$value'. Skipping...")
            return null
        }
        if (field.type == Int::class.java
            || field.type == Integer::class.java
            || field.type == Long::class.java) {
            return value.toString()
        }
        return DecimalFormat(format).format(value)
    }

    fun parseNumericCell(value: Double, field: Field): Any {
        return when (field.type.kotlin) {
            Int::class -> value.toInt()
            Long::class -> value.toLong()
            Float::class -> value.toFloat()
            Double::class -> value
            BigDecimal::class -> BigDecimal(value)
            else -> value
        }
    }
}
