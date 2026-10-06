package ir.tolooesalamat.app.mapper

import ir.tolooesalamat.app.domain.DoctorProfile
import ir.tolooesalamat.app.domain.WeeklySchedule
import ir.tolooesalamat.app.dto.WeeklyScheduleDto
import org.springframework.stereotype.Component

@Component
class WeeklyScheduleMapper {

    fun toDto(entity: WeeklySchedule): WeeklyScheduleDto =
        WeeklyScheduleDto(
            id = entity.id,
            doctorProfileId = entity.doctorProfile.id,
            dayOfWeek = entity.dayOfWeek,
            startTime = entity.startTime,
            endTime = entity.endTime,
            slotDuration = entity.slotDuration
        )

    fun toEntity(dto: WeeklyScheduleDto, profile: DoctorProfile): WeeklySchedule =
        WeeklySchedule(
            doctorProfile = profile,
            dayOfWeek = requireNotNull(dto.dayOfWeek) { "dayOfWeek is required" },
            startTime = requireNotNull(dto.startTime) { "startTime is required" },
            endTime = requireNotNull(dto.endTime) { "endTime is required" },
            slotDuration = dto.slotDuration
        )

    fun updateEntity(entity: WeeklySchedule, dto: WeeklyScheduleDto) {
        dto.dayOfWeek?.let { entity.dayOfWeek = it }
        dto.startTime?.let { entity.startTime = it }
        dto.endTime?.let { entity.endTime = it }
        entity.slotDuration = dto.slotDuration
    }
}