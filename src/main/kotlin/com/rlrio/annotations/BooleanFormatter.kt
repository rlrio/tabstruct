package com.rlrio.annotations

import com.rlrio.common.util.BooleanFormatConstants.FALSE_DEFAULT_STRING_VALUE
import com.rlrio.common.util.BooleanFormatConstants.TRUE_DEFAULT_STRING_VALUE

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class BooleanFormatter(
    val trueValue: String = TRUE_DEFAULT_STRING_VALUE,
    val falseValue: String = FALSE_DEFAULT_STRING_VALUE
)
