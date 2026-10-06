package ir.tolooesalamat.app.repository

import ir.tolooesalamat.app.domain.AccessLog
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AccessLogRepository : JpaRepository<AccessLog, Long>