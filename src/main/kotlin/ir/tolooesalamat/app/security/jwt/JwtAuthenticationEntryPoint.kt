package ir.tolooesalamat.app.security.jwt

 import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
 import tools.jackson.databind.ObjectMapper
 import java.time.LocalDateTime

/**
 * پاسخ JSON تمیز برای خطای 401 (Unauthorized).
 */
@Component
class JwtAuthenticationEntryPoint(
    private val objectMapper: ObjectMapper
) : AuthenticationEntryPoint {

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"
        response.status = HttpServletResponse.SC_UNAUTHORIZED

        val body = mapOf(
            "timestamp" to LocalDateTime.now().toString(),
            "status" to 401,
            "error" to "UNAUTHORIZED",
            "message" to "احراز هویت نامعتبر یا منقضی شده است",
            "path" to request.requestURI
        )

        objectMapper.writeValue(response.writer, body)
    }
}