package com.rlrio.converters.excel

import com.rlrio.annotations.Column
import com.rlrio.converters.SpreadsheetToObjConverter
import com.rlrio.validators.ValidationProcessor
import java.io.ByteArrayInputStream
import java.lang.reflect.Field
import java.util.Locale
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.LoggerFactory

import com.rlrio.common.util.ParseUtil.getInstanceWithValues

class ExcelToObjConverter(private val startRow: Int = 1,
                          private val batchSize: Int = 1000,
                          private val locale: Locale = Locale.getDefault(),
                          private val validationProcessor: ValidationProcessor? = null,
                          private val addToListIfParseError: Boolean = false
): SpreadsheetToObjConverter {
    private val log = LoggerFactory.getLogger(ExcelToObjConverter::class.java)

    override fun <T : Any> convert(source: ByteArray?, clazz: Class<T>): List<T> {
        if (source == null || source.isEmpty()) {
            log.error("Source byte array is null or empty. Conversion cannot proceed.")
            return emptyList()
        }

        val objects = mutableListOf<T>()

        ByteArrayInputStream(source).use { inputStream ->
            val workbook: Workbook = XSSFWorkbook(inputStream)
            val sheet = workbook.getSheetAt(0)

            val headerRow = sheet.getRow(0) ?: return emptyList()
            val headers = headerRow.map { it.stringCellValue.trim() }

            val fieldMap = mapHeaderToFields(clazz, headers)

            var currentBatch = mutableListOf<T>()
            var rowIndex = startRow

            while (rowIndex < sheet.physicalNumberOfRows) {
                val row = sheet.getRow(rowIndex)
                if (row.all{ it.stringCellValue.isNullOrEmpty()}) {
                    rowIndex++
                    continue
                }

                try {
                    getInstanceWithValues(
                        clazz,
                        fieldMap,
                        headers,
                        row,
                        locale,
                        validationProcessor,
                        addToListIfParseError
                    )?.let { currentBatch.add(it) }
                    if (currentBatch.size >= batchSize) {
                        objects.addAll(currentBatch)
                        currentBatch = mutableListOf()
                    }
                } catch (e: Exception) {
                    log.error("Error while converting row $rowIndex: ${e.message}", e)
                }

                rowIndex++
            }

            if (currentBatch.isNotEmpty()) {
                objects.addAll(currentBatch)
            }
        }

        return objects
    }

    private fun <T : Any> mapHeaderToFields(clazz: Class<T>, headers: List<String>): Map<String, Field> {
        return clazz.declaredFields.associateBy { field ->
            val fieldName = field.getAnnotation(Column::class.java)?.name
            if (!fieldName.isNullOrBlank()) fieldName else field.name
        }.filterKeys { header ->
            headers.contains(header)
        }
    }
}