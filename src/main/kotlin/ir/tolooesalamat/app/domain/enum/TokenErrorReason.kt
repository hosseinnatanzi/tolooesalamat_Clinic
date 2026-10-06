package ir.tolooesalamat.app.domain.enums

/**
 * دلیل خطای توکن.
 */
enum class TokenErrorReason {
    EXPIRED,
    MALFORMED,
    INVALID_SIGNATURE,
    UNSUPPORTED,
    MISSING
}