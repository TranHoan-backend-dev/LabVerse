package com.se1853_jv.exception

import com.se1853_jv.dto.response.WrapperApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import java.time.LocalDateTime

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<WrapperApiResponse> {
        val body = buildResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            message = ex.message!!,
        )
        return ResponseEntity.badRequest().body(body)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<WrapperApiResponse> {
        val fieldError = ex.bindingResult.fieldErrors.firstOrNull()
        val message = fieldError?.defaultMessage ?: "Validation failed"
        val body = buildResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            message = message,
        )
        return ResponseEntity.badRequest().body(body)
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(ex: MethodArgumentTypeMismatchException): ResponseEntity<WrapperApiResponse> {
        val body = buildResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            message = "Invalid value for parameter '${ex.name}'",
        )
        return ResponseEntity.badRequest().body(body)
    }

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(ex: NoSuchElementException): ResponseEntity<WrapperApiResponse> {
        val body = buildResponse(
            status = HttpStatus.NOT_FOUND.value(),
            message = ex.message ?: "Resource not found",
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body)
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneral(ex: Exception): ResponseEntity<WrapperApiResponse> {
        val body = buildResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            message = ex.message ?: "Unexpected error occurred",
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body)
    }

    private fun buildResponse(status: Int, message: String) = WrapperApiResponse(
        status = status,
        message = message,
        data = null,
        timestamp = LocalDateTime.now(),
    )
}
