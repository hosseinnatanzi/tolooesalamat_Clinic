package ir.tolooesalamat.app.mapper

import ir.tolooesalamat.app.domain.AccessLog
import ir.tolooesalamat.app.dto.AccessLogDto
import org.springframework.stereotype.Component

@Component
class AccessLogMapper {

    fun toDto(log: AccessLog): AccessLogDto = AccessLogDto(
        id = log.id ?: 0L,
        userId = log.userId,
        username = log.username,
        resourceType = log.resourceType,
        resourceId = log.resourceId,
        action = log.action,
        ipAddress = log.ipAddress,
        userAgent = log.userAgent,
        accessedAt = log.accessedAt,
        success = log.success,
        details = log.details
    )
}