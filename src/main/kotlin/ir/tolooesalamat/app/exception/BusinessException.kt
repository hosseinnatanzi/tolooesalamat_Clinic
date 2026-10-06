package ir.tolooesalamat.app.exception

class BusinessException(
    message: String,
    val code: String = "BUSINESS_ERROR"
) : RuntimeException(message)