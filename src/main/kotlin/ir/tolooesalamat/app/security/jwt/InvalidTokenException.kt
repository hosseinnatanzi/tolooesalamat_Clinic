package ir.tolooesalamat.app.security.jwt

class JwtInvalidTokenException(
    message: String = "توکن JWT نامعتبر است"
) : RuntimeException(message)