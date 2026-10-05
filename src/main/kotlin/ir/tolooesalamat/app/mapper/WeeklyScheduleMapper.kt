package ir.tolooesalamat.app.mapper

import ir.tolooesalamat.app.domain.WeeklySchedule
import ir.tolooesalamat.app.dto.WeeklyScheduleDto
import org.springframework.stereotype.Component

@Component
class WeeklyScheduleMapper {

    fun toDto(schedule: WeeklySchedule): WeeklyScheduleDto = WeeklyScheduleDto(
        id = schedule.id,
        doctorProfileId = schedule.doctorProfile.id,
        dayOfWeek = schedule.dayOfWeek,
        startTime = schedule.startTime,
        endTime = schedule.endTime,
        slotDuration = schedule.slotDuration
    )

    fun toEntity(
        dto: WeeklyScheduleDto,
        doctorProfile: ir.tolooesalamat.app.domain.DoctorProfile
    ): WeeklySchedule = WeeklySchedule(
        doctorProfile = doctorProfile,
        dayOfWeek = dto.dayOfWeek!!,
        startTime = dto.startTime!!,
        endTime = dto.endTime!!,
        slotDuration = dto.slotDuration
    )

    fun updateEntity(entity: WeeklySchedule, dto: WeeklyScheduleDto) {
        dto.dayOfWeek?.let { entity.dayOfWeek = it }
        dto.startTime?.let { entity.startTime = it }
        dto.endTime?.let { entity.endTime = it }
        entity.slotDuration = dto.slotDuration
    }
}