package ir.tolooesalamat.app.exception

/**
 * خطای «منبع یافت نشد».
 * HTTP Status: 404
 *
 * مثال:
 *  - "کاربر با شناسه 5 یافت نشد"
 *  - "نوبت با شناسه 10 یافت نشد"
 *  - "پرونده با شماره P-2024-00001 یافت نشد"
 */
class ResourceNotFoundException(
    message: String,
    val resourceType: String? = null,
    val resourceId: Any? = null
) : RuntimeException(message) {

    companion object {
        fun of(resourceType: String, id: Any): ResourceNotFoundException =
            ResourceNotFoundException(
                message = "$resourceType با شناسه $id یافت نشد",
                resourceType = resourceType,
                resourceId = id
            )

        fun of(resourceType: String, field: String, value: Any): ResourceNotFoundException =
            ResourceNotFoundException(
                message = "$resourceType با $field '$value' یافت نشد",
                resourceType = resourceType,
                resourceId = value
            )
    }
}