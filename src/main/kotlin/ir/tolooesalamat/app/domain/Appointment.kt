package ir.tolooesalamat.app.domain

import ir.tolooesalamat.app.domain.enum.*   // ← import جدید
 import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Entity
@Table(
    name = "appointments",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_doctor_date_time",
            columnNames = ["doctor_id", "date", "start_time"])
    ],
    indexes = [
        Index(name = "idx_appt_doctor_date", columnList = "doctor_id, date"),
        Index(name = "idx_appt_patient", columnList = "patient_id"),
        Index(name = "idx_appt_status", columnList = "status")
    ]
)
class Appointment(

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false,
        foreignKey = ForeignKey(name = "fk_appointment_doctor"))
    var doctor: User = User(),

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false,
        foreignKey = ForeignKey(name = "fk_appointment_patient"))
    var patient: User = User(),

    @Column(name = "date", nullable = false)
    var date: LocalDate = LocalDate.now(),

    @Column(name = "start_time", nullable = false)
    var startTime: LocalTime = LocalTime.of(9, 0),

    @Column(name = "duration", nullable = false)
    var duration: Int = 45,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    var status: AppointmentStatus = AppointmentStatus.PENDING,

    @Enumerated(EnumType.STRING)
    @Column(name = "session_type", nullable = false, length = 20)
    var sessionType: SessionType = SessionType.FOLLOW_UP,

    @Column(name = "queue_number")
    var queueNumber: Int? = null,

    @Convert(converter = ir.tolooesalamat.app.crypto.converter.EncryptedStringConverter::class)
    @Column(name = "notes_enc", columnDefinition = "TEXT")
    var notes: String? = null,

    @Column(name = "checked_in_at")
    var checkedInAt: LocalDateTime? = null,

    @Column(name = "started_at")
    var startedAt: LocalDateTime? = null,

    @Column(name = "completed_at")
    var completedAt: LocalDateTime? = null

) : BaseEntity() {

    @get:Transient
    val endTime: LocalTime
        get() = startTime.plusMinutes(duration.toLong())
}