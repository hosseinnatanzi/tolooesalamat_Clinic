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

/**
 * ثبت دسترسی به داده‌های حساس.
 * در یک Transaction جداگانه اجرا می‌شود تا اگر عملیات اصلی Rollback شد، لاگ حفظ شود.
 */
@Service
class AccessLogService(
    private val accessLogRepository: AccessLogRepository
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun logAccess(
        user: User?,
        resourceType: String,
        resourceId: Long?,
        action: String,
        success: Boolean = true,
        details: String? = null
    ) {
        try {
            val request = currentRequest()

            val accessLog = AccessLog(
                userId = user?.id,
                username = user?.username ?: currentUsername(),
                resourceType = resourceType,
                resourceId = resourceId,
                action = action,
                ipAddress = extractIp(request),
                userAgent = request?.getHeader("User-Agent")?.take(500),
                success = success,
                details = details
            )

            accessLogRepository.save(accessLog)

            if (!success) {
                log.warn("🚨 دسترسی ناموفق: user=${accessLog.username}, " +
                        "resource=$resourceType:$resourceId, action=$action")
            }

        } catch (ex: Exception) {
            // شکست در ثبت لاگ، عملیات اصلی را متوقف نمی‌کند
            log.error("خطا در ثبت AccessLog: ${ex.message}", ex)
        }
    }

    private fun currentUsername(): String =
        SecurityContextHolder.getContext().authentication?.name ?: "ANONYMOUS"

    private fun currentRequest(): HttpServletRequest? =
        (RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes)?.request

    private fun extractIp(request: HttpServletRequest?): String? {
        if (request == null) return null

        val headers = listOf(
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP"
        )

        for (header in headers) {
            val ip = request.getHeader(header)
            if (!ip.isNullOrBlank() && ip != "unknown") {
                return ip.split(",").first().trim()
            }
        }

        return request.remoteAddr
    }
}