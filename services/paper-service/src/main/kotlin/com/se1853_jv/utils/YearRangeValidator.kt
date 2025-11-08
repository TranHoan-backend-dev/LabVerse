package com.se1853_jv.utils

import com.se1853_jv.config.annotation.ValidYearRange
import com.se1853_jv.dto.request.SearchPapersRequest
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class YearRangeValidator : ConstraintValidator<ValidYearRange, SearchPapersRequest> {
    override fun isValid(value: SearchPapersRequest?, context: ConstraintValidatorContext?): Boolean {
        if (value == null) return true
        
        val yearFrom = value.yearFrom
        val yearTo = value.yearTo
        
        // If both are null, it's valid
        if (yearFrom == null && yearTo == null) return true
        
        // If only one is provided, it's valid
        if (yearFrom == null || yearTo == null) return true
        
        // If both are provided, yearTo must be >= yearFrom
        return yearTo >= yearFrom
    }
}

