package ir.tolooesalamat.app.exception

/**
 * خطای «توکن نامعتبر».
 * HTTP Status: 401
 *
 * مثال:
 *  - "توکن منقضی شده است"
 *  - "توکن نامعتبر است"
 *  - "Refresh Token منقضی شده است"
 */
class InvalidTokenException(
    message: String = "توکن نامعتبر یا منقضی شده است",
    val reason: TokenErrorReason? = null
) : RuntimeException(message)

enum class TokenErrorReason {
    EXPIRED,
    MALFORMED,
    INVALID_SIGNATURE,
    UNSUPPORTED,
    MISSING
}