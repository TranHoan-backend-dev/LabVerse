package com.se1853_jv.utils

import com.se1853_jv.config.annotation.CurrentYear
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import java.time.LocalDate

class MaxCurrentYearValidator : ConstraintValidator<CurrentYear, Int> {
    override fun isValid(value: Int?, context: ConstraintValidatorContext): Boolean {
        if (value == null) return true
        return value <= LocalDate.now().year
    }
}