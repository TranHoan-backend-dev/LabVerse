package com.se1853_jv.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CreateHighlightRequest(
    @field:NotBlank(message = "Paper ID is required")
    val paperId: String,

    @field:NotBlank(message = "Collection ID is required")
    val collectionId: String,

    @field:NotBlank(message = "Color is required")
    val color: String,

    @field:NotNull(message = "X coordinate is required")
    val coordinationX: Int,

    @field:NotNull(message = "Y coordinate is required")
    val coordinationY: Int,

    @field:NotNull(message = "Page number is required")
    val pageNumber: Int
)







