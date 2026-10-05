package ir.tolooesalamat.app.dto

import java.time.LocalDateTime

data class AccessLogDto(
    val id: Long,
    val userId: Long? = null,
    val username: String,
    val resourceType: String,
    val resourceId: Long? = null,
    val action: String,
    val ipAddress: String? = null,
    val userAgent: String? = null,
    val accessedAt: LocalDateTime,
    val success: Boolean,
    val details: String? = null
)

data class AccessLogSearchRequest(
    val username: String? = null,
    val resourceType: String? = null,
    val resourceId: Long? = null,
    val action: String? = null,
    val success: Boolean? = null,
    val from: LocalDateTime? = null,
    val to: LocalDateTime? = null,
    val page: Int = 0,
    val size: Int = 50
)