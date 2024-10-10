package com.rlrio.validators.impl

import com.rlrio.annotations.Pattern
import com.rlrio.validators.Validator
import java.lang.reflect.Field
import kotlin.reflect.KClass
import org.slf4j.LoggerFactory

import com.rlrio.common.util.CommonUtil.isStringValue

class PatternValidator : Validator {
    private val log = LoggerFactory.getLogger(PatternValidator::class.java)

    override fun isValid(value: Any, field: Field): Boolean {
        val annotation = field.getAnnotation(Pattern::class.java)
        return isPatternValid(value as String, annotation.pattern)
    }

    override fun getValidationAnnotation(): KClass<out Annotation> {
        return Pattern::class
    }

    private fun isPatternValid(value: Any, pattern: String): Boolean {
        if (!isStringValue(value)) return false
        try {
            return (value as String).matches(pattern.toRegex())
        } catch (e: Exception) {
            log.warn("pattern is not valid: $pattern")
            return false
        }
    }
}