package ir.tolooesalamat.app.crypto.core

import org.springframework.stereotype.Service
import java.util.Base64

@Service
class DigitalSignatureService(
    private val rsaKeyManager: RsaKeyManager
) {
    fun sign(data: String): String =
        Base64.getEncoder().encodeToString(
            rsaKeyManager.sign(data.toByteArray(Charsets.UTF_8))
        )

    fun verify(data: String, signatureBase64: String): Boolean = try {
        rsaKeyManager.verify(
            data.toByteArray(Charsets.UTF_8),
            Base64.getDecoder().decode(signatureBase64)
        )
    } catch (e: Exception) { false }

    fun signDiagnosis(doctorId: Long, patientId: Long, diagnosis: String, timestamp: Long): String =
        sign("doctor=$doctorId|patient=$patientId|diagnosis=$diagnosis|ts=$timestamp")

    fun verifyDiagnosis(
        doctorId: Long, patientId: Long, diagnosis: String,
        timestamp: Long, signature: String
    ): Boolean = verify(
        "doctor=$doctorId|patient=$patientId|diagnosis=$diagnosis|ts=$timestamp",
        signature
    )

    fun signTestResult(
        doctorId: Long, patientId: Long, testId: Long,
        interpretation: String, timestamp: Long
    ): String = sign(
        "doctor=$doctorId|patient=$patientId|test=$testId|interp=$interpretation|ts=$timestamp"
    )
}