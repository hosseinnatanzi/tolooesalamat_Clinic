package ir.tolooesalamat.app.domain

import jakarta.persistence.*
import java.time.LocalDate


@Entity
@Table(name = "doctor_time_offs")
class DoctorTimeOff(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_profile_id", nullable = false)
    var doctorProfile: DoctorProfile = DoctorProfile(),

    @Column(name = "from_date", nullable = false)
    var fromDate: LocalDate = LocalDate.now(),

    @Column(name = "to_date", nullable = false)
    var toDate: LocalDate = LocalDate.now(),

    @Column(name = "reason", length = 200)
    var reason: String? = null
) : BaseEntity()