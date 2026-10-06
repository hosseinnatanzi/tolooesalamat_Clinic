package ir.tolooesalamat.app.domain

import jakarta.persistence.*

@Entity
@Table(
    name = "specialties",
    indexes = [
        Index(name = "idx_specialty_name", columnList = "name", unique = true),
        Index(name = "idx_specialty_code", columnList = "code", unique = true)
    ]
)
class Specialty(
    @Column(name = "name", nullable = false, unique = true, length = 100)
    var name: String = "",

    @Column(name = "code", unique = true, length = 50)
    var code: String? = null,

    @Column(name = "description", columnDefinition = "TEXT")
    var description: String? = null,

    @Column(name = "active", nullable = false)
    var active: Boolean = true
) : BaseEntity()