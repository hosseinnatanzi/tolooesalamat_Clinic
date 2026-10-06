package ir.tolooesalamat.app.repository

import ir.tolooesalamat.app.domain.DoctorProfile
import org.springframework.data.jpa.repository.JpaRepository

interface DoctorProfileRepository : JpaRepository<DoctorProfile, Long>