package ir.tolooesalamat.app.exception

/**
 * خطای «دسترسی غیرمجاز».
 * HTTP Status: 403
 *
 * ⚠️ توجه: با org.springframework.security.access.AccessDeniedException
 * اشتباه نشود. این کلاس برای منطق کسب‌وکار است.
 *
 * مثال:
 *  - "شما به این پرونده دسترسی ندارید"
 *  - "فقط پزشک معالج می‌تواند این کار را انجام دهد"
 *  - "شما به سابقه این بیمار دسترسی ندارید"
 */
class AccessDeniedException(
    message: String = "شما به این بخش دسترسی ندارید",
    val resourceType: String? = null,
    val resourceId: Long? = null
) : RuntimeException(message)