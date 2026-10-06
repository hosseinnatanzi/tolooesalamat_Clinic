package ir.tolooesalamat.app.domain

import jakarta.persistence.*

@Entity
@Table(name = "psychological_tests")
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

    @Column(name = "active", nullable = false)
    var active: Boolean = true
) : BaseEntity()