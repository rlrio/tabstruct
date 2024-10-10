package com.rlrio.common.util

import com.rlrio.validators.ValidationProcessor
import java.lang.reflect.Field
import java.math.BigDecimal
import java.net.URI
import java.net.URL
import java.time.temporal.TemporalAccessor
import java.util.Date
import java.util.Locale
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DateUtil
import org.apache.poi.ss.usermodel.Row
import org.slf4j.LoggerFactory

import com.rlrio.common.util.DateTimeFormatUtil.parseDateCell
import com.rlrio.common.util.DateTimeFormatUtil.toDateTimeOrNull
import com.rlrio.common.util.DateTimeFormatUtil.toUtilDateOrNull
import com.rlrio.common.util.NumericFormatUtil.parseNumericCell

object ParseUtil {
    private val log = LoggerFactory.getLogger(ParseUtil::class.java)

    fun <T : Any> getInstanceWithValues(clazz: Class<T>,
                                        fieldMap: Map<Int, Field>,
                                        values: List<String>,
                                        locale: Locale,
                                        validationProcessor: ValidationProcessor?,
                                        addToListIfParseError: Boolean
    ): T? {
        if (clazz.kotlin.isData) {
            return getInstanceForDataClass(clazz, values, fieldMap, locale, validationProcessor, addToListIfParseError)
        }
        return getInstance(clazz, values, fieldMap, locale, validationProcessor, addToListIfParseError)
    }

    fun <T : Any> getInstanceWithValues(clazz: Class<T>,
                                        fieldMap: Map<String, Field>,
                                        headers: List<String>,
                                        row: Row,
                                        locale: Locale,
                                        validationProcessor: ValidationProcessor?,
                                        addToListIfParseError: Boolean
    ): T? {
        if (clazz.kotlin.isData) {
            return getInstanceForDataClassFromExcelRow(clazz, fieldMap, headers, row, locale, validationProcessor, addToListIfParseError)
        }
        return getInstanceFromExcelRow(clazz, fieldMap, headers, row, locale, validationProcessor, addToListIfParseError)
    }

    private fun <T : Any> getInstanceForDataClass(
        clazz: Class<T>,
        values: List<String>,
        fieldMap: Map<Int, Field>,
        locale: Locale,
        validationProcessor: ValidationProcessor?,
        addToListIfParseError: Boolean
    ): T? {
        val primaryConstructor = clazz.kotlin.primaryConstructor
        val args = mutableMapOf<KParameter, Any?>()
        values.forEachIndexed { index, value ->
            fieldMap[index]?.let { field ->
                val parameter = primaryConstructor?.parameters?.get(index)!!
                val parsedValue = parseStringCell(value, field, locale)
                if (parsedValue == null || validationProcessor?.isValid(parsedValue, field) == false) {
                    log.error("value is null or not valid: $parsedValue")
                    if (!addToListIfParseError) {
                        return null
                    }
                    return@forEachIndexed
                }
                args[parameter] = parsedValue
            }
        }
        return primaryConstructor?.callBy(args)
    }

    private fun <T : Any> getInstanceForDataClassFromExcelRow(clazz: Class<T>,
                                                              fieldMap: Map<String, Field>,
                                                              headers: List<String>,
                                                              row: Row,
                                                              locale: Locale,
                                                              validationProcessor: ValidationProcessor?,
                                                              addToListIfParseError: Boolean
    ): T? {
        val primaryConstructor = clazz.kotlin.primaryConstructor
        val args = mutableMapOf<KParameter, Any?>()
        headers.forEachIndexed { index, header ->
            val field = fieldMap[header] ?: return@forEachIndexed
            val cell = row.getCell(index)
            if (cell != null) {
                val parsedValue = parseCellValue(cell, field, locale)
                if (parsedValue == null || validationProcessor?.isValid(parsedValue, field) == false) {
                    log.error("value is not valid: $parsedValue")
                    if (!addToListIfParseError) {
                        return null
                    }
                    return@forEachIndexed
                }
                val parameter = primaryConstructor?.parameters?.get(index)!!
                args[parameter] = parsedValue
            }
        }
        return primaryConstructor?.callBy(args)
    }

    private fun <T : Any> getInstance(
        clazz: Class<T>,
        values: List<String>,
        fieldMap: Map<Int, Field>,
        locale: Locale,
        validationProcessor: ValidationProcessor?,
        addToListIfParseError: Boolean
    ): T? {
        val instance = clazz.getDeclaredConstructor().newInstance()
        values.forEachIndexed { index, value ->
            fieldMap[index]?.let { field ->
                val parsedValue = parseStringCell(value, field, locale)
                if (parsedValue == null || validationProcessor?.isValid(parsedValue, field) == false) {
                    log.error("value is null or not valid: $parsedValue")
                    if (!addToListIfParseError) {
                        return null
                    }
                    return@forEachIndexed
                }
                setFieldValue(instance, field, parsedValue)
            }
        }
        return instance
    }

    private fun <T : Any> getInstanceFromExcelRow(
        clazz: Class<T>,
        fieldMap: Map<String, Field>,
        headers: List<String>,
        row: Row,
        locale: Locale,
        validationProcessor: ValidationProcessor?,
        addToListIfParseError: Boolean
    ): T? {
        val instance = clazz.getDeclaredConstructor().newInstance()
        headers.forEachIndexed { index, header ->
            val field = fieldMap[header] ?: return@forEachIndexed
            val cell = row.getCell(index)
            if (cell != null) {
                val parsedValue = parseCellValue(cell, field, locale)
                if (parsedValue == null || validationProcessor?.isValid(parsedValue, field) == false) {
                    log.error("value is not valid: $parsedValue")
                    if (!addToListIfParseError) {
                        return null
                    }
                    return@forEachIndexed
                }
                setFieldValue(instance, field, parsedValue)
            }
        }
        return instance
    }

    private fun parseStringCell(value: String, field: Field, locale: Locale): Any? {
        val type = field.type
        return when {
            type.isClassOrSubclassOf(TemporalAccessor::class.java) -> value.toDateTimeOrNull(field, field.type.kotlin, locale)
            type.isClassOrSubclassOf(Date::class.java)-> value.toUtilDateOrNull(field, locale)
            type.isClassOrSubclassOf(Boolean::class.java) || type.isClassOrSubclassOf(java.lang.Boolean::class.java) -> value.toBooleanOrNull(field)
            type.isClassOrSubclassOf(Int::class.java) || type.isClassOrSubclassOf(Integer::class.java) -> value.toIntOrNull()
            type.isClassOrSubclassOf(Long::class.java) || type.isClassOrSubclassOf(java.lang.Long::class.java) -> value.toLongOrNull()
            type.isClassOrSubclassOf(Float::class.java) || type.isClassOrSubclassOf(java.lang.Float::class.java) -> value.toFloatOrNull()
            type.isClassOrSubclassOf(Double::class.java) || type.isClassOrSubclassOf(java.lang.Double::class.java) -> value.toDoubleOrNull()
            type.isClassOrSubclassOf(BigDecimal::class.java) -> value.toBigDecimalOrNull()
            type.isClassOrSubclassOf(URL::class.java) -> value.toUrl()
            type.isClassOrSubclassOf(URI::class.java) -> value.toUri()
            type.isClassOrSubclassOf(String::class.java) -> value
            else -> {
                log.warn("Unsupported field type '${field.type.kotlin.simpleName}' for value '$value'. Skipping...")
                null
            }
        }
    }

    private fun parseCellValue(cell: Cell?, field: Field, locale: Locale = Locale.getDefault()): Any? {
        if (cell == null) return null

        return when (cell.cellType) {
            CellType.BOOLEAN -> cell.booleanCellValue
            CellType.NUMERIC -> if (DateUtil.isCellDateFormatted(cell)) {
                parseDateCell(cell.dateCellValue, field)
            } else {
                parseNumericCell(cell.numericCellValue, field)
            }
            CellType.STRING -> parseStringCell(cell.stringCellValue, field, locale)
            CellType.BLANK -> null
            else -> {
                log.warn("Unexpected cell type: ${cell.cellType}. Field: ${field.name}")
                null
            }
        }
    }

    private fun setFieldValue(instance: Any, field: Field, value: Any?) {
        try {
            if (field.trySetAccessible()) {
                field.set(instance, value)
            } else {
                log.warn("Could not set field '${field.name}' accessible. Value not set for the field.")
            }
        } catch (e: IllegalAccessException) {
            log.error("Failed to set value for field '${field.name}': ${e.message}", e)
        }
    }

    private fun <T: Any, U: Any> Class<T>.isClassOrSubclassOf(clazz: Class<U>): Boolean {
        return clazz.isAssignableFrom(this)
    }

    private fun String.toUrl(): URL? {
        try {
            return URL(this)
        } catch (e: Exception) {
            log.warn("invalid url $this", e)
            return null
        }
    }

    private fun String.toUri(): URI? {
        try {
            return URI(this)
        } catch (e: Exception) {
            log.warn("invalid uri $this", e)
            return null
        }
    }
}