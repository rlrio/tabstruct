package com.rlrio.converters.csv

import com.rlrio.annotations.Column
import com.rlrio.converters.SpreadsheetToObjConverter
import com.rlrio.validators.ValidationProcessor
import java.lang.reflect.Field
import java.util.Locale
import org.slf4j.LoggerFactory

import com.rlrio.common.util.ParseUtil.getInstanceWithValues

class CsvToObjConverter(private val delimiter: String = ",",
                        private val locale: Locale = Locale.getDefault(),
                        private val startRow: Int = 1,
                        private val batchSize: Int = 1000,
                        private val validationProcessor: ValidationProcessor? = null,
                        private val addToListIfParseError: Boolean = false
): SpreadsheetToObjConverter {
    private val log = LoggerFactory.getLogger(CsvToObjConverter::class.java)

    override fun <T : Any> convert(source: ByteArray?, clazz: Class<T>): List<T> {
        if (source == null || source.isEmpty()) {
            log.error("Source byte array is null or empty. Conversion cannot proceed.")
            return emptyList()
        }

        val resultList = mutableListOf<T>()

        try {
            source.inputStream().bufferedReader().use { reader ->
                var lineNumber = 0
                var batchCount = 0
                val batchLines = mutableListOf<String>()

                val headers = reader.readLine()?.split(delimiter)?.map { it.trim() } ?: run {
                    log.error("Failed to read header line from CSV source.")
                    return emptyList()
                }
                val fieldMap = mapHeaderToFields(headers, clazz)

                reader.lineSequence()
                    .drop(startRow - 1)
                    .forEach { line ->
                        lineNumber++
                        batchLines.add(line)
                        batchCount++

                        if (batchCount >= batchSize) {
                            resultList.addAll(processBatch(batchLines, fieldMap, clazz, addToListIfParseError))
                            batchLines.clear()
                            batchCount = 0
                        }
                    }

                if (batchLines.isNotEmpty()) {
                    resultList.addAll(processBatch(batchLines, fieldMap, clazz, addToListIfParseError))
                }
            }
        } catch (e: Exception) {
            log.error("Error while processing CSV source: {}", e.message, e)
        }

        return resultList
    }

    private fun <T : Any> mapHeaderToFields(headers: List<String>, clazz: Class<T>): Map<Int, Field> {
        val fieldMap = mutableMapOf<Int, Field>()
        val fields = clazz.declaredFields

        headers.forEachIndexed { index, header ->
            val field = fields.find {
                it.isAnnotationPresent(Column::class.java)
                        && it.getAnnotation(Column::class.java).name == header
            } ?: fields.find { it.name.equals(header, ignoreCase = true) }

            if (field != null) {
                fieldMap[index] = field
            } else {
                log.warn("No matching field found for header: '{}'", header)
            }
        }

        return fieldMap
    }

    private fun <T : Any> processBatch(
        lines: List<String>,
        fieldMap: Map<Int, Field>,
        clazz: Class<T>,
        addToListIfParseError: Boolean
    ): List<T> {
        val batchResult = mutableListOf<T>()

        lines.forEach { line ->
            val values = line.split(delimiter).map { it.trim() }
            try {
                getInstanceWithValues(
                    clazz,
                    fieldMap,
                    values,
                    locale,
                    validationProcessor,
                    addToListIfParseError
                )?.let { batchResult.add(it) }
            } catch (e: Exception) {
                log.error("Error converting line to object: '{}'. Reason: {}", line, e.message, e)
            }
        }

        return batchResult
    }
}