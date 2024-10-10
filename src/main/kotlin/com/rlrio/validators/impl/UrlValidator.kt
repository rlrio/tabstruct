package com.rlrio.validators.impl

import com.rlrio.annotations.Url
import com.rlrio.validators.Validator
import java.lang.reflect.Field
import java.net.URI
import java.net.URL
import kotlin.reflect.KClass
import org.slf4j.LoggerFactory

import com.rlrio.common.util.CommonUtil.isStringValue

class UrlValidator : Validator {
    private val log = LoggerFactory.getLogger(UrlValidator::class.java)

    override fun isValid(value: Any, field: Field): Boolean {
        if (value is URL || value is URI) return true
        if (!isStringValue(value)) return false

        return try {
            URI(value as String)
            true
        } catch (e: Exception) {
            log.warn("url is not valid: $value")
            false
        }
    }

    override fun getValidationAnnotation(): KClass<out Annotation> {
        return Url::class
    }
}