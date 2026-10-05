package ir.tolooesalamat.app.exception

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDateTime

/**
 * پاسخ استاندارد خطا.
 *
 * @JsonInclude(NON_NULL) → فیلدهای null در JSON حذف می‌شوند
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ErrorResponse(
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val status: Int,
    val error: String,
    val code: String? = null,
    val message: String,
    val path: String? = null,
    val validationErrors: Map<String, String>? = null,
    val details: String? = null
) {
    companion object {
        fun of(
            status: Int,
            error: String,
            message: String,
            path: String? = null,
            code: String? = null
        ) = ErrorResponse(
            status = status,
            error = error,
            code = code,
            message = message,
            path = path
        )
    }
}