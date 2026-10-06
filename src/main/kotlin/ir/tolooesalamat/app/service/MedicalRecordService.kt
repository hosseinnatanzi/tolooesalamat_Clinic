package ir.tolooesalamat.app.service

import ir.tolooesalamat.app.crypto.core.DigitalSignatureService
import ir.tolooesalamat.app.domain.MedicalRecord
import ir.tolooesalamat.app.domain.Role
import ir.tolooesalamat.app.domain.User
import ir.tolooesalamat.app.dto.*
import ir.tolooesalamat.app.exception.AccessDeniedException
import ir.tolooesalamat.app.exception.BusinessException
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
import java.time.ZoneId

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
    // 📋 لیست و جستجو
    // ═══════════════════════════════════════════

    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST')")
    fun search(
        request: MedicalRecordSearchRequest,
        currentUser: User
    ): PagedResponse<MedicalRecordSummaryDto> {

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

    /**
     * سابقه پزشکی یک بیمار — برای بررسی طول درمان.
     */
    fun getPatientHistory(
        patientId: Long,
        currentUser: User
    ): List<MedicalRecordSummaryDto> {

        val patient = userRepository.findById(patientId)
            .orElseThrow { ResourceNotFoundException.of("بیمار", patientId) }

        val hasAccess = when (currentUser.role) {
            Role.ADMIN, Role.DOCTOR -> true
            Role.PATIENT -> patient.id == currentUser.id
            Role.RECEPTIONIST -> false
        }

        if (!hasAccess) {
            accessLogService.logAccess(
                user = currentUser,
                resourceType = "PATIENT_HISTORY",
                resourceId = patientId,
                action = "VIEW",
                success = false,
                details = "دسترسی غیرمجاز"
            )
            throw AccessDeniedException("شما به سابقه این بیمار دسترسی ندارید")
        }

        accessLogService.logAccess(
            user = currentUser,
            resourceType = "PATIENT_HISTORY",
            resourceId = patientId,
            action = "VIEW",
            success = true,
            details = "بررسی طول درمان"
        )

        return medicalRecordRepository
            .findAllByPatientOrderBySessionDateDesc(patient)
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
            throw BusinessException("کاربر انتخابی بیمار نیست")
        }

        // شماره جلسه خودکار
        val sessionNumber = medicalRecordRepository
            .getMaxSessionNumber(patient.id!!) + 1

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
    // ✏️ به‌روزرسانی پرونده
    // ═══════════════════════════════════════════

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    fun update(id: Long, dto: MedicalRecordDto, currentUser: User): MedicalRecordDto {

        // 1. پیدا کردن پرونده با بررسی دسترسی
        val record = findWithPermission(id, currentUser)

        // 2. اگر نهایی شده، قابل ویرایش نیست (به جز ادمین)
        if (record.isFinalized && currentUser.role != Role.ADMIN) {
            throw BusinessException("پرونده تأیید‌شده قابل ویرایش نیست")
        }

        // 3. پزشک فقط پرونده خودش
        if (currentUser.role == Role.DOCTOR && record.doctor.id != currentUser.id) {
            throw AccessDeniedException("شما به این پرونده دسترسی ندارید")
        }

        // 4. به‌روزرسانی فیلدها
        record.chiefComplaint = dto.chiefComplaint
        record.presentIllness = dto.presentIllness
        record.mentalStatusExam = dto.mentalStatusExam
        record.diagnosis = dto.diagnosis
        record.treatmentPlan = dto.treatmentPlan
        record.medications = dto.medications
        record.sessionNotes = dto.sessionNotes

        // 5. ذخیره
        val saved = medicalRecordRepository.save(record)

        // 6. ثبت لاگ
        accessLogService.logAccess(
            user = currentUser,
            resourceType = "MEDICAL_RECORD",
            resourceId = id,
            action = "UPDATE",
            details = "به‌روزرسانی پرونده جلسه ${record.sessionNumber}"
        )

        log.info("✅ پرونده به‌روزرسانی شد: id=$id by ${currentUser.phone}")
        return medicalRecordMapper.toDto(saved)
    }

    // ═══════════════════════════════════════════
    // 🔏 تأیید نهایی با امضای دیجیتال
    // ═══════════════════════════════════════════

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    fun finalizeRecord(id: Long, currentUser: User): MedicalRecordDto {
        val record = findWithPermission(id, currentUser)

        // فقط پزشک معالج یا ادمین
        if (record.doctor.id != currentUser.id && currentUser.role != Role.ADMIN) {
            throw AccessDeniedException("فقط پزشک معالج می‌تواند تأیید نهایی کند")
        }

        if (record.isFinalized) {
            throw BusinessException("این پرونده قبلاً تأیید شده است")
        }

        if (record.diagnosis.isNullOrBlank()) {
            throw BusinessException("تشخیص نمی‌تواند خالی باشد")
        }

        // امضای دیجیتال
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

        log.info("✅ پرونده نهایی شد: id=$id by ${currentUser.phone}")
        return medicalRecordMapper.toDto(saved)
    }

    /**
     * بررسی صحت امضای دیجیتال.
     */
    fun verifySignature(id: Long, currentUser: User): Boolean {
        val record = findWithPermission(id, currentUser)

        val signature = record.diagnosisSignature
        val diagnosis = record.diagnosis

        if (signature.isNullOrBlank() || diagnosis.isNullOrBlank()) {
            return false
        }

        val timestamp = record.finalizedAt
            ?.atZone(ZoneId.systemDefault())
            ?.toInstant()
            ?.toEpochMilli() ?: return false

        return digitalSignatureService.verifyDiagnosis(
            doctorId = record.doctor.id!!,
            patientId = record.patient.id!!,
            diagnosis = diagnosis,
            timestamp = timestamp,
            signature = signature
        )
    }

    // ═══════════════════════════════════════════
    // 🛠 متد کمکی — بررسی دسترسی
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