package com.rlrio.annotations

import com.rlrio.common.util.DateTimeFormatUtil.DATE_DEFAULT_FORMAT

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Pattern(val pattern: String)

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Email

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Url

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Phone

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class DateRange(val from: String = "", val to: String = "", val dateFormat: String = DATE_DEFAULT_FORMAT)

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class NumberRange(val min: Double = Double.MIN_VALUE, val max: Double = Double.MAX_VALUE)
