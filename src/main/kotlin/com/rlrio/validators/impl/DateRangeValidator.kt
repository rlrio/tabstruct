package com.rlrio.validators.impl

import com.rlrio.annotations.DateRange
import com.rlrio.validators.Validator
import java.lang.reflect.Field
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor
import java.util.Date
import kotlin.reflect.KClass
import org.slf4j.LoggerFactory

import com.rlrio.common.util.DateTimeFormatUtil.getFormatterOfPattern
import com.rlrio.common.util.DateTimeFormatUtil.toUtilDateOrNull

class DateRangeValidator : Validator {
    private val log = LoggerFactory.getLogger(DateRangeValidator::class.java)

    override fun isValid(value: Any, field: Field): Boolean {
        val annotation = field.getAnnotation(DateRange::class.java) ?: return true
        return isDateValid(value, annotation.from, annotation.to, annotation.dateFormat)
    }

    override fun getValidationAnnotation(): KClass<out Annotation> {
        return DateRange::class
    }

    private fun <T: Any> isDateValid(value: T, from: String, to: String, dateFormat: String): Boolean {
        if (isDateOrTemporalAccessor(value)) return false
        val formatter = dateFormat.getFormatterOfPattern() ?: return false

        return if (value is TemporalAccessor) {
            isTemporalAccessorValid(value as TemporalAccessor, from, to, formatter)
        } else {
            isDateValid(value as Date, from, to, formatter)
        }
    }

    private fun isDateOrTemporalAccessor(value: Any): Boolean {
        if (value !is Date || value !is TemporalAccessor) {
            log.warn("value is not date")
            return false
        }
        return true
    }

    private fun isTemporalAccessorValid(value: TemporalAccessor, from: String?, to: String?, formatter: DateTimeFormatter): Boolean {
        val valueDate = LocalDate.from(value)

        if (!from.isNullOrBlank()) {
            val fromDate = LocalDate.from(formatter.parse(from))
            if (valueDate.isBefore(fromDate)) {
                log.error("date is before $fromDate")
                return false
            }
        }

        if (!to.isNullOrBlank()) {
            val toDate = LocalDate.from(formatter.parse(to))
            if (valueDate.isAfter(toDate)) {
                log.error("date is after $toDate")
                return false
            }
        }

        return true
    }

    private fun isDateValid(value: Date, from: String?, to: String?, formatter: DateTimeFormatter): Boolean {
        if (!from.isNullOrBlank()) {
            if (from.toUtilDateOrNull(formatter) == null) {
                log.error("incorrect from value ${from} to validate date")
                return false
            }
            if (value.before(from.toUtilDateOrNull(formatter))) {
                log.error("date is before $from")
                return false
            }
        }
        if (!to.isNullOrBlank()) {
            if (to.toUtilDateOrNull(formatter) == null) {
                log.error("incorrect from value ${to} to validate date")
                return false
            }
            if (value.after(to.toUtilDateOrNull(formatter))) {
                log.error("date is after $to")
                return false
            }
        }
        return true
    }
}