package ir.tolooesalamat.app.domain

import jakarta.persistence.*
import java.time.DayOfWeek
import java.time.LocalTime

@Entity
@Table(
    name = "weekly_schedules",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_doctor_day",
            columnNames = ["doctor_profile_id", "day_of_week"])
    ]
)
class WeeklySchedule(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_profile_id", nullable = false,
        foreignKey = ForeignKey(name = "fk_schedule_doctor"))
    var doctorProfile: DoctorProfile = DoctorProfile(),

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 15)
    var dayOfWeek: DayOfWeek = DayOfWeek.SATURDAY,

    @Column(name = "start_time", nullable = false)
    var startTime: LocalTime = LocalTime.of(9, 0),

    @Column(name = "end_time", nullable = false)
    var endTime: LocalTime = LocalTime.of(17, 0),

    @Column(name = "slot_duration", nullable = false)
    var slotDuration: Int = 45
) : BaseEntity()