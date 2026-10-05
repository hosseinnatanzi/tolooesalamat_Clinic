package ir.tolooesalamat.app.mapper

import ir.tolooesalamat.app.domain.User
import ir.tolooesalamat.app.dto.UserDto
import ir.tolooesalamat.app.dto.UserSummaryDto
import org.springframework.stereotype.Component

@Component
class UserMapper {

    /**
     * Entity → DTO کامل.
     * رمز عبور هرگز برگردانده نمی‌شود.
     */
    fun toDto(user: User): UserDto = UserDto(
        id = user.id,
        phone = user.phone,
        landline = user.landline,
        password = null,                    // ← هرگز برنگردان
        firstName = user.firstName,
        lastName = user.lastName,
        age = user.age,
        gender = user.gender,
        username = user.username,
        email = user.email,
        nationalId = user.nationalId,
        role = user.role,
        enabled = user.enabled,
        clinicId = user.clinic?.id,
        clinicName = user.clinic?.name,
        createdAt = user.createdAt,
        updatedAt = user.updatedAt
    )

    /**
     * Entity → UserSummaryDto.
     */
    fun toSummary(user: User): UserSummaryDto = UserSummaryDto(
        id = user.id ?: 0L,
        phone = user.phone,
        landline = user.landline,
        fullName = user.fullName,
        firstName = user.firstName,
        lastName = user.lastName,
        age = user.age,
        gender = user.gender.name,
        genderLabel = user.gender.label,
        role = user.role.name,
        roleLabel = user.role.label
    )

    /**
     * DTO → Entity جدید.
     * نیاز به رمز هش‌شده دارد.
     */
    fun toEntity(dto: UserDto, encodedPassword: String): User = User(
        phone = dto.phone,
        landline = dto.landline,
        password = encodedPassword,
        firstName = dto.firstName.trim(),
        lastName = dto.lastName.trim(),
        age = dto.age,
        gender = dto.gender ?: ir.tolooesalamat.app.domain.Gender.FEMALE,
        username = dto.username ?: "user_${dto.phone}",
        email = dto.email,
        nationalId = dto.nationalId,
        role = dto.role,
        enabled = dto.enabled
    )

    /**
     * به‌روزرسانی Entity از DTO.
     */
    fun updateEntity(entity: User, dto: UserDto) {
        entity.phone = dto.phone
        entity.landline = dto.landline
        entity.firstName = dto.firstName.trim()
        entity.lastName = dto.lastName.trim()
        entity.age = dto.age
        dto.gender?.let { entity.gender = it }
        entity.email = dto.email
        entity.nationalId = dto.nationalId
        entity.role = dto.role
        entity.enabled = dto.enabled
    }
}