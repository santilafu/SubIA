package com.subia.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.subia.dto.api.ApiError
import com.subia.dto.api.ApiResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

@Component
class CustomAuthEntryPoint(private val objectMapper: ObjectMapper) : AuthenticationEntryPoint {
    override fun commence(request: HttpServletRequest, response: HttpServletResponse, ex: AuthenticationException) {
        response.status = HttpStatus.UNAUTHORIZED.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        objectMapper.writeValue(response.writer,
            ApiResponse<Nothing>(error = ApiError("UNAUTHORIZED", "Autenticación requerida. Proporciona un token Bearer válido.")))
    }
}
