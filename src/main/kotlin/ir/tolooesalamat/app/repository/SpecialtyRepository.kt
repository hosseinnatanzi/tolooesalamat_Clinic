package ir.tolooesalamat.app.repository

import ir.tolooesalamat.app.domain.Specialty
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SpecialtyRepository : JpaRepository<Specialty, Long> {

    /** لیست تخصص‌های فعال (برای UI) */
    fun findAllByActiveTrue(): List<Specialty>

    /** لیست تخصص‌های غیرفعال (برای ادمین) */
    fun findAllByActiveFalse(): List<Specialty>

    /** جستجو با نام (فعال) */
    fun findByNameAndActiveTrue(name: String): Specialty?

    /** جستجو با کد (فعال) */
    fun findByCodeAndActiveTrue(code: String): Specialty?

    /** بررسی وجود نام */
    fun existsByName(name: String): Boolean

    /** بررسی وجود کد */
    fun existsByCode(code: String): Boolean

    /** شمارش تخصص‌های فعال */
    fun countByActiveTrue(): Long
}