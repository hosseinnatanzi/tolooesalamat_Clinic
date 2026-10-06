package ir.tolooesalamat.app.exception

class AccessDeniedException(
    message: String = "شما به این بخش دسترسی ندارید",
    val resourceType: String? = null,
    val resourceId: Long? = null
) : RuntimeException(message)