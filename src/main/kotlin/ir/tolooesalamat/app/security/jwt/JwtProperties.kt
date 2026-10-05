package ir.tolooesalamat.app.security.jwt

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

/**
 * تنظیمات JWT.
 * مقادیر از application.yml خوانده می‌شوند.
 */
@Component
@ConfigurationProperties(prefix = "app.jwt")
data class JwtProperties(
    var issuer: String = "clinic-app",
    var audience: String = "clinic-users",
    var expirationHours: Long = 24,
    var refreshExpirationDays: Long = 1,
    var header: String = "Authorization",
    var prefix: String = "Bearer ",
    var clockSkewSeconds: Long = 30
)