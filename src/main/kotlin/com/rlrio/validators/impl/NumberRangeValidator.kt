package com.rlrio.validators.impl

import com.rlrio.annotations.NumberRange
import com.rlrio.validators.Validator
import java.lang.reflect.Field
import kotlin.reflect.KClass
import org.slf4j.LoggerFactory

class NumberRangeValidator : Validator {
    private val log = LoggerFactory.getLogger(NumberRangeValidator::class.java)

    override fun isValid(value: Any, field: Field): Boolean {
        val annotation = field.getAnnotation(NumberRange::class.java)
        return isInNumberRange(value, annotation.min, annotation.max)
    }

    override fun getValidationAnnotation(): KClass<out Annotation> {
        return NumberRange::class
    }

    private fun isInNumberRange(value: Any, min: Double, max: Double): Boolean {
        if (value !is Number) {
            log.warn("value is not number")
            return false
        }
        if (min >= max) {
            log.warn("incorrect values, max should be greater than min")
            return false
        }
        return value.toDouble() in min..max
    }
}