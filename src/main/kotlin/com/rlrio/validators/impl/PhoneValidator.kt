package com.rlrio.validators.impl

import com.rlrio.annotations.Phone
import com.rlrio.validators.Validator
import java.lang.reflect.Field
import kotlin.reflect.KClass

import com.rlrio.common.util.CommonUtil.isStringValue

class PhoneValidator : Validator {
    companion object {
        private const val PHONE_REGEX_PATTERNS =
            ("^(\\+\\d{1,3}( )?)?((\\(\\d{3}\\))|\\d{3})[- .]?\\d{3}[- .]?\\d{4}$"
                    + "|^(\\+\\d{1,3}( )?)?(\\d{3}[ ]?){2}\\d{3}$"
                    + "|^(\\+\\d{1,3}( )?)?(\\d{3}[ ]?)(\\d{2}[ ]?){2}\\d{2}$")
    }

    override fun isValid(value: Any, field: Field): Boolean {
        if (!isStringValue(value)) return false
        return PHONE_REGEX_PATTERNS.toRegex().matches(value as String)
    }

    override fun getValidationAnnotation(): KClass<out Annotation> {
        return Phone::class
    }
}