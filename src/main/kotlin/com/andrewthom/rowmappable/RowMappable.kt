package com.andrewthom.rowmappable

import com.andrewthom.rowmappable.exception.NoColumnsFoundException
import com.andrewthom.rowmappable.exception.NotADataClassException
import com.andrewthom.rowmappable.exception.TooManyConstructorsException
import org.springframework.jdbc.core.RowMapper
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.annotation.AnnotationTarget.PROPERTY
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation

object RowMapperMaker {
	/**
	 * Makes a row mapper for the given data class.  The {@link KClass} given must
	 * be on a data class.  If it is not a data class, the method will throw an
	 * exception immediately.  The data class may have one and only one constructor.
	 */
	fun <T: Any> makeRowMapper(klass: KClass<T>): RowMapper<T> {
		if (!klass.isData) {
			throw NotADataClassException(klass)
		}
		if (klass.constructors.count() > 1) {
			throw TooManyConstructorsException(klass)
		}
		val fieldToColumn = mutableMapOf<String, String>()
		for (field in klass.members) {
			val annotation = field.findAnnotation<Column>()
			if (annotation != null) {
				val name = annotation.name
				if (name == NO_COLUMN_NAME) {
					fieldToColumn[field.name] = field.name
				} else {
					fieldToColumn[field.name] = annotation.name
				}
			}
		}
		if (fieldToColumn.isEmpty()) {
			throw NoColumnsFoundException(klass)
		}
		val constructor = klass.constructors.first()
		return RowMapper { rs, _ ->
			val callParameterMap = mutableMapOf<KParameter, Any?>()
			for (parameter in constructor.parameters) {
				if (!fieldToColumn.containsKey(parameter.name)) {
					continue
				}
				val columnName = fieldToColumn[parameter.name]
				val value: Any? = when(parameter.type) {
					String::class -> rs.getString(columnName)
					Int::class -> rs.getInt(columnName)
					Boolean::class -> rs.getBoolean(columnName)
					BigDecimal::class -> rs.getBigDecimal(columnName)
					LocalDate::class -> rs.getDate(columnName).toLocalDate()
					LocalDateTime::class -> rs.getTimestamp(columnName).toLocalDateTime()
					else -> rs.getString(columnName)
				}
				if (value != null) {
					callParameterMap[parameter] = value
				} else {
					if (!parameter.isOptional) {
						callParameterMap[parameter] = value
					}
				}
			}
			return@RowMapper constructor.callBy(callParameterMap)
		}
	}
}

@Target(PROPERTY)
annotation class Column(val name: String = NO_COLUMN_NAME)

private const val NO_COLUMN_NAME = "FDJSAIOFJDSAIOFHJDOIAHSOFJSDAIOJFIDSAJSFD"

fun <T: Any> makeRowMapper(klass: KClass<T>): RowMapper<T> {
	return RowMapperMaker.makeRowMapper(klass)
}
