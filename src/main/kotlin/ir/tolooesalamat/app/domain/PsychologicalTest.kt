package ir.tolooesalamat.app.domain

import ir.tolooesalamat.app.domain.enum.TestCategory   // ← import جدید
import jakarta.persistence.*

@Entity
@Table(
    name = "psychological_tests",
    indexes = [
        Index(name = "idx_test_name", columnList = "name", unique = true),
        Index(name = "idx_test_code", columnList = "code", unique = true),
        Index(name = "idx_test_active", columnList = "active")
    ]
)
class PsychologicalTest(

    @Column(name = "name", nullable = false, unique = true, length = 100)
    var name: String = "",

    @Column(name = "code", unique = true, length = 50)
    var code: String? = null,

    @Column(name = "description", columnDefinition = "TEXT")
    var description: String? = null,

    @Column(name = "total_questions")
    var totalQuestions: Int = 0,

    @Column(name = "estimated_minutes")
    var estimatedMinutes: Int = 0,

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 30)
    var category: TestCategory? = null,

    @Column(name = "active", nullable = false)
    var active: Boolean = true

) : BaseEntity() {

    override fun toString(): String =
        "PsychologicalTest(id=$id, name='$name', code='$code')"
}