package com.rlrio.validators

import com.rlrio.validators.impl.DateRangeValidator
import com.rlrio.validators.impl.EmailValidator
import com.rlrio.validators.impl.NumberRangeValidator
import com.rlrio.validators.impl.PatternValidator
import com.rlrio.validators.impl.PhoneValidator
import com.rlrio.validators.impl.UrlValidator
import java.lang.reflect.Field
import kotlin.reflect.KClass

class ValidationProcessor {
    private val validators = mutableMapOf<KClass<out Annotation>, (Any, Field) -> Boolean>()

    init {
        registerValidator(PatternValidator())
        registerValidator(EmailValidator())
        registerValidator(PhoneValidator())
        registerValidator(DateRangeValidator())
        registerValidator(NumberRangeValidator())
        registerValidator(UrlValidator())
    }

    fun registerValidator(validator: Validator) {
        validators[validator.getValidationAnnotation()] = validator::isValid
    }

    fun registerValidator(annotationClass: KClass<out Annotation>, validator: (Any, Field) -> Boolean) {
        validators[annotationClass] = validator
    }

    fun removeValidator(annotationClass: KClass<out Annotation>) {
        validators.remove(annotationClass)
    }

    fun isValid(value: Any?, field: Field): Boolean {
        if (value == null) return true

        for ((annotationClass, validator) in validators) {
            if (field.isAnnotationPresent(annotationClass.java)) {
                return validator(value, field)
            }
        }
        return true
    }
}