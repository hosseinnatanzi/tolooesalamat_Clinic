package ir.tolooesalamat.app.repository.spec

import ir.tolooesalamat.app.domain.User
import ir.tolooesalamat.app.domain.enum.Role        // ← import جدید
import jakarta.persistence.criteria.Predicate
import org.springframework.data.jpa.domain.Specification

object UserSpecifications {

    fun isNotDeleted(): Specification<User> =
        Specification { root, _, cb ->
            cb.equal(root.get<Boolean>("isDeleted"), false)
        }

    fun hasRole(role: Role?): Specification<User> =
        Specification { root, _, cb ->
            if (role == null) cb.conjunction()
            else cb.equal(root.get<Role>("role"), role)
        }

    fun isEnabled(enabled: Boolean?): Specification<User> =
        Specification { root, _, cb ->
            if (enabled == null) cb.conjunction()
            else cb.equal(root.get<Boolean>("enabled"), enabled)
        }

    fun matchesKeyword(keyword: String?): Specification<User> =
        Specification { root, _, cb ->
            if (keyword.isNullOrBlank()) cb.conjunction()
            else {
                val pattern = "%${keyword.lowercase()}%"
                val predicates = mutableListOf<Predicate>()

                predicates.add(
                    cb.like(
                        cb.lower(cb.concat(cb.concat(cb.coalesce(root.get<String>("firstName"), ""), " "), cb.coalesce(root.get<String>("lastName"), ""))),
                        pattern
                    )
                )
                predicates.add(cb.like(root.get<String>("phone"), pattern))
                predicates.add(cb.like(cb.lower(cb.coalesce(root.get<String>("email"), "")), pattern))
                predicates.add(cb.like(cb.coalesce(root.get<String>("nationalId"), ""), pattern))

                cb.or(*predicates.toTypedArray())
            }
        }

    fun belongsToClinic(clinicId: Long?): Specification<User> =
        Specification { root, _, cb ->
            if (clinicId == null) cb.conjunction()
            else cb.equal(root.get<Any>("clinic").get<Long>("id"), clinicId)
        }
}