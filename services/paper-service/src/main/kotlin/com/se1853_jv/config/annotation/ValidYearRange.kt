package com.se1853_jv.config.annotation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [YearRangeValidator::class])
annotation class ValidYearRange(
    val message: String = "yearTo must be greater than or equal to yearFrom",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

