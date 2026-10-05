package ir.tolooesalamat.app.service

import ir.tolooesalamat.app.crypto.core.DigitalSignatureService
import ir.tolooesalamat.app.domain.MedicalRecord
import ir.tolooesalamat.app.domain.Role
import ir.tolooesalamat.app.domain.User
import ir.tolooesalamat.app.dto.MedicalRecordDto
import ir.tolooesalamat.app.dto.MedicalRecordSearchRequest
import ir.tolooesalamat.app.dto.MedicalRecordSummaryDto
import ir.tolooesalamat.app.dto.PagedResponse
import ir.tolooesalamat.app.exception.AccessDeniedException
import ir.tolooesalamat.app.exception.ResourceNotFoundException
import ir.tolooesalamat.app.mapper.MedicalRecordMapper
import ir.tolooesalamat.app.repository.MedicalRecordRepository
import ir.tolooesalamat.app.repository.UserRepository
 import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class MedicalRecordService(
    private val medicalRecordRepository: MedicalRecordRepository,
    private val userRepository: UserRepository,
    private val medicalRecordMapper: MedicalRecordMapper,
    private val digitalSignatureService: DigitalSignatureService,
    private val accessLogService: AccessLogService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    // ═══════════════════════════════════════════
    // 📋 لیست
    // ═══════════════════════════════════════════

    fun search(request: MedicalRecordSearchRequest, currentUser: User): PagedResponse<MedicalRecordSummaryDto> {
        val pageable = PageRequest.of(
            request.page.coerceAtLeast(0),
            request.size.coerceIn(1, 100),
            Sort.by(Sort.Direction.DESC, "sessionDate")
        )

        val page = medicalRecordRepository.search(
            patientId = request.patientId,
            doctorId = request.doctorId,
            from = request.fromDate,
            to = request.toDate,
            pageable = pageable
        )

        return PagedResponse(
            content = page.content.map { medicalRecordMapper.toSummary(it) },
            page = page.number,
            size = page.size,
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            first = page.isFirst,
            last = page.isLast,
            hasNext = page.hasNext(),
            hasPrevious = page.hasPrevious()
        )
    }

    fun getPatientHistory(patientId: Long, currentUser: User): List<MedicalRecordSummaryDto> {
        val patient = userRepository.findById(patientId)
            .orElseThrow { ResourceNotFoundException.of("بیمار", patientId) }

        val hasAccess = when (currentUser.role) {
            Role.ADMIN, Role.DOCTOR -> true
            Role.PATIENT -> patient.id == currentUser.id
            Role.RECEPTIONIST -> false
        }

        if (!hasAccess) {
            throw AccessDeniedException("شما به سابقه این بیمار دسترسی ندارید")
        }

        accessLogService.logAccess(
            user = currentUser,
            resourceType = "PATIENT_HISTORY",
            resourceId = patientId,
            action = "VIEW"
        )

        return medicalRecordRepository.findAllByPatientOrderBySessionDateDesc(patient)
            .map { medicalRecordMapper.toSummary(it) }
    }

    // ═══════════════════════════════════════════
    // 🔍 خواندن
    // ═══════════════════════════════════════════

    fun getById(id: Long, currentUser: User): MedicalRecordDto {
        val record = findWithPermission(id, currentUser)

        accessLogService.logAccess(
            user = currentUser,
            resourceType = "MEDICAL_RECORD",
            resourceId = id,
            action = "VIEW"
        )

        return medicalRecordMapper.toDto(record)
    }

    // ═══════════════════════════════════════════
    // ➕ ثبت (فقط پزشک)
    // ═══════════════════════════════════════════

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    fun create(dto: MedicalRecordDto, currentUser: User): MedicalRecordDto {

        val patient = userRepository.findById(dto.patientId!!)
            .orElseThrow { ResourceNotFoundException.of("بیمار", dto.patientId) }

        if (patient.role != Role.PATIENT) {
            throw ir.tolooesalamat.app.exception.BusinessException("کاربر انتخابی بیمار نیست")
        }

        val sessionNumber = medicalRecordRepository.getMaxSessionNumber(patient.id!!) + 1

        val record = MedicalRecord(
            patient = patient,
            doctor = currentUser,
            sessionNumber = sessionNumber,
            chiefComplaint = dto.chiefComplaint,
            presentIllness = dto.presentIllness,
            mentalStatusExam = dto.mentalStatusExam,
            diagnosis = dto.diagnosis,
            treatmentPlan = dto.treatmentPlan,
            medications = dto.medications,
            sessionNotes = dto.sessionNotes
        )

        val saved = medicalRecordRepository.save(record)

        accessLogService.logAccess(
            user = currentUser,
            resourceType = "MEDICAL_RECORD",
            resourceId = saved.id,
            action = "CREATE",
            details = "جلسه $sessionNumber برای بیمار ${patient.phone}"
        )

        log.info("✅ پرونده ثبت شد: patient=${patient.phone}, session=$sessionNumber")
        return medicalRecordMapper.toDto(saved)
    }

    // ═══════════════════════════════════════════
    // 🔏 امضای دیجیتال
    // ═══════════════════════════════════════════

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    fun finalizeRecord(id: Long, currentUser: User): MedicalRecordDto {
        val record = findWithPermission(id, currentUser)

        if (record.doctor.id != currentUser.id && currentUser.role != Role.ADMIN) {
            throw AccessDeniedException("فقط پزشک معالج می‌تواند تأیید نهایی کند")
        }

        if (record.diagnosis.isNullOrBlank()) {
            throw ir.tolooesalamat.app.exception.BusinessException("تشخیص خالی است")
        }

        val timestamp = System.currentTimeMillis()
        val signature = digitalSignatureService.signDiagnosis(
            doctorId = currentUser.id!!,
            patientId = record.patient.id!!,
            diagnosis = record.diagnosis!!,
            timestamp = timestamp
        )

        record.diagnosisSignature = signature
        record.isFinalized = true
        record.finalizedAt = LocalDateTime.now()

        val saved = medicalRecordRepository.save(record)

        accessLogService.logAccess(
            user = currentUser,
            resourceType = "MEDICAL_RECORD",
            resourceId = id,
            action = "FINALIZE",
            details = "امضای دیجیتال پزشک"
        )

        log.info("✅ پرونده نهایی شد: id=$id")
        return medicalRecordMapper.toDto(saved)
    }

    fun verifySignature(id: Long, currentUser: User): Boolean {
        val record = findWithPermission(id, currentUser)

        val signature = record.diagnosisSignature ?: return false
        val diagnosis = record.diagnosis ?: return false
        val timestamp = record.createdAt?.atZone(java.time.ZoneId.systemDefault())
            ?.toInstant()?.toEpochMilli() ?: return false

        return digitalSignatureService.verifyDiagnosis(
            doctorId = record.doctor.id!!,
            patientId = record.patient.id!!,
            diagnosis = diagnosis,
            timestamp = timestamp,
            signature = signature
        )
    }

    // ═══════════════════════════════════════════
    // 🛠 متد کمکی
    // ═══════════════════════════════════════════

    private fun findWithPermission(id: Long, currentUser: User): MedicalRecord {
        val record = medicalRecordRepository.findById(id)
            .orElseThrow { ResourceNotFoundException.of("پرونده", id) }

        val hasAccess = when (currentUser.role) {
            Role.ADMIN -> true
            Role.DOCTOR -> record.doctor.id == currentUser.id
            Role.PATIENT -> record.patient.id == currentUser.id
            Role.RECEPTIONIST -> false
        }

        if (!hasAccess) {
            accessLogService.logAccess(
                user = currentUser,
                resourceType = "MEDICAL_RECORD",
                resourceId = id,
                action = "VIEW",
                success = false,
                details = "دسترسی غیرمجاز"
            )
            throw AccessDeniedException("شما به این پرونده دسترسی ندارید")
        }

        return record
    }
}