package ir.tolooesalamat.app.repository

import ir.tolooesalamat.app.domain.WeeklySchedule
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.DayOfWeek

@Repository
interface WeeklyScheduleRepository : JpaRepository<WeeklySchedule, Long> {

    /** تمام برنامه‌های یک پزشک */
    fun findAllByDoctorProfileId(doctorProfileId: Long): List<WeeklySchedule>

    /** برنامه یک پزشک در یک روز خاص */
    fun findByDoctorProfileIdAndDayOfWeek(
        doctorProfileId: Long,
        dayOfWeek: DayOfWeek
    ): WeeklySchedule?

    /** برنامه‌های یک پزشک در چند روز */
    fun findAllByDoctorProfileIdAndDayOfWeekIn(
        doctorProfileId: Long,
        days: List<DayOfWeek>
    ): List<WeeklySchedule>

    /** بررسی وجود برنامه برای یک روز */
    fun existsByDoctorProfileIdAndDayOfWeek(
        doctorProfileId: Long,
        dayOfWeek: DayOfWeek
    ): Boolean

    /** برنامه‌های فعال یک پزشک */
    @Query("""
        SELECT ws FROM WeeklySchedule ws
        JOIN ws.doctorProfile dp
        JOIN dp.user u
        WHERE dp.id = :doctorProfileId
        AND u.enabled = true
        ORDER BY ws.dayOfWeek
    """)
    fun findActiveByDoctorProfileId(@Param("doctorProfileId") doctorProfileId: Long): List<WeeklySchedule>

    /** حذف تمام برنامه‌های یک پزشک */
    @Modifying
    @Query("DELETE FROM WeeklySchedule ws WHERE ws.doctorProfile.id = :doctorProfileId")
    fun deleteAllByDoctorProfileId(@Param("doctorProfileId") doctorProfileId: Long)

    /** شمارش برنامه‌های یک پزشک */
    fun countByDoctorProfileId(doctorProfileId: Long): Long
}