package com.rlrio.common.util

import com.rlrio.annotations.BooleanFormatter
import java.lang.reflect.Field


object BooleanFormatConstants {
    const val TRUE_DEFAULT_STRING_VALUE = "true"
    const val FALSE_DEFAULT_STRING_VALUE = "false"
}

fun formatBooleanValue(value: Boolean, field: Field): String {
    val annotation = field.getAnnotation(BooleanFormatter::class.java)
    return when {
        annotation != null -> if (value) annotation.trueValue else annotation.falseValue
        else -> value.toString()
    }
}

fun String.toBooleanOrNull(field: Field?): Boolean? {
    val format = field?.getAnnotation(BooleanFormatter::class.java)
    return when {
        format != null -> {
            when (this) {
                format.trueValue -> true
                format.falseValue -> false
                else -> null
            }
        }
        else -> this.toBoolean()
    }
}
