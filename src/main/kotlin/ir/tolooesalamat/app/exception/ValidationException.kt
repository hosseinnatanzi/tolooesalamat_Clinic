package ir.tolooesalamat.app.exception

/**
 * خطای «اعتبارسنجی».
 * HTTP Status: 400
 *
 * معمولاً برای اعتبارسنجی‌های منطقی که به `@Valid` نمی‌سپاریم.
 */
class ValidationException(
    message: String,
    val errors: Map<String, String>? = null
) : RuntimeException(message)