package com.subia.security

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

@Component
class RateLimitFilter(
    @Value("\${rate-limit.requests-per-minute:100}") private val requestsPerMinute: Long
) : OncePerRequestFilter() {

    private val buckets = ConcurrentHashMap<String, Bucket>()

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val key = request.getHeader("X-Forwarded-For")?.split(",")?.first()?.trim()
            ?: request.remoteAddr
        val bucket = buckets.computeIfAbsent(key) {
            Bucket.builder()
                .addLimit(
                    Bandwidth.builder()
                        .capacity(requestsPerMinute)
                        .refillGreedy(requestsPerMinute, Duration.ofMinutes(1))
                        .build()
                )
                .build()
        }
        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response)
        } else {
            response.status = HttpStatus.TOO_MANY_REQUESTS.value()
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            response.addHeader("Retry-After", "60")
            response.writer.write("""{"data":null,"error":{"code":"RATE_LIMIT_EXCEEDED","message":"Demasiadas peticiones. Reintenta en 60 segundos.","details":[]}}""")
        }
    }
}
