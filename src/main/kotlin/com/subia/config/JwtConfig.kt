package com.subia.config

import com.subia.security.JwtService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder

@Configuration
class JwtConfig(private val jwtService: JwtService) {
    @Bean
    fun jwtDecoder(): NimbusJwtDecoder = jwtService.decoder
}