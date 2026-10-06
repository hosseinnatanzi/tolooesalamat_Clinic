package ir.tolooesalamat.app.repository

import ir.tolooesalamat.app.domain.DoctorProfile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface DoctorProfileRepository : JpaRepository<DoctorProfile, Long> {

    fun findByUserId(userId: Long): DoctorProfile?

    fun existsByMedicalCode(medicalCode: String): Boolean

    @Query("""
        select d from DoctorProfile d
        join fetch d.user u
        join fetch d.specialty s
        where d.active = true and u.active = true
    """)
    fun findAllActive(): List<DoctorProfile>

    @Query("""
        select d from DoctorProfile d
        join fetch d.user u
        join fetch d.specialty s
        where d.active = true and s.id = :specialtyId
    """)
    fun findActiveBySpecialtyId(@Param("specialtyId") specialtyId: Long): List<DoctorProfile>
}