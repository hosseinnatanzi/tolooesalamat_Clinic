package ir.tolooesalamat.app.repository

import ir.tolooesalamat.app.domain.WeeklySchedule
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.DayOfWeek

interface WeeklyScheduleRepository : JpaRepository<WeeklySchedule, Long> {

    fun findAllByDoctorProfileId(doctorProfileId: Long): List<WeeklySchedule>

    fun existsByDoctorProfileIdAndDayOfWeek(
        doctorProfileId: Long?,
        dayOfWeek: DayOfWeek
    ): Boolean

    @Modifying
    @Query("delete from WeeklySchedule s where s.doctorProfile.id = :doctorProfileId")
    fun deleteAllByDoctorProfileId(@Param("doctorProfileId") doctorProfileId: Long)
}