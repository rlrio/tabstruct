package com.rlrio.converters.excel

import com.rlrio.model.Person
import com.rlrio.util.outputToFile
import com.rlrio.util.readFromXlsxFile
import com.rlrio.util.testOutputXlsxFileName
import java.io.File
import model.PersonJava
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull

class ObjToExcelConverterTest {
    private lateinit var converter: ObjToExcelConverter

    @BeforeEach
    fun setUp() {
        converter = ObjToExcelConverter()
    }

    @AfterEach
    fun tearDown() {
        val testFile = File(testOutputXlsxFileName)
        if (testFile.exists()) {
            testFile.delete()
        }
    }

    @Test
    fun `convert should correctly map Person objects to Excel`() {
        // given
       val listGiven = listOf(
           Person(name = "Jane", age = 17, email = "jane@mail.com"),
           Person(name = "Sandra", age = 18, email = "mary@mail.com")
       )

        // when
        val result = converter.convert(listGiven)
        assertNotNull(result)
        outputToFile(testOutputXlsxFileName, result!!)

        // then
        val fileData = readFromXlsxFile(testOutputXlsxFileName)
        val person1DataFromFile = fileData[1].split(",")
        assertEquals(listGiven[0].name, person1DataFromFile[0])
        assertEquals(listGiven[0].age.toString(), person1DataFromFile[1])
        assertEquals(listGiven[0].email, person1DataFromFile[2])
        val person2DataFromFile = fileData[2].split(",")
        assertEquals(listGiven[1].name, person2DataFromFile[0])
        assertEquals(listGiven[1].age.toString(), person2DataFromFile[1])
        assertEquals(listGiven[1].email, person2DataFromFile[2])
    }

    @Test
    fun `convert should correctly map PersonJava objects to Excel`() {
        // given
        val listGiven = listOf(
            PersonJava().apply {
                name = "John"
                age = 17
                email = "john@mail.com"
            }
        )

        // when
        val result = converter.convert(listGiven)
        assertNotNull(result)
        outputToFile(testOutputXlsxFileName, result!!)

        // then
        val fileData = readFromXlsxFile(testOutputXlsxFileName)
        assertEquals(2,fileData.size)
        val personDataFromFile = fileData[1].split(",")
        assertEquals(listGiven[0].name, personDataFromFile[0])
        assertEquals(listGiven[0].age.toString(), personDataFromFile[1])
        assertEquals(listGiven[0].email, personDataFromFile[2])
    }
}