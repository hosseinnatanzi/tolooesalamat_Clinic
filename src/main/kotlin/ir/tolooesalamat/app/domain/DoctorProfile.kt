package ir.tolooesalamat.app.domain

import jakarta.persistence.*

/**
 * پروفایل پزشک.
 *
 * یک User با نقش DOCTOR، دقیقاً یک DoctorProfile دارد.
 */
@Entity
@Table(
    name = "doctor_profiles",
    indexes = [
        Index(name = "idx_doctor_specialty", columnList = "specialty_id"),
        Index(name = "idx_doctor_medical_code", columnList = "medical_code", unique = true),
        Index(name = "idx_doctor_user", columnList = "user_id", unique = true)
    ]
)
class DoctorProfile(

    // ═══════════════════════════════════════════
    // 🔗 ارتباط با User (کلید خارجی یکتا)
    // ═══════════════════════════════════════════

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        unique = true,
        foreignKey = ForeignKey(name = "fk_doctor_profile_user")
    )
    var user: User = User(),

    // ═══════════════════════════════════════════
    // 🎯 تخصص (کلید خارجی)
    // ═══════════════════════════════════════════

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "specialty_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_doctor_profile_specialty")
    )
    var specialty: Specialty = Specialty(),

    // ═══════════════════════════════════════════
    // 📋 اطلاعات حرفه‌ای
    // ═══════════════════════════════════════════

    /**
     * شماره نظام پزشکی — یکتا و اجباری.
     * فرمت معمول: ۶ تا ۸ رقم
     */
    @Column(
        name = "medical_code",
        unique = true,
        nullable = false,
        length = 20
    )
    var medicalCode: String = "",

    @Column(
        name = "bio",
        columnDefinition = "TEXT"
    )
    var bio: String? = null,

    /** سابقه کار (سال) */
    @Column(
        name = "years_of_experience",
        nullable = false
    )
    var yearsOfExperience: Int = 0,

    /** هزینه ویزیت (ریال) */
    @Column(
        name = "visit_fee",
        nullable = false
    )
    var visitFee: Long = 0,

    /** مدت پیش‌فرض هر جلسه (دقیقه) */
    @Column(
        name = "default_session_duration",
        nullable = false
    )
    var defaultSessionDuration: Int = 45,

    /** حداکثر نوبت روزانه */
    @Column(
        name = "max_daily_appointments",
        nullable = false
    )
    var maxDailyAppointments: Int = 20,

    // ═══════════════════════════════════════════
    // 🔗 ارتباطات
    // ═══════════════════════════════════════════

    /**
     * برنامه‌های هفتگی پزشک.
     * حذف DoctorProfile → حذف تمام برنامه‌ها (orphanRemoval)
     */
    @OneToMany(
        mappedBy = "doctorProfile",
        cascade = [CascadeType.ALL],
        fetch = FetchType.LAZY,
        orphanRemoval = true
    )
    var weeklySchedules: MutableList<WeeklySchedule> = mutableListOf()

) : BaseEntity() {

    override fun toString(): String =
        "DoctorProfile(id=$id, userId=${user.id}, medicalCode='$medicalCode')"
}