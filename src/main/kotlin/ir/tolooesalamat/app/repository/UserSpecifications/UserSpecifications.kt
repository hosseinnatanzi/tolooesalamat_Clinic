package ir.tolooesalamat.app.repository.spec

import ir.tolooesalamat.app.domain.Role
import ir.tolooesalamat.app.domain.User
import jakarta.persistence.criteria.Predicate
import org.springframework.data.jpa.domain.Specification

/**
 * Specification های User برای جستجوی پیشرفته.
 */
object UserSpecifications {

    /**
     * فیلتر رکوردهای حذف‌نشده.
     */
    fun isNotDeleted(): Specification<User> =
        Specification { root, _, cb ->
            cb.equal(root.get<Boolean>("isDeleted"), false)
        }

    /**
     * فیلتر بر اساس نقش.
     */
    fun hasRole(role: Role?): Specification<User> =
        Specification { root, _, cb ->
            if (role == null) {
                cb.conjunction()   // ← شرط همیشه true
            } else {
                cb.equal(root.get<Role>("role"), role)
            }
        }

    /**
     * فیلتر بر اساس فعال/غیرفعال.
     */
    fun isEnabled(enabled: Boolean?): Specification<User> =
        Specification { root, _, cb ->
            if (enabled == null) {
                cb.conjunction()
            } else {
                cb.equal(root.get<Boolean>("enabled"), enabled)
            }
        }

    /**
     * جستجو در: نام، نام خانوادگی، موبایل، ایمیل، کد ملی.
     */
    fun matchesKeyword(keyword: String?): Specification<User> =
        Specification { root, _, cb ->
            if (keyword.isNullOrBlank()) {
                cb.conjunction()
            } else {
                val pattern = "%${keyword.lowercase()}%"

                val predicates = mutableListOf<Predicate>()

                // نام و نام خانوادگی
                predicates.add(
                    cb.like(
                        cb.lower(
                            cb.concat(
                                cb.concat(
                                    cb.coalesce(root.get<String>("firstName"), ""),
                                    " "
                                ),
                                cb.coalesce(root.get<String>("lastName"), "")
                            )
                        ),
                        pattern
                    )
                )

                // موبایل
                predicates.add(
                    cb.like(root.get<String>("phone"), pattern)
                )

                // ایمیل
                predicates.add(
                    cb.like(
                        cb.lower(cb.coalesce(root.get<String>("email"), "")),
                        pattern
                    )
                )

                // کد ملی
                predicates.add(
                    cb.like(
                        cb.coalesce(root.get<String>("nationalId"), ""),
                        pattern
                    )
                )

                cb.or(*predicates.toTypedArray())
            }
        }

    /**
     * فیلتر بر اساس مطب.
     */
    fun belongsToClinic(clinicId: Long?): Specification<User> =
        Specification { root, _, cb ->
            if (clinicId == null) {
                cb.conjunction()
            } else {
                cb.equal(root.get<Any>("clinic").get<Long>("id"), clinicId)
            }
        }
}