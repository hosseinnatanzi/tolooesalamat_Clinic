package ir.tolooesalamat.app.crypto.core

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.Base64

/**
 * سرویس امضای دیجیتال با RSA.
 *
 * کاربرد:
 *  - تأیید اصالت (Authenticity)
 *  - یکپارچگی داده (Integrity)
 *  - عدم انکار (Non-repudiation)
 *
 * الگوریتم: SHA256withRSA
 */
@Service
class DigitalSignatureService(
    private val rsaKeyManager: RsaKeyManager
) {

    private val log = LoggerFactory.getLogger(javaClass)

    // ═══════════════════════════════════════════
    // 📝 امضای عمومی
    // ═══════════════════════════════════════════

    /**
     * امضای داده.
     * @return Base64 امضا
     */
    fun sign(data: String): String {
        val signatureBytes = rsaKeyManager.sign(data.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(signatureBytes)
    }

    /**
     * بررسی امضا.
     */
    fun verify(data: String, signatureBase64: String): Boolean = try {
        rsaKeyManager.verify(
            data.toByteArray(Charsets.UTF_8),
            Base64.getDecoder().decode(signatureBase64)
        )
    } catch (e: Exception) {
        log.debug("خطا در بررسی امضا: ${e.message}")
        false
    }

    // ═══════════════════════════════════════════
    // 🩺 امضای تشخیص پزشک
    // ═══════════════════════════════════════════

    /**
     * امضای تشخیص پزشک.
     *
     * Payload: "doctor={id}|patient={id}|diagnosis={text}|ts={timestamp}"
     */
    fun signDiagnosis(
        doctorId: Long,
        patientId: Long,
        diagnosis: String,
        timestamp: Long
    ): String {
        val payload = buildDiagnosisPayload(doctorId, patientId, diagnosis, timestamp)
        return sign(payload)
    }

    /**
     * بررسی امضای تشخیص پزشک.
     */
    fun verifyDiagnosis(
        doctorId: Long,
        patientId: Long,
        diagnosis: String,
        timestamp: Long,
        signature: String
    ): Boolean {
        val payload = buildDiagnosisPayload(doctorId, patientId, diagnosis, timestamp)
        return verify(payload, signature)
    }

    // ═══════════════════════════════════════════
    // 🧠 امضای نتیجه تست روانشناسی
    // ═══════════════════════════════════════════

    /**
     * امضای نتیجه تست روانشناسی.
     */
    fun signTestResult(
        doctorId: Long,
        patientId: Long,
        testId: Long,
        interpretation: String,
        timestamp: Long
    ): String {
        val payload = buildTestResultPayload(
            doctorId, patientId, testId, interpretation, timestamp
        )
        return sign(payload)
    }

    fun verifyTestResult(
        doctorId: Long,
        patientId: Long,
        testId: Long,
        interpretation: String,
        timestamp: Long,
        signature: String
    ): Boolean {
        val payload = buildTestResultPayload(
            doctorId, patientId, testId, interpretation, timestamp
        )
        return verify(payload, signature)
    }

    // ═══════════════════════════════════════════
    // 🛠 متدهای کمکی
    // ═══════════════════════════════════════════

    private fun buildDiagnosisPayload(
        doctorId: Long,
        patientId: Long,
        diagnosis: String,
        timestamp: Long
    ): String = "doctor=$doctorId|patient=$patientId|diagnosis=$diagnosis|ts=$timestamp"

    private fun buildTestResultPayload(
        doctorId: Long,
        patientId: Long,
        testId: Long,
        interpretation: String,
        timestamp: Long
    ): String = "doctor=$doctorId|patient=$patientId|test=$testId|interp=$interpretation|ts=$timestamp"
}