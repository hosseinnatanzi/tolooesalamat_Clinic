package ir.tolooesalamat.app.exception

/**
 * خطای منطق کسب‌وکار.
 * برای موقعیت‌هایی که درخواست معتبر است اما منطق کسب‌وکار اجازه نمی‌دهد.
 *
 * مثال:
 *  - "این شماره موبایل قبلاً ثبت‌نام کرده است"
 *  - "این زمان قبلاً رزرو شده است"
 *  - "این نوبت قابل لغو نیست"
 */
class BusinessException(
    message: String,
    val code: String = "BUSINESS_ERROR"
) : RuntimeException(message)