package com.rlrio.common.util

import java.lang.reflect.Field
import java.math.BigDecimal
import java.time.temporal.Temporal
import java.util.Date
import java.util.Locale
import org.slf4j.LoggerFactory

import com.rlrio.common.util.DateTimeFormatUtil.formatDateTimeValue
import com.rlrio.common.util.NumericFormatUtil.formatDecimalValue

object CommonUtil {
    private val log = LoggerFactory.getLogger(CommonUtil::class.java)

    fun String.format(format: String, locale: Locale = Locale.getDefault()): String {
        return String.format(locale, "%${format}", this)
    }

    fun format(field: Field, item: Any): String {
        return when (val value = field.get(item)) {
            is Boolean -> formatBooleanValue(value, field)
            is Temporal -> formatDateTimeValue(value, field)
            is Date -> formatDateTimeValue(value, field)
            is Float, Double-> formatDecimalValue(value, field) ?: ""
            is BigDecimal -> formatDecimalValue(value, field) ?: ""
            else -> value?.toString() ?: ""
        }
    }

    fun isStringValue(value: Any): Boolean {
        if (value !is String) {
            log.warn("value is not a string and cannot be validated")
            return false
        }
        return true
    }
}