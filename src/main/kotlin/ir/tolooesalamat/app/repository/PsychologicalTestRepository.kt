package ir.tolooesalamat.app.repository

import ir.tolooesalamat.app.domain.PsychologicalTest
import ir.tolooesalamat.app.domain.TestCategory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PsychologicalTestRepository : JpaRepository<PsychologicalTest, Long> {

    // ═══════════════════════════════════════════
    // 🔍 جستجو‌های پایه
    // ═══════════════════════════════════════════

    /** لیست تست‌های فعال (برای UI) */
    fun findAllByActiveTrue(): List<PsychologicalTest>

    /** لیست تست‌های غیرفعال (برای ادمین) */
    fun findAllByActiveFalse(): List<PsychologicalTest>

    /** تست‌های یک دسته‌بندی خاص */
    fun findAllByCategoryAndActiveTrue(category: TestCategory): List<PsychologicalTest>

    /** جستجو با نام (فعال) */
    fun findByNameAndActiveTrue(name: String): PsychologicalTest?

    /** جستجو با کد (فعال) */
    fun findByCodeAndActiveTrue(code: String): PsychologicalTest?

    // ═══════════════════════════════════════════
    // ✅ بررسی وجود
    // ═══════════════════════════════════════════

    /** بررسی وجود نام */
    fun existsByName(name: String): Boolean

    /** بررسی وجود کد */
    fun existsByCode(code: String): Boolean

    // ═══════════════════════════════════════════
    // 📊 آمار
    // ═══════════════════════════════════════════

    /** تعداد تست‌های فعال */
    fun countByActiveTrue(): Long

    /** تعداد تست‌های یک دسته‌بندی */
    fun countByCategoryAndActiveTrue(category: TestCategory): Long
}