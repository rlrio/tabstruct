package com.rlrio.annotations

import com.rlrio.common.util.IsoFormat

import com.rlrio.common.util.DateTimeFormatUtil.DATE_TIME_DEFAULT_FORMAT

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class TemporalFormatter(
    val format: String = DATE_TIME_DEFAULT_FORMAT,
    val iso: IsoFormat = IsoFormat.NONE
)
