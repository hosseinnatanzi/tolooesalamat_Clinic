package ir.tolooesalamat.app.security.jwt

 import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component
 import tools.jackson.databind.ObjectMapper
 import java.time.LocalDateTime

/**
 * پاسخ JSON تمیز برای خطای 403 (Forbidden).
 */
@Component
class JwtAccessDeniedHandler(
    private val objectMapper: ObjectMapper
) : AccessDeniedHandler {

    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException
    ) {
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"
        response.status = HttpServletResponse.SC_FORBIDDEN

        val body = mapOf(
            "timestamp" to LocalDateTime.now().toString(),
            "status" to 403,
            "error" to "FORBIDDEN",
            "message" to "شما به این بخش دسترسی ندارید",
            "path" to request.requestURI
        )

        objectMapper.writeValue(response.writer, body)
    }
}