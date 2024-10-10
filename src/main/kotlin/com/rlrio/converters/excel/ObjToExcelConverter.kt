package com.rlrio.converters.excel

import com.rlrio.annotations.Column
import com.rlrio.common.util.formatBooleanValue
import com.rlrio.converters.ObjToSpreadsheetConverter
import com.rlrio.common.util.DateTimeFormatUtil.formatDateTimeValue
import com.rlrio.common.util.NumericFormatUtil.formatDecimalValue
import com.rlrio.common.util.NumericFormatUtil.formatIntValue
import java.io.ByteArrayOutputStream
import java.math.BigDecimal
import java.time.temporal.Temporal
import java.util.Date
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.xssf.usermodel.XSSFWorkbook

class ObjToExcelConverter: ObjToSpreadsheetConverter {
    override fun <T : Any> convert(list: List<T>?): ByteArray? {
        if (list.isNullOrEmpty()) return null

        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Sheet1")
        val headerRow = sheet.createRow(0)
        val clazz = list.first()::class.java
        val fields = clazz.declaredFields

        fields.forEachIndexed { index, field ->
            field.isAccessible = true
            var headerName = field.getAnnotation(Column::class.java)?.name
            if (headerName.isNullOrBlank()) {
                headerName = field.name
            }
            val cell = headerRow.createCell(index, CellType.STRING)
            cell.setCellValue(headerName)
        }

        list.forEachIndexed { rowIndex, item ->
            val row = sheet.createRow(rowIndex + 1)
            fields.forEachIndexed { colIndex, field ->
                field.isAccessible = true
                val value = field.get(item)
                val cell = row.createCell(colIndex)

                cell.cellType = when (value) {
                    is Boolean -> CellType.BOOLEAN
                    is Temporal, is Date -> CellType.STRING
                    is Float, is Double, is BigDecimal, is Int -> CellType.NUMERIC
                    else -> CellType.STRING
                }

                cell.setCellValue(when (value) {
                    is Boolean -> formatBooleanValue(value, field)
                    is Temporal -> formatDateTimeValue(value, field)
                    is Date -> formatDateTimeValue(value, field)
                    is Float -> formatDecimalValue(value, field)
                    is Double -> formatDecimalValue(value, field)
                    is BigDecimal -> formatDecimalValue(value, field)
                    is Int -> formatIntValue(value, field)
                    else -> value?.toString() ?: ""
                })
            }
        }

        return ByteArrayOutputStream().use { outputStream ->
            workbook.write(outputStream)
            workbook.close()
            outputStream.toByteArray()
        }
    }
}