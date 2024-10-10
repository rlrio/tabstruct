package com.rlrio.util

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import org.apache.poi.xssf.usermodel.XSSFWorkbook

const val testOutputXlsxFileName = "src/test/resources/testOutput.xlsx"
const val testOutputCsvFileName = "src/test/resources/testOutput.csv"
const val testXlsxFileName = "src/test/resources/test.xlsx"
const val testCsvFileName = "src/test/resources/test.csv"


fun outputToFile(fileName: String, data: ByteArray) {
    try {
        val file = File(fileName)
        FileOutputStream(file).use { outputStream ->
            outputStream.write(data)
        }
    } catch (e: IOException) {
        println("Error writing to file: ${e.message}")
    }
}

fun readFromXlsxFile(fileName: String): List<String> {
    val file = File(fileName)
    val result = ArrayList<String>()
    FileInputStream(file).use {
        val workbook = XSSFWorkbook(it)
        workbook.first().forEach { row ->
            val sb = StringBuilder()
            row.forEach { cell ->
                sb.append("${cell.stringCellValue},")
            }
            result.add(sb.toString())
        }
    }
    return result
}

fun readFromCsvFile(fileName: String): List<String> = File(fileName).readLines()

fun readAllBytesFromFile(fileName:String): ByteArray = Files.readAllBytes(Paths.get(fileName))