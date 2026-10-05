package ir.tolooesalamat.app.domain

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import org.hibernate.annotations.SQLRestriction


@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
@SQLRestriction("is_deleted = false")
abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id :Long? =null

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null
@CreatedBy
@Column(name = "created_by",updatable=false, length=50)
var createdBy:String? = null
    @LastModifiedBy
    @Column(name = "last_modifiedBy", length = 50)
    var updatedBy:String?=null

    @LastModifiedBy
    @Column(name="updated_at")
    var updatedAt: LocalDateTime?=null
    @Column(name = "is_deleted")
    var isDeleted: Boolean = false

}