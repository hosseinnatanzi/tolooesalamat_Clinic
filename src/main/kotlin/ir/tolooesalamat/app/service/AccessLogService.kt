package ir.tolooesalamat.app.service

import ir.tolooesalamat.app.domain.AccessLog
import ir.tolooesalamat.app.domain.User
import ir.tolooesalamat.app.repository.AccessLogRepository
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

@Service
class AccessLogService(
    private val accessLogRepository: AccessLogRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun logAccess(
        user: User?,
        resourceType: String,
        resourceId: Long?,              // ← Long? (nullable)
        action: String,
        success: Boolean = true,
        details: String? = null
    ) {
        try {
            val request = currentRequest()
            accessLogRepository.save(
                AccessLog(
                    userId = user?.id,
                    username = user?.username ?: currentUsername(),
                    resourceType = resourceType,
                    resourceId = resourceId,
                    action = action,
                    ipAddress = extractIp(request),
                    userAgent = request?.getHeader("User-Agent")?.take(500),   // ← این فیلد هم مهم است
                    success = success,
                    details = details
                )
            )
        } catch (ex: Exception) {
            log.error("خطا در ثبت AccessLog: ${ex.message}", ex)
        }
    }

    private fun currentUsername(): String =
        SecurityContextHolder.getContext().authentication?.name ?: "ANONYMOUS"

    private fun currentRequest(): HttpServletRequest? =
        (RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes)?.request

    private fun extractIp(request: HttpServletRequest?): String? {
        if (request == null) return null
        return request.getHeader("X-Forwarded-For")?.split(",")?.first()?.trim()
            ?: request.remoteAddr
    }
}