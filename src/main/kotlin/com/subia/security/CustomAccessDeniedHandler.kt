package com.subia.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.subia.dto.api.ApiError
import com.subia.dto.api.ApiResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component

@Component
class CustomAccessDeniedHandler(private val objectMapper: ObjectMapper) : AccessDeniedHandler {
    override fun handle(request: HttpServletRequest, response: HttpServletResponse, ex: AccessDeniedException) {
        response.status = HttpStatus.FORBIDDEN.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        objectMapper.writeValue(response.writer,
            ApiResponse<Nothing>(error = ApiError("FORBIDDEN", "No tienes permisos para acceder a este recurso.")))
    }
}
