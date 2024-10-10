package com.rlrio.validators

import java.lang.reflect.Field
import kotlin.reflect.KClass

interface Validator {
    fun isValid(value: Any, field: Field): Boolean
    fun getValidationAnnotation(): KClass<out Annotation>
}