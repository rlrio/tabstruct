package com.rlrio.converters.csv

import com.rlrio.model.Person
import com.rlrio.util.readAllBytesFromFile
import com.rlrio.util.testCsvFileName
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import model.PersonJava

import org.junit.jupiter.api.Assertions.assertEquals

class CsvToObjConverterTest {
    private lateinit var converter: CsvToObjConverter

    @BeforeEach
    fun setUp() {
        converter = CsvToObjConverter(batchSize = 2, addToListIfParseError = true)
    }

    @Test
    fun `convert should correctly map csv rows to Person objects`() {
        // given
        val csvData = readAllBytesFromFile(testCsvFileName)
        val clazz = Person::class.java

        // when
        val result: List<Person> = converter.convert(csvData, clazz)

        // then
        assertEquals(2, result.size)
        assertEquals(Person(name = "John", age = 17, email = "john@mail.com"), result[0])
    }

    @Test
    fun `convert should correctly map csv rows to PersonJava objects`() {
        // given
        val csvData = readAllBytesFromFile(testCsvFileName)
        val clazz = PersonJava::class.java
        val expected = PersonJava().apply {
            name = "John"
            age = 17
            email = "john@mail.com"
        }

        // when
        val result: List<PersonJava> = converter.convert(csvData, clazz)

        // then
        assertEquals(2, result.size)
        assertEquals(expected, result[0])
    }
}