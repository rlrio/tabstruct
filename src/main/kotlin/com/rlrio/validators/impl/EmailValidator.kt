package com.rlrio.validators.impl

import com.rlrio.annotations.Email
import com.rlrio.validators.Validator
import java.lang.reflect.Field
import kotlin.reflect.KClass

import com.rlrio.common.util.CommonUtil.isStringValue

class EmailValidator : Validator {
    companion object {
        private const val EMAIL_REGEX_PATTERN = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$"
    }

    override fun isValid(value: Any, field: Field): Boolean {
        if (!isStringValue(value)) return false
        return EMAIL_REGEX_PATTERN.toRegex().matches(value as String)
    }

    override fun getValidationAnnotation(): KClass<out Annotation> {
        return Email::class
    }
}