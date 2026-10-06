package ir.tolooesalamat.app.repository

import ir.tolooesalamat.app.domain.PsychologicalTest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PsychologicalTestRepository : JpaRepository<PsychologicalTest, Long> {

    fun findAllByActiveTrue(): List<PsychologicalTest>
    fun findAllByActiveFalse(): List<PsychologicalTest>

    fun findByNameAndActiveTrue(name: String): PsychologicalTest?
    fun findByCodeAndActiveTrue(code: String): PsychologicalTest?

    fun existsByName(name: String): Boolean
    fun existsByCode(code: String): Boolean

    fun countByActiveTrue(): Long
}