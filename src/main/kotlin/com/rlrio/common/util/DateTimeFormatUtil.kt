package com.rlrio.common.util

import com.rlrio.common.exception.ParseException
import com.rlrio.annotations.TemporalFormatter
import java.lang.reflect.Field
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.Temporal
import java.time.temporal.TemporalAccessor
import java.util.Date
import java.util.Locale
import kotlin.reflect.KClass
import org.slf4j.LoggerFactory

enum class IsoFormat(val format: DateTimeFormatter?) {
    NONE(null),
    ISO_LOCAL_DATE(DateTimeFormatter.ISO_LOCAL_DATE),
    ISO_LOCAL_TIME(DateTimeFormatter.ISO_LOCAL_TIME),
    ISO_LOCAL_DATE_TIME(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
    ISO_OFFSET_TIME(DateTimeFormatter.ISO_OFFSET_TIME),
    ISO_OFFSET_DATE_TIME(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
    ISO_INSTANT(DateTimeFormatter.ISO_INSTANT),
    ISO_ZONED_DATE_TIME(DateTimeFormatter.ISO_ZONED_DATE_TIME)
}

object DateTimeFormatUtil {
    const val DATE_TIME_DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss"
    const val DATE_DEFAULT_FORMAT = "yyyy-MM-dd"

    private val log = LoggerFactory.getLogger(DateTimeFormatUtil::class.java)

    private val classToFormatterMap: Map<Class<out TemporalAccessor>, DateTimeFormatter> = mapOf(
        LocalDate::class.java to DateTimeFormatter.ISO_LOCAL_DATE,
        LocalTime::class.java to DateTimeFormatter.ISO_LOCAL_TIME,
        LocalDateTime::class.java to DateTimeFormatter.ISO_LOCAL_DATE_TIME,
        OffsetTime::class.java to DateTimeFormatter.ISO_OFFSET_TIME,
        OffsetDateTime::class.java to DateTimeFormatter.ISO_OFFSET_DATE_TIME,
        Instant::class.java to DateTimeFormatter.ISO_INSTANT,
        ZonedDateTime::class.java to DateTimeFormatter.ISO_ZONED_DATE_TIME
    )

    fun formatDateTimeValue(value: Temporal, field: Field): String {
        val formatter = getFormatterFromField(field) ?: getFormatterByClass(value::class.java)!!
        return formatter.format(value)
    }

    fun formatDateTimeValue(value: Date, field: Field): String {
        val formatter = getFormatterFromField(field) ?: DateTimeFormatter.ofPattern(DATE_TIME_DEFAULT_FORMAT)
        return formatter.format(value.toInstant())
    }

    fun parseDateCell(value: Date, field: Field): Any? {
        return value.toInstant().let { instant ->
            when (field.type.kotlin) {
                LocalDate::class -> instant.atZone(ZoneId.systemDefault()).toLocalDate()
                LocalDateTime::class -> instant.atZone(ZoneId.systemDefault()).toLocalDateTime()
                ZonedDateTime::class -> instant.atZone(ZoneId.systemDefault())
                OffsetDateTime::class -> OffsetDateTime.ofInstant(instant, ZoneId.systemDefault())
                OffsetTime::class -> OffsetTime.ofInstant(instant, ZoneId.systemDefault())
                Instant::class -> instant
                Date::class -> Date.from(instant)
                else -> value
            }
        }
    }

    fun String.toDateTimeOrNull(field: Field? = null, clazz: KClass<*>, locale: Locale? = null): TemporalAccessor? {
        return parseDate(this, field, clazz, locale)
    }

    fun String.toUtilDateOrNull(formatter: DateTimeFormatter): Date? {
        return try {
            LocalDate.parse(this, formatter).let {
                return Date.from(it.atStartOfDay(ZoneId.systemDefault()).toInstant())
            }
        } catch (e: DateTimeParseException) {
            try {
                LocalDateTime.parse(this, formatter).let {
                    return Date.from(it.atZone(ZoneId.systemDefault()).toInstant())
                }
            } catch (e: DateTimeParseException) {
                try {
                    OffsetDateTime.parse(this, formatter).let {
                        return Date.from(it.toInstant())
                    }
                } catch (e: DateTimeParseException) {
                    try {
                        ZonedDateTime.parse(this, formatter).let {
                            return Date.from(it.toInstant())
                        }
                    } catch (e: DateTimeParseException) {
                        try {
                            Instant.parse(this).let {
                                return Date.from(it)
                            }
                        } catch (e: DateTimeParseException) {
                            log.warn("Error parsing date ${this}: ${e.message}")
                            null
                        }
                    }
                }
            }
        }
    }

    fun String.toUtilDateOrNull(field: Field, locale: Locale? = null): Date? {
        val temporal = parseDate(this, field, field.type.kotlin, locale)
        try {
            if (temporal != null) {
                return Date.from(Instant.from(temporal))
            }
            throw ParseException("Failed to parse Date from string $this")
        } catch (e: Exception) {
            log.warn("Failed to parse Date from string '{}'", this)
            return null
        }
    }

    fun String.getFormatterOfPattern(): DateTimeFormatter? {
        try {
            return DateTimeFormatter.ofPattern(this)
        } catch (e: Exception) {
            log.warn("Failed to get formatter from pattern $this")
            return null
        }
    }

    private fun parseDate(value: String, field: Field?, clazz: KClass<*>, locale: Locale? = null): TemporalAccessor? {
        val formatters = getFormatters(field, locale)

        val res = formatters.mapNotNull { formatter ->
            try {
                when(clazz) {
                    LocalDateTime::class -> LocalDateTime.parse(value, formatter)
                    LocalDate::class -> LocalDate.parse(value, formatter)
                    ZonedDateTime::class -> ZonedDateTime.parse(value, formatter)
                    OffsetTime::class -> OffsetTime.parse(value, formatter)
                    OffsetDateTime::class -> OffsetDateTime.parse(value, formatter)
                    Instant::class -> Instant.parse(value)
                    else -> null
                }
            } catch (e: Exception) {
                null
            }
        }

        if (res.isEmpty()) {
            log.warn("Failed to parse $value for class ${clazz.simpleName}, invalid date format")
            return null
        }
        return res.first()
    }

    private fun getFormatters(field: Field?, locale: Locale? = null): List<DateTimeFormatter> {
        val formatters = mutableSetOf<DateTimeFormatter>()
        val formatterFromField = getFormatterFromField(field)
        if (formatterFromField != null) {
            formatters.add(formatterFromField)
        }

        formatters.addAll(classToFormatterMap.values)
        formatters.add(DateTimeFormatter.ofPattern(DATE_TIME_DEFAULT_FORMAT))


        return formatters.map { formatter -> if (locale != null) formatter.withLocale(locale) else formatter }
    }

    private fun getFormatterByClass(clazz: Class<out TemporalAccessor>?): DateTimeFormatter? {
        if (clazz == null) {
            return null
        }
        return classToFormatterMap[clazz]
    }

    private fun getFormatterFromField(field:Field?): DateTimeFormatter? {
        if (field == null) {
            return null
        }
        val annotation = field.getAnnotation(TemporalFormatter::class.java)
        if (annotation != null) {
            return if (annotation.iso != IsoFormat.NONE) {
                annotation.iso.format
            } else {
                DateTimeFormatter.ofPattern(annotation.format)
            }
        }
        return null
    }
}
