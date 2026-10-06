package ir.tolooesalamat.app.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "access_logs")
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

    @Column(name = "accessed_at", nullable = false, updatable = false)
    var accessedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "success", nullable = false)
    var success: Boolean = true,

    @Column(name = "details", columnDefinition = "TEXT")
    var details: String? = null
)