package ir.tolooesalamat.app.repository

import ir.tolooesalamat.app.domain.Clinic
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ClinicRepository : JpaRepository<Clinic, Long> {

    /** پیدا کردن با نام */
    fun findByName(name: String): Clinic?

    /** بررسی وجود نام */
    fun existsByName(name: String): Boolean

    /** تمام مطب‌های فعال */
    fun findAllByActiveTrue(): List<Clinic>

    /** تمام مطب‌های غیرفعال */
    fun findAllByActiveFalse(): List<Clinic>

    /** شمارش مطب‌های فعال */
    fun countByActiveTrue(): Long
}