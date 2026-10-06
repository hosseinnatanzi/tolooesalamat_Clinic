package ir.tolooesalamat.app.repository

import ir.tolooesalamat.app.domain.User
import ir.tolooesalamat.app.domain.enum.Role        // ← import جدید
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

@Repository
interface UserRepository :
    JpaRepository<User, Long>,
    JpaSpecificationExecutor<User> {

    fun findByPhone(phone: String): User?
    fun existsByPhone(phone: String): Boolean
    fun findByUsername(username: String): User?
    fun existsByUsername(username: String): Boolean
    fun findByEmail(email: String): User?
    fun existsByEmail(email: String): Boolean
    fun findAllByRole(role: Role): List<User>
    fun findAllByRoleAndEnabledTrue(role: Role): List<User>
}