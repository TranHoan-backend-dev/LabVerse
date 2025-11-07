package com.se1853_jv.config.annotation

import com.se1853_jv.utils.MaxCurrentYearValidator
import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY_GETTER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [MaxCurrentYearValidator::class])
annotation class CurrentYear(
    val message: String = "Publication year cannot be in the future",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
