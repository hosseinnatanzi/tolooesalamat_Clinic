package ir.tolooesalamat.app.exception

class DuplicateResourceException(
    message: String,
    val field: String? = null,
    val value: Any? = null
) : RuntimeException(message) {
    companion object {
        fun of(resourceType: String, field: String, value: Any): DuplicateResourceException =
            DuplicateResourceException(
                message = "$resourceType با $field '$value' قبلاً ثبت شده است",
                field = field,
                value = value
            )
    }
}