package ir.tolooesalamat.app.service

import ir.tolooesalamat.app.crypto.core.DigitalSignatureService
import ir.tolooesalamat.app.domain.enum.Role
import ir.tolooesalamat.app.domain.TestResult
import ir.tolooesalamat.app.domain.User
import ir.tolooesalamat.app.dto.*
import ir.tolooesalamat.app.exception.AccessDeniedException
import ir.tolooesalamat.app.exception.BusinessException
import ir.tolooesalamat.app.exception.DuplicateResourceException
import ir.tolooesalamat.app.exception.ResourceNotFoundException
import ir.tolooesalamat.app.mapper.PsychologicalTestMapper
import ir.tolooesalamat.app.mapper.TestResultMapper
import ir.tolooesalamat.app.repository.PsychologicalTestRepository
import ir.tolooesalamat.app.repository.TestResultRepository
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
class PsychologicalTestService(
    private val testRepository: PsychologicalTestRepository,
    private val testResultRepository: TestResultRepository,
    private val userRepository: UserRepository,
    private val testMapper: PsychologicalTestMapper,
    private val resultMapper: TestResultMapper,
    private val digitalSignatureService: DigitalSignatureService,
    private val accessLogService: AccessLogService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    // ═══════════════════════════════════════════
    // 📋 کاتالوگ تست‌ها
    // ═══════════════════════════════════════════

    fun getActiveTests(): List<PsychologicalTestSummaryDto> =
        testRepository.findAllByActiveTrue().map { testMapper.toSummary(it) }

    @PreAuthorize("hasRole('ADMIN')")
    fun getAllTests(): List<PsychologicalTestDto> =
        testRepository.findAll().map { testMapper.toDto(it) }

    fun getTestById(id: Long): PsychologicalTestDto =
        testMapper.toDto(
            testRepository.findById(id)
                .orElseThrow { ResourceNotFoundException.of("تست", id) }
        )

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    fun createTest(dto: PsychologicalTestDto, currentUser: User): PsychologicalTestDto {
        if (testRepository.existsByName(dto.name)) {
            throw DuplicateResourceException.of("تست", "name", dto.name)
        }
        if (dto.code != null && testRepository.existsByCode(dto.code)) {
            throw DuplicateResourceException.of("تست", "code", dto.code)
        }

        val test = testMapper.toEntity(dto)
        val saved = testRepository.save(test)

        accessLogService.logAccess(
            user = currentUser,
            resourceType = "PSYCHOLOGICAL_TEST",
            resourceId = saved.id,
            action = "CREATE",
            details = "ایجاد تست: ${saved.name}"
        )

        log.info("✅ تست جدید: ${saved.name}")
        return testMapper.toDto(saved)
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    fun updateTest(
        id: Long,
        dto: PsychologicalTestDto,
        currentUser: User
    ): PsychologicalTestDto {
        val test = testRepository.findById(id)
            .orElseThrow { ResourceNotFoundException.of("تست", id) }

        if (test.name != dto.name && testRepository.existsByName(dto.name)) {
            throw DuplicateResourceException.of("تست", "name", dto.name)
        }

        testMapper.updateEntity(test, dto)
        val saved = testRepository.save(test)

        return testMapper.toDto(saved)
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    fun toggleTestActive(id: Long, currentUser: User): PsychologicalTestDto {
        val test = testRepository.findById(id)
            .orElseThrow { ResourceNotFoundException.of("تست", id) }

        test.active = !test.active
        val saved = testRepository.save(test)

        accessLogService.logAccess(
            user = currentUser,
            resourceType = "PSYCHOLOGICAL_TEST",
            resourceId = id,
            action = if (saved.active) "ENABLE" else "DISABLE"
        )

        return testMapper.toDto(saved)
    }

    // ═══════════════════════════════════════════
    // 🔒 ثبت نتیجه تست (فقط پزشک)
    // ═══════════════════════════════════════════

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    fun recordTestResult(
        request: RecordTestResultRequest,
        currentUser: User
    ): TestResultDto {

        val patient = userRepository.findById(request.patientId!!)
            .orElseThrow { ResourceNotFoundException.of("بیمار", request.patientId) }

        if (patient.role != Role.PATIENT) {
            throw BusinessException("کاربر انتخابی بیمار نیست")
        }

        val test = testRepository.findById(request.testId!!)
            .orElseThrow { ResourceNotFoundException.of("تست", request.testId) }

        if (!test.active) {
            throw BusinessException("این تست غیرفعال است")
        }

        val result = TestResult(
            patient = patient,
            doctor = currentUser,
            test = test,
            testDate = LocalDateTime.now(),
            rawScore = request.rawScore,
            interpretation = request.interpretation,
            diagnosis = request.diagnosis,
            rawData = request.rawData,
            recommendations = request.recommendations,
            confidentialityLevel = TestResult.LEVEL_HIGHLY_CONFIDENTIAL
        )

        val saved = testResultRepository.save(result)

        accessLogService.logAccess(
            user = currentUser,
            resourceType = "TEST_RESULT",
            resourceId = saved.id,
            action = "CREATE",
            details = "نتیجه تست ${test.name} برای ${patient.phone}"
        )

        log.info("✅ نتیجه تست ثبت شد: test=${test.name}, patient=${patient.phone}")
        return resultMapper.toDto(saved)
    }

    // ═══════════════════════════════════════════
    // 🔏 تأیید نهایی با امضای دیجیتال
    // ═══════════════════════════════════════════

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    fun finalizeTestResult(id: Long, currentUser: User): TestResultDto {
        val result = findWithPermission(id, currentUser)

        if (result.doctor.id != currentUser.id && currentUser.role != Role.ADMIN) {
            throw AccessDeniedException("فقط پزشک معالج می‌تواند تأیید نهایی کند")
        }

        val interpretation = result.interpretation
        if (interpretation.isNullOrBlank()) {
            throw BusinessException("تفسیر نتیجه خالی است")
        }

        val timestamp = System.currentTimeMillis()
        val signature = digitalSignatureService.signTestResult(
            doctorId = currentUser.id!!,
            patientId = result.patient.id!!,
            testId = result.test.id!!,
            interpretation = interpretation,
            timestamp = timestamp
        )

        result.doctorSignature = signature
        val saved = testResultRepository.save(result)

        accessLogService.logAccess(
            user = currentUser,
            resourceType = "TEST_RESULT",
            resourceId = id,
            action = "FINALIZE",
            details = "امضای دیجیتال پزشک"
        )

        return resultMapper.toDto(saved)
    }

    // ═══════════════════════════════════════════
    // 🔍 خواندن نتیجه (با کنترل دسترسی)
    // ═══════════════════════════════════════════

    fun getTestResultById(id: Long, currentUser: User): TestResultDto {
        val result = findWithPermission(id, currentUser)

        accessLogService.logAccess(
            user = currentUser,
            resourceType = "TEST_RESULT",
            resourceId = id,
            action = "VIEW"
        )

        return resultMapper.toDto(result)
    }

    // ═══════════════════════════════════════════
    // 📊 بررسی طول درمان (سابقه تست‌های بیمار)
    // ═══════════════════════════════════════════

    fun getPatientTestHistory(
        patientId: Long,
        currentUser: User
    ): List<TestResultSummaryDto> {

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
                resourceType = "PATIENT_TEST_HISTORY",
                resourceId = patientId,
                action = "VIEW",
                success = false,
                details = "دسترسی غیرمجاز"
            )
            throw AccessDeniedException("شما به سابقه تست‌های این بیمار دسترسی ندارید")
        }

        accessLogService.logAccess(
            user = currentUser,
            resourceType = "PATIENT_TEST_HISTORY",
            resourceId = patientId,
            action = "VIEW",
            success = true,
            details = "بررسی طول درمان با تست"
        )

        return testResultRepository
            .findAllByPatientOrderByTestDateDesc(patient)
            .map { resultMapper.toSummary(it) }
    }

    /**
     * جستجوی نتایج تست با فیلترهای متعدد.
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    fun searchTestResults(
        request: TestResultSearchRequest
    ): PagedResponse<TestResultSummaryDto> {

        val pageable = PageRequest.of(
            request.page.coerceAtLeast(0),
            request.size.coerceIn(1, 100),
            Sort.by(Sort.Direction.DESC, "testDate")
        )

        val page = testResultRepository.search(
            patientId = request.patientId,
            doctorId = request.doctorId,
            testId = request.testId,
            from = request.from,
            to = request.to,
            pageable = pageable
        )

        return PagedResponse(
            content = page.content.map { resultMapper.toSummary(it) },
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
     * آخرین N نتیجه یک بیمار (برای بررسی روند).
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    fun getLatestResults(
        patientId: Long,
        limit: Int = 10,
        currentUser: User
    ): List<TestResultSummaryDto> {

        val patient = userRepository.findById(patientId)
            .orElseThrow { ResourceNotFoundException.of("بیمار", patientId) }

        val pageable = PageRequest.of(0, limit.coerceIn(1, 50))

        val page = testResultRepository.findLatestResults(patient.id!!, pageable)

        accessLogService.logAccess(
            user = currentUser,
            resourceType = "PATIENT_TEST_HISTORY",
            resourceId = patientId,
            action = "VIEW",
            details = "آخرین $limit نتیجه"
        )

        return page.content.map { resultMapper.toSummary(it) }
    }

    // ═══════════════════════════════════════════
    // 🛠 متد کمکی — بررسی دسترسی
    // ═══════════════════════════════════════════

    private fun findWithPermission(id: Long, currentUser: User): TestResult {
        val result = testResultRepository.findById(id)
            .orElseThrow { ResourceNotFoundException.of("نتیجه تست", id) }

        val hasAccess = when (currentUser.role) {
            Role.ADMIN -> true
            Role.DOCTOR -> result.doctor.id == currentUser.id
            Role.PATIENT -> result.patient.id == currentUser.id
            Role.RECEPTIONIST -> false
        }

        if (!hasAccess) {
            accessLogService.logAccess(
                user = currentUser,
                resourceType = "TEST_RESULT",
                resourceId = id,
                action = "VIEW",
                success = false,
                details = "دسترسی غیرمجاز"
            )
            throw AccessDeniedException("شما به این نتیجه تست دسترسی ندارید")
        }

        return result
    }
}