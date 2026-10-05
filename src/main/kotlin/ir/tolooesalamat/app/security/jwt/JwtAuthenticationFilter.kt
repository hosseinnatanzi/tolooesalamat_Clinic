package ir.tolooesalamat.app.security.jwt

import ir.tolooesalamat.app.security.CustomUserDetailsService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.lang.NonNull
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * فیلتر JWT — در هر درخواست اجرا می‌شود.
 * توکن را از Header استخراج و اعتبارسنجی می‌کند.
 */
@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val jwtProperties: JwtProperties,
    private val userDetailsService: CustomUserDetailsService
) : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun doFilterInternal(
        @NonNull request: HttpServletRequest,
        @NonNull response: HttpServletResponse,
        @NonNull filterChain: FilterChain
    ) {
        val token = extractToken(request)

        if (token != null && SecurityContextHolder.getContext().authentication == null) {
            // فقط Access Token پذیرفته می‌شود (نه Refresh)
            if (jwtService.isAccessToken(token)) {
                val phone = jwtService.extractPhone(token)

                if (phone != null) {
                    try {
                        val userDetails = userDetailsService.loadUserByUsername(phone)
                        val authToken = UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.authorities
                        )
                        authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                        SecurityContextHolder.getContext().authentication = authToken

                        log.trace("احراز هویت موفق: $phone")

                    } catch (ex: Exception) {
                        log.debug("خطا در احراز هویت JWT: ${ex.message}")
                    }
                }
            }
        }

        filterChain.doFilter(request, response)
    }

    /**
     * استخراج توکن از Header.
     * فرمت: "Authorization: Bearer <token>"
     */
    private fun extractToken(request: HttpServletRequest): String? {
        val header = request.getHeader(jwtProperties.header) ?: return null
        val prefix = jwtProperties.prefix

        return if (header.startsWith(prefix)) {
            header.substring(prefix.length).trim()
        } else null
    }
}