package com.rlrio.converters.excel

import com.rlrio.model.Person
import com.rlrio.util.readAllBytesFromFile
import com.rlrio.util.testXlsxFileName
import com.rlrio.validators.ValidationProcessor
import model.PersonJava
import java.time.LocalDate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.assertEquals

class ExcelToObjConverterTest {
    private lateinit var converter: ExcelToObjConverter

    @BeforeEach
    fun setUp() {
        converter = ExcelToObjConverter(
            batchSize = 2,
            validationProcessor = ValidationProcessor(),
            addToListIfParseError = true
        )
    }

    @Test
    fun `convert should correctly map Excel rows to Person objects`() {
        // given
        val excelData = readAllBytesFromFile(testXlsxFileName)
        val clazz = Person::class.java

        // when
        val result: List<Person> = converter.convert(excelData, clazz)

        // then
        assertEquals(2, result.size)
        assertEquals(Person(name = "John", age = 17, email = "john@mail.com"), result[0])
    }

    @Test
    fun `convert should correctly map Excel rows to PersonJava objects`() {
        // given
        val excelData = readAllBytesFromFile(testXlsxFileName)
        val clazz = PersonJava::class.java
        val expected = PersonJava().apply {
            name = "John"
            age = 17
            email = "john@mail.com"
            registerDate = LocalDate.of(2024, 1, 1)
        }

        // when
        val result: List<PersonJava> = converter.convert(excelData, clazz)

        // then
        assertEquals(3, result.size)
        assertEquals(expected, result[0])
    }
}