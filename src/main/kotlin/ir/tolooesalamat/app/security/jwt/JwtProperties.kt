package ir.tolooesalamat.app.security.jwt
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.jwt")
data class JwtProperties(
    val issuer: String = "clinic-app",
    val audience: String = "clinic-users",
    val expirationHours: Long = 24,
    val refreshExpirationDays: Long = 1,
    val header: String = "Authorization",
    val prefix: String = "Bearer ",
    val clockSkewSeconds: Long = 30
)