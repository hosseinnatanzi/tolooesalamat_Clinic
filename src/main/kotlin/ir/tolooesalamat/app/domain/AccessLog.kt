package ir.tolooesalamat.app.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "access_logs",
    indexes = [
        Index(name = "idx_log_user", columnList = "username"),
        Index(name = "idx_log_time", columnList = "accessed_at"),
        Index(name = "idx_log_resource", columnList = "resource_type, resource_id")
    ]
)
class AccessLog(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "user_id")
    var userId: Long? = null,

    @Column(name = "username", nullable = false, length = 50)
    var username: String = "",

    @Column(name = "resource_type", nullable = false, length = 50)
    var resourceType: String = "",

    @Column(name = "resource_id")
    var resourceId: Long? = null,

    @Column(name = "action", nullable = false, length = 20)
    var action: String = "",

    @Column(name = "ip_address", length = 45)
    var ipAddress: String? = null,

    @Column(name = "user_agent", length = 500)
    var userAgent: String? = null,

    @Column(name = "accessed_at", nullable = false, updatable = false)
    var accessedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "success", nullable = false)
    var success: Boolean = true,

    @Column(name = "details", columnDefinition = "TEXT")
    var details: String? = null
) {
    override fun toString(): String =
        "AccessLog(user='$username', action='$action')"
}