package ir.tolooesalamat.app.security.jwt

import ir.tolooesalamat.app.domain.User
import ir.tolooesalamat.app.crypto.core.RsaKeyManager
import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.UUID

@Service
class JwtService(
    private val jwtProperties: JwtProperties,
    private val rsaKeyManager: RsaKeyManager
) {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        const val CLAIM_ROLE = "role"
        const val CLAIM_USER_ID = "uid"
        const val CLAIM_PHONE = "phone"
        const val CLAIM_FULL_NAME = "name"
        const val CLAIM_TOKEN_TYPE = "type"
        const val TYPE_ACCESS = "access"
        const val TYPE_REFRESH = "refresh"
    }

    // ═══════════════════════════════════════════
    // 🎯 تولید توکن
    // ═══════════════════════════════════════════

    fun generateAccessToken(user: User): String {
        val now = Instant.now()
        val expiresAt = now.plus(jwtProperties.expirationHours, ChronoUnit.HOURS)

        return Jwts.builder()
            .id(UUID.randomUUID().toString())
            .subject(user.phone)
            .issuer(jwtProperties.issuer)
            .audience().add(jwtProperties.audience).and()
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiresAt))
            .claim(CLAIM_USER_ID, user.id)
            .claim(CLAIM_ROLE, user.role.name)
            .claim(CLAIM_PHONE, user.phone)
            .claim(CLAIM_FULL_NAME, user.fullName)
            .claim(CLAIM_TOKEN_TYPE, TYPE_ACCESS)
            .signWith(rsaKeyManager.getPrivateKey(), Jwts.SIG.RS256)
            .compact()
    }

    fun generateRefreshToken(user: User): String {
        val now = Instant.now()
        val expiresAt = now.plus(jwtProperties.refreshExpirationDays, ChronoUnit.DAYS)

        return Jwts.builder()
            .id(UUID.randomUUID().toString())
            .subject(user.phone)
            .issuer(jwtProperties.issuer)
            .audience().add(jwtProperties.audience).and()
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiresAt))
            .claim(CLAIM_USER_ID, user.id)
            .claim(CLAIM_TOKEN_TYPE, TYPE_REFRESH)
            .signWith(rsaKeyManager.getPrivateKey(), Jwts.SIG.RS256)
            .compact()
    }

    // ═══════════════════════════════════════════
    // 🔍 اعتبارسنجی
    // ═══════════════════════════════════════════

    fun validateAndParse(token: String): Claims? = try {
        Jwts.parser()
            .verifyWith(rsaKeyManager.getPublicKey())
            .requireIssuer(jwtProperties.issuer)          // still valid in 0.12.x
            .clockSkewSeconds(jwtProperties.clockSkewSeconds)
            .build()
            .parseSignedClaims(token)
            .payload
    } catch (ex: JwtException) {
        log.debug("JWT نامعتبر: ${ex.message}")
        null
    } catch (ex: IllegalArgumentException) {
        log.debug("JWT خالی: ${ex.message}")
        null
    }

    // ═══════════════════════════════════════════
    // 📤 استخراج داده‌ها
    // ═══════════════════════════════════════════

    fun extractPhone(token: String): String? = validateAndParse(token)?.subject

    fun extractUserId(token: String): Long? =
        validateAndParse(token)?.get(CLAIM_USER_ID, Number::class.java)?.toLong()

    fun extractRole(token: String): String? =
        validateAndParse(token)?.get(CLAIM_ROLE, String::class.java)

    fun extractFullName(token: String): String? =
        validateAndParse(token)?.get(CLAIM_FULL_NAME, String::class.java)

    fun isAccessToken(token: String): Boolean =
        validateAndParse(token)?.get(CLAIM_TOKEN_TYPE, String::class.java) == TYPE_ACCESS

    fun isRefreshToken(token: String): Boolean =
        validateAndParse(token)?.get(CLAIM_TOKEN_TYPE, String::class.java) == TYPE_REFRESH

    fun isTokenExpired(token: String): Boolean =
        validateAndParse(token)?.expiration?.before(Date()) ?: true

    fun getExpirationSeconds(): Long = jwtProperties.expirationHours * 3600
}