package ir.tolooesalamat.app.domain

import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
@SQLRestriction("is_deleted = false")
abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null

    @LastModifiedDate
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null

    @CreatedBy
    @Column(name = "created_by", updatable = false, length = 50)
    var createdBy: String? = null

    @LastModifiedBy
    @Column(name = "updated_by", length = 50)
    var updatedBy: String? = null

    @Column(name = "is_deleted", nullable = false)
    var isDeleted: Boolean = false
}