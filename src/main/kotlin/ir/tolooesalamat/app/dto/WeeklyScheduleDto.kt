package ir.tolooesalamat.app.dto

import jakarta.validation.constraints.*
import java.time.DayOfWeek
import java.time.LocalTime

data class WeeklyScheduleDto(
    val id: Long? = null,
    val doctorProfileId: Long? = null,

    @field:NotNull(message = "روز هفته الزامی است")
    val dayOfWeek: DayOfWeek? = null,

    @field:NotNull(message = "ساعت شروع الزامی است")
    val startTime: LocalTime? = null,

    @field:NotNull(message = "ساعت پایان الزامی است")
    val endTime: LocalTime? = null,

    @field:Min(15) @field:Max(180)
    val slotDuration: Int = 45
) {
    @AssertTrue(message = "ساعت پایان باید بعد از ساعت شروع باشد")
    fun isTimeRangeValid(): Boolean {
        if (startTime == null || endTime == null) return true
        return startTime.isBefore(endTime)
    }
}