package ir.tolooesalamat.app.exception

/**
 * خطای «منبع تکراری».
 * HTTP Status: 409 Conflict
 *
 * مثال:
 *  - "این شماره موبایل قبلاً ثبت‌نام کرده است"
 *  - "کاربری با این کد ملی وجود دارد"
 *  - "تخصصی با این کد قبلاً ثبت شده است"
 */
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