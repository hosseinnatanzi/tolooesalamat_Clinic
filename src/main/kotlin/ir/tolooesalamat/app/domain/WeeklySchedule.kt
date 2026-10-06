package ir.tolooesalamat.app.domain

import jakarta.persistence.*
import java.time.DayOfWeek
import java.time.LocalTime

@Entity
@Table(
    name = "weekly_schedules",
    uniqueConstraints = [UniqueConstraint(columnNames = ["doctor_profile_id", "day_of_week"])]
)
class WeeklySchedule(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_profile_id", nullable = false)
    var doctorProfile: DoctorProfile,

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    var dayOfWeek: DayOfWeek,

    @Column(name = "start_time", nullable = false)
    var startTime: LocalTime,

    @Column(name = "end_time", nullable = false)
    var endTime: LocalTime,

    @Column(name = "slot_duration", nullable = false)
    var slotDuration: Int = 45
)