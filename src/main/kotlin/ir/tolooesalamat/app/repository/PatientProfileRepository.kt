package ir.tolooesalamat.app.repository

import ir.tolooesalamat.app.domain.PatientProfile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface PatientProfileRepository : JpaRepository<PatientProfile, Long> {

    fun findByUserId(userId: Long): PatientProfile?
    fun findByFileNumber(fileNumber: String): PatientProfile?
    fun existsByFileNumber(fileNumber: String): Boolean

    @Query("""
        SELECT COALESCE(MAX(CAST(SUBSTRING(pp.fileNumber, 4) AS integer)), 0)
        FROM PatientProfile pp
        WHERE pp.fileNumber LIKE CONCAT(:prefix, '%')
    """)
    fun getMaxFileNumber(@Param("prefix") prefix: String): Int
}