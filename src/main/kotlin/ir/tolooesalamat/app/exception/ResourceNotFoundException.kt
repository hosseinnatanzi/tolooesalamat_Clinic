package ir.tolooesalamat.app.exception

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