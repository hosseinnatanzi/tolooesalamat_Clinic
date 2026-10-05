package ir.tolooesalamat.app.domain

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * وضعیت نوبت.
 */
enum class AppointmentStatus(val label: String) {
    PENDING("در انتظار تأیید"),
    CONFIRMED("تأیید شده"),
    CHECKED_IN("پذیرش شده"),
    IN_PROGRESS("در حال ویزیت"),
    COMPLETED("انجام شده"),
    CANCELLED("لغو شده"),
    NO_SHOW("عدم مراجعه");

    companion object {
        fun fromString(value: String?): AppointmentStatus? =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
    }
}

/**
 * نوع جلسه.
 */
enum class SessionType(val label: String) {
    INITIAL("اولیه"),
    FOLLOW_UP("پیگیری"),
    EMERGENCY("اورژانسی");

    companion object {
        fun fromString(value: String?): SessionType? =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
    }
}

/**
 * نوبت پزشکی.
 *
 * پزشک + بیمار + تاریخ + ساعت شروع = یکتا
 */
@Entity
@Table(
    name = "appointments",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_doctor_date_time",
            columnNames = ["doctor_id", "date", "start_time"]
        )
    ],
    indexes = [
        Index(name = "idx_appt_doctor_date", columnList = "doctor_id, date"),
        Index(name = "idx_appt_patient", columnList = "patient_id"),
        Index(name = "idx_appt_status", columnList = "status"),
        Index(name = "idx_appt_date", columnList = "date")
    ]
)
class Appointment(

    // ═══════════════════════════════════════════
    // 🔗 ارتباطات
    // ═══════════════════════════════════════════

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "doctor_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_appointment_doctor")
    )
    var doctor: User = User(),

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "patient_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_appointment_patient")
    )
    var patient: User = User(),

    // ═══════════════════════════════════════════
    // 🕐 زمان‌بندی
    // ═══════════════════════════════════════════

    @Column(name = "date", nullable = false)
    var date: LocalDate = LocalDate.now(),

    @Column(name = "start_time", nullable = false)
    var startTime: LocalTime = LocalTime.of(9, 0),

    /** مدت جلسه (دقیقه) */
    @Column(name = "duration", nullable = false)
    var duration: Int = 45,

    // ═══════════════════════════════════════════
    // 📋 وضعیت و نوع
    // ═══════════════════════════════════════════

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    var status: AppointmentStatus = AppointmentStatus.PENDING,

    @Enumerated(EnumType.STRING)
    @Column(name = "session_type", nullable = false, length = 20)
    var sessionType: SessionType = SessionType.FOLLOW_UP,

    /**
     * شماره نوبت در صف آن روز.
     * (فقط برای نوبت‌های همان روز محاسبه می‌شود)
     */
    @Column(name = "queue_number")
    var queueNumber: Int? = null,

    @Column(name = "notes", columnDefinition = "TEXT")
    var notes: String? = null,

    // ═══════════════════════════════════════════
    // ⏱️ زمان‌های رویداد
    // ═══════════════════════════════════════════

    /** زمان پذیرش بیمار (check-in) */
    @Column(name = "checked_in_at")
    var checkedInAt: LocalDateTime? = null,

    /** زمان شروع ویزیت */
    @Column(name = "started_at")
    var startedAt: LocalDateTime? = null,

    /** زمان پایان ویزیت */
    @Column(name = "completed_at")
    var completedAt: LocalDateTime? = null

) : BaseEntity() {

    /** زمان پایان محاسبه‌شده */
    @get:Transient
    val endTime: LocalTime
        get() = startTime.plusMinutes(duration.toLong())

    /** بررسی فعال بودن نوبت */
    @get:Transient
    val isActive: Boolean
        get() = status !in listOf(AppointmentStatus.CANCELLED, AppointmentStatus.COMPLETED, AppointmentStatus.NO_SHOW)

    /** بررسی گذشته بودن */
    @get:Transient
    val isPast: Boolean
        get() = date.isBefore(LocalDate.now()) ||
                (date == LocalDate.now() && endTime.isBefore(LocalTime.now()))

    override fun toString(): String =
        "Appointment(id=$id, doctorId=${doctor.id}, patientId=${patient.id}, " +
                "date=$date, time=$startTime-$endTime, status=$status)"
}