package ir.tolooesalamat.app.repository

import ir.tolooesalamat.app.domain.TestResult
import ir.tolooesalamat.app.domain.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface TestResultRepository : JpaRepository<TestResult, Long> {

    fun findAllByPatientOrderByTestDateDesc(patient: User): List<TestResult>

    fun findAllByDoctorOrderByTestDateDesc(doctor: User): List<TestResult>

    fun findAllByPatientAndTestId(patient: User, testId: Long): List<TestResult>

    fun countByPatient(patient: User): Long

    @Query("""
        SELECT r FROM TestResult r
        JOIN FETCH r.test t
        JOIN FETCH r.doctor d
        WHERE r.patient.id = :patientId
        ORDER BY r.testDate DESC
    """)
    fun findLatestResults(
        @Param("patientId") patientId: Long,
        pageable: Pageable
    ): Page<TestResult>

    @Query("""
        SELECT r FROM TestResult r
        JOIN FETCH r.patient p
        JOIN FETCH r.doctor d
        JOIN FETCH r.test t
        WHERE (:patientId IS NULL OR p.id = :patientId)
        AND (:doctorId IS NULL OR d.id = :doctorId)
        AND (:testId IS NULL OR t.id = :testId)
        AND (:from IS NULL OR r.testDate >= :from)
        AND (:to IS NULL OR r.testDate <= :to)
        ORDER BY r.testDate DESC
    """)
    fun search(
        @Param("patientId") patientId: Long?,
        @Param("doctorId") doctorId: Long?,
        @Param("testId") testId: Long?,
        @Param("from") from: LocalDateTime?,
        @Param("to") to: LocalDateTime?,
        pageable: Pageable
    ): Page<TestResult>
}