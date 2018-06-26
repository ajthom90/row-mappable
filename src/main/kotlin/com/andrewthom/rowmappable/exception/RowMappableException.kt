package com.andrewthom.rowmappable.exception

import kotlin.reflect.KClass

open class RowMappableException(message: String): Exception(message)

class NotADataClassException(klass: KClass<*>): RowMappableException("${klass.simpleName} is not a data class.")
class NoColumnsFoundException(klass: KClass<*>): RowMappableException("${klass.simpleName} does not have any properties annotated with @Column.")
class TooManyConstructorsException(klass: KClass<*>): RowMappableException("${klass.simpleName} has more than one constructor.")
