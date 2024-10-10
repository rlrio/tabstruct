package com.rlrio.annotations

import com.rlrio.common.util.NumericFormatUtil.DECIMAL_DEFAULT_FORMAT

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class DecimalFormatter(
    val format: String = DECIMAL_DEFAULT_FORMAT
)
