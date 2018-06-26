package com.andrewthom.rowmappable

import com.andrewthom.rowmappable.exception.NoColumnsFoundException
import com.andrewthom.rowmappable.exception.NotADataClassException
import com.andrewthom.rowmappable.exception.TooManyConstructorsException
import org.junit.Test
import org.mockito.Mockito
import java.math.BigDecimal
import java.sql.Date
import java.sql.ResultSet
import java.time.LocalDate
import kotlin.test.assertEquals

class RowMappableApplicationTests {
	@Test
	fun test_simpleObject() {
		val mapper = RowMapperMaker.makeRowMapper(TestClass::class)
		val resultSet = Mockito.mock(ResultSet::class.java)
		Mockito.`when`(resultSet.getString("name")).thenReturn("Name from DB")
		Mockito.`when`(resultSet.getString("id")).thenReturn("ID from DB")
		val mapped = mapper.mapRow(resultSet, 0)

		val testValue = TestClass("ID from DB", "Name from DB")
		assertEquals(testValue, mapped, "Test Value should equal the mapped value")
	}

	@Test
	fun test_simpleObject_withPropertyNonAnnotated() {
		val mapper = RowMapperMaker.makeRowMapper(TestClass3::class)
		val resultSet = Mockito.mock(ResultSet::class.java)
		Mockito.`when`(resultSet.getString("name")).thenReturn("Name from DB")
		Mockito.`when`(resultSet.getString("id")).thenReturn("ID from DB")
		val mapped = mapper.mapRow(resultSet, 0)

		val testValue = TestClass3("ID from DB", "Name from DB")
		assertEquals(testValue, mapped, "Test Value should equal the mapped value")
	}

	@Test
	fun moreComplexObject() {
		val mapper = makeRowMapper(TestClass2::class)
		val resultSet = Mockito.mock(ResultSet::class.java)
		Mockito.`when`(resultSet.getString("name")).thenReturn("Name from DB")
		Mockito.`when`(resultSet.getString("id")).thenReturn("ID from DB")
		Mockito.`when`(resultSet.getDate("date")).thenReturn(Date(2018, 8, 1))
		Mockito.`when`(resultSet.getBigDecimal("price_of_object")).thenReturn(BigDecimal("18.95"))
		val mapped = mapper.mapRow(resultSet, 0)
		println(mapped)
	}

	@Test(expected = NotADataClassException::class)
	fun test_NotADataClass() {
		RowMapperMaker.makeRowMapper(NotADataClass::class)
	}

	@Test(expected = TooManyConstructorsException::class)
	fun test_tooManyConstructors() {
		RowMapperMaker.makeRowMapper(MultipleConstructorsClass::class)
	}

	@Test(expected = NoColumnsFoundException::class)
	fun test_noColumns() {
		RowMapperMaker.makeRowMapper(NoAnnotatedColumns::class)
	}
}

data class TestClass(@Column val id: String, @Column val name: String)
data class TestClass2(@Column val id: String, @Column val name: String, @Column val date: LocalDate?, @Column("price_of_object") val price: BigDecimal?)
data class TestClass3(@Column val id: String, @Column val name: String, val price: BigDecimal = BigDecimal.ZERO)
class NotADataClass
data class MultipleConstructorsClass(@Column val id: String, @Column val name: String) {
	constructor(id: String): this(id, "")
}
data class NoAnnotatedColumns(val id: String)