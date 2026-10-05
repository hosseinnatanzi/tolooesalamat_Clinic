package ir.tolooesalamat.app.domain

import jakarta.persistence.*
import java.time.DayOfWeek
import java.time.LocalTime

/**
 * برنامه هفتگی پزشک.
 *
 * هر پزشک برای هر روز هفته می‌تواند یک برنامه داشته باشد.
 * مثال:
 *   شنبه: 09:00 - 13:00
 *   یکشنبه: 15:00 - 19:00
 */
@Entity
@Table(
    name = "weekly_schedules",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_doctor_day",
            columnNames = ["doctor_profile_id", "day_of_week"]
        )
    ],
    indexes = [
        Index(
            name = "idx_schedule_doctor_day",
            columnList = "doctor_profile_id, day_of_week"
        )
    ]
)
class WeeklySchedule(

    // ═══════════════════════════════════════════
    // 🔗 ارتباط با DoctorProfile (کلید خارجی)
    // ═══════════════════════════════════════════

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "doctor_profile_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_schedule_doctor_profile")
    )
    var doctorProfile: DoctorProfile = DoctorProfile(),

    // ═══════════════════════════════════════════
    // 🕐 زمان‌بندی
    // ═══════════════════════════════════════════

    /**
     * روز هفته.
     * مقادیر: SATURDAY, SUNDAY, MONDAY, ..., FRIDAY
     */
    @Enumerated(EnumType.STRING)
    @Column(
        name = "day_of_week",
        nullable = false,
        length = 15
    )
    var dayOfWeek: DayOfWeek = DayOfWeek.SATURDAY,

    /** ساعت شروع کار */
    @Column(
        name = "start_time",
        nullable = false
    )
    var startTime: LocalTime = LocalTime.of(9, 0),

    /** ساعت پایان کار */
    @Column(
        name = "end_time",
        nullable = false
    )
    var endTime: LocalTime = LocalTime.of(17, 0),

    /**
     * مدت هر اسلات نوبت (دقیقه).
     * مثال: 45 دقیقه‌ای برای روان‌پزشکی
     */
    @Column(
        name = "slot_duration",
        nullable = false
    )
    var slotDuration: Int = 45

) : BaseEntity() {

    /** بررسی صحت بازه زمانی */
    @get:Transient
    val isValidTimeRange: Boolean
        get() = startTime.isBefore(endTime)

    /** تعداد اسلات‌های ممکن در این روز */
    @get:Transient
    val totalSlots: Int
        get() {
            if (!isValidTimeRange || slotDuration <= 0) return 0
            val minutes = java.time.Duration.between(startTime, endTime).toMinutes()
            return (minutes / slotDuration).toInt()
        }

    override fun toString(): String =
        "WeeklySchedule(day=$dayOfWeek, $startTime-$endTime, slot=${slotDuration}min)"
}