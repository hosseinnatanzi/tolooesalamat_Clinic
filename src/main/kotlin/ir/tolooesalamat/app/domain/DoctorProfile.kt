package ir.tolooesalamat.app.domain

import jakarta.persistence.*

@Entity
@Table(
    name = "doctor_profiles",
    indexes = [
        Index(name = "idx_doctor_user", columnList = "user_id", unique = true),
        Index(name = "idx_doctor_specialty", columnList = "specialty_id"),
        Index(name = "idx_doctor_medical_code", columnList = "medical_code", unique = true)
    ]
)
class DoctorProfile(
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true,
        foreignKey = ForeignKey(name = "fk_doctor_user"))
    var user: User = User(),

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "specialty_id", nullable = false,
        foreignKey = ForeignKey(name = "fk_doctor_specialty"))
    var specialty: Specialty = Specialty(),

    @Column(name = "medical_code", unique = true, nullable = false, length = 20)
    var medicalCode: String = "",

    @Column(name = "bio", columnDefinition = "TEXT")
    var bio: String? = null,

    @Column(name = "years_of_experience")
    var yearsOfExperience: Int = 0,

    @Column(name = "visit_fee")
    var visitFee: Long = 0,

    @Column(name = "default_session_duration")
    var defaultSessionDuration: Int = 45,

    @Column(name = "max_daily_appointments")
    var maxDailyAppointments: Int = 20,

    @OneToMany(mappedBy = "doctorProfile", cascade = [CascadeType.ALL],
        fetch = FetchType.LAZY, orphanRemoval = true)
    var weeklySchedules: MutableList<WeeklySchedule> = mutableListOf()
) : BaseEntity()