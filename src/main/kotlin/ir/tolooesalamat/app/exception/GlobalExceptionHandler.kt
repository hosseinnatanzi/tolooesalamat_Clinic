package ir.tolooesalamat.app.exception

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.security.SignatureException
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.LockedException
import org.springframework.security.core.AuthenticationException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.NoHandlerFoundException

/**
 * مدیریت متمرکز تمام خطاهای پروژه.
 *
 * هر خطا به یک پاسخ JSON استاندارد تبدیل می‌شود:
 * {
 *   "timestamp": "2024-04-15T10:30:00",
 *   "status": 400,
 *   "error": "BAD_REQUEST",
 *   "message": "اطلاعات ورودی نامعتبر است",
 *   "path": "/api/auth/login",
 *   "validationErrors": { "phone": "شماره موبایل نامعتبر است" }
 * }
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    // ═══════════════════════════════════════════
    // 4xx — خطاهای کلاینت
    // ═══════════════════════════════════════════

    /**
     * 400 — Business Exception
     */
    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(
        ex: BusinessException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        log.warn("خطای کسب‌وکار: ${ex.message} - path=${request.requestURI}")

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse.of(
                    status = 400,
                    error = "BUSINESS_ERROR",
                    code = ex.code,
                    message = ex.message ?: "خطای کسب‌وکار",
                    path = request.requestURI
                )
            )
    }

    /**
     * 400 — Validation Exception
     */
    @ExceptionHandler(ValidationException::class)
    fun handleValidationException(
        ex: ValidationException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        log.warn("خطای اعتبارسنجی: ${ex.message}")

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse.of(
                    status = 400,
                    error = "VALIDATION_ERROR",
                    message = ex.message ?: "اطلاعات ورودی نامعتبر است",
                    path = request.requestURI
                ).copy(validationErrors = ex.errors)
            )
    }

    /**
     * 400 — Method Argument Not Valid (از @Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val errors = ex.bindingResult.fieldErrors.associate {
            it.field to (it.defaultMessage ?: "مقدار نامعتبر")
        }

        log.warn("خطای اعتبارسنجی فیلدها: $errors")

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    status = 400,
                    error = "VALIDATION_ERROR",
                    message = "اطلاعات ورودی نامعتبر است",
                    path = request.requestURI,
                    validationErrors = errors
                )
            )
    }

    /**
     * 400 — Constraint Violation (از @Validated روی پارامترها)
     */
    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(
        ex: ConstraintViolationException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val errors = ex.constraintViolations.associate {
            it.propertyPath.toString() to it.message
        }

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    status = 400,
                    error = "VALIDATION_ERROR",
                    message = "اطلاعات ورودی نامعتبر است",
                    path = request.requestURI,
                    validationErrors = errors
                )
            )
    }

    /**
     * 400 — Bad Request (missing params, wrong types)
     */
    @ExceptionHandler(
        MissingServletRequestParameterException::class,
        MethodArgumentTypeMismatchException::class,
        HttpMessageNotReadableException::class,
        IllegalArgumentException::class
    )
    fun handleBadRequest(
        ex: Exception,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        log.warn("درخواست نامعتبر: ${ex.message}")

        val message = when (ex) {
            is MissingServletRequestParameterException ->
                "پارامتر '${ex.parameterName}' الزامی است"
            is MethodArgumentTypeMismatchException ->
                "مقدار پارامتر '${ex.name}' نامعتبر است"
            is HttpMessageNotReadableException ->
                "بدنه درخواست نامعتبر است"
            else -> ex.message ?: "درخواست نامعتبر"
        }

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse.of(
                    status = 400,
                    error = "BAD_REQUEST",
                    message = message,
                    path = request.requestURI
                )
            )
    }

    /**
     * 401 — Unauthorized (BadCredentials)
     */
    @ExceptionHandler(BadCredentialsException::class)
    fun handleBadCredentials(
        ex: BadCredentialsException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        log.warn("اعتبارنامه اشتباه: ${request.requestURI}")

        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(
                ErrorResponse.of(
                    status = 401,
                    error = "BAD_CREDENTIALS",
                    message = "شماره موبایل یا رمز عبور اشتباه است",
                    path = request.requestURI
                )
            )
    }

    /**
     * 401 — Disabled account
     */
    @ExceptionHandler(DisabledException::class)
    fun handleDisabled(
        ex: DisabledException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(
                ErrorResponse.of(
                    status = 401,
                    error = "ACCOUNT_DISABLED",
                    message = "حساب کاربری غیرفعال است",
                    path = request.requestURI
                )
            )

    /**
     * 401 — Locked account
     */
    @ExceptionHandler(LockedException::class)
    fun handleLocked(
        ex: LockedException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(
                ErrorResponse.of(
                    status = 401,
                    error = "ACCOUNT_LOCKED",
                    message = "حساب کاربری قفل شده است. لطفاً بعداً تلاش کنید",
                    path = request.requestURI
                )
            )

    /**
     * 401 — Invalid Token (JWT)
     */
    @ExceptionHandler(InvalidTokenException::class)
    fun handleInvalidToken(
        ex: InvalidTokenException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(
                ErrorResponse.of(
                    status = 401,
                    error = "INVALID_TOKEN",
                    message = ex.message ?: "توکن نامعتبر است",
                    path = request.requestURI
                )
            )

    /**
     * 401 — JWT Exception
     */
    @ExceptionHandler(
        ExpiredJwtException::class,
        MalformedJwtException::class,
        SignatureException::class,
        JwtException::class
    )
    fun handleJwtException(
        ex: JwtException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val message = when (ex) {
            is ExpiredJwtException -> "توکن منقضی شده است. لطفاً دوباره وارد شوید"
            is MalformedJwtException -> "ساختار توکن نامعتبر است"
            is SignatureException -> "امضای توکن نامعتبر است"
            else -> "توکن نامعتبر است"
        }

        log.warn("JWT Exception: $message")

        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(
                ErrorResponse.of(
                    status = 401,
                    error = "INVALID_TOKEN",
                    message = message,
                    path = request.requestURI
                )
            )
    }

    /**
     * 401 — Authentication Exception عمومی
     */
    @ExceptionHandler(AuthenticationException::class)
    fun handleAuthenticationException(
        ex: AuthenticationException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(
                ErrorResponse.of(
                    status = 401,
                    error = "UNAUTHORIZED",
                    message = "احراز هویت نامعتبر است",
                    path = request.requestURI
                )
            )

    /**
     * 403 — Access Denied (Business)
     */
    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(
        ex: AccessDeniedException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        log.warn("دسترسی غیرمجاز: ${ex.message} - path=${request.requestURI}")

        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(
                ErrorResponse.of(
                    status = 403,
                    error = "ACCESS_DENIED",
                    message = ex.message ?: "شما به این بخش دسترسی ندارید",
                    path = request.requestURI
                )
            )
    }

    /**
     * 403 — Spring Security Access Denied
     */
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException::class)
    fun handleSpringAccessDenied(
        ex: org.springframework.security.access.AccessDeniedException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(
                ErrorResponse.of(
                    status = 403,
                    error = "FORBIDDEN",
                    message = "شما به این بخش دسترسی ندارید",
                    path = request.requestURI
                )
            )

    /**
     * 404 — Resource Not Found
     */
    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFound(
        ex: ResourceNotFoundException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        log.warn("منبع یافت نشد: ${ex.message}")

        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(
                ErrorResponse.of(
                    status = 404,
                    error = "NOT_FOUND",
                    message = ex.message ?: "منبع مورد نظر یافت نشد",
                    path = request.requestURI
                )
            )
    }

    /**
     * 404 — No Handler Found
     */
    @ExceptionHandler(NoHandlerFoundException::class)
    fun handleNoHandlerFound(
        ex: NoHandlerFoundException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(
                ErrorResponse.of(
                    status = 404,
                    error = "NOT_FOUND",
                    message = "آدرس '${ex.requestURL}' یافت نشد",
                    path = request.requestURI
                )
            )

    /**
     * 405 — Method Not Allowed
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleMethodNotSupported(
        ex: HttpRequestMethodNotSupportedException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(HttpStatus.METHOD_NOT_ALLOWED)
            .body(
                ErrorResponse.of(
                    status = 405,
                    error = "METHOD_NOT_ALLOWED",
                    message = "متد '${ex.method}' برای این آدرس مجاز نیست",
                    path = request.requestURI
                )
            )

    /**
     * 409 — Duplicate Resource
     */
    @ExceptionHandler(DuplicateResourceException::class)
    fun handleDuplicateResource(
        ex: DuplicateResourceException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        log.warn("منبع تکراری: ${ex.message}")

        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(
                ErrorResponse.of(
                    status = 409,
                    error = "DUPLICATE_RESOURCE",
                    message = ex.message ?: "این منبع قبلاً ثبت شده است",
                    path = request.requestURI
                )
            )
    }

    /**
     * 409 — Data Integrity Violation (Database constraint)
     */
    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrity(
        ex: DataIntegrityViolationException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        log.error("خطای یکپارچگی داده: ${ex.message}")

        val message = when {
            ex.message?.contains("duplicate key", ignoreCase = true) == true ->
                "این مقدار قبلاً در سیستم ثبت شده است"
            ex.message?.contains("foreign key", ignoreCase = true) == true ->
                "این عملیات به دلیل وابستگی‌های موجود امکان‌پذیر نیست"
            else -> "خطا در ذخیره‌سازی داده"
        }

        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(
                ErrorResponse.of(
                    status = 409,
                    error = "DATA_INTEGRITY_ERROR",
                    message = message,
                    path = request.requestURI
                )
            )
    }

    // ═══════════════════════════════════════════
    // 5xx — خطاهای سرور
    // ═══════════════════════════════════════════

    /**
     * 500 — Generic Exception
     */
    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        log.error("خطای غیرمنتظره در ${request.requestURI}: ${ex.message}", ex)

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                ErrorResponse.of(
                    status = 500,
                    error = "INTERNAL_ERROR",
                    message = "خطای داخلی سرور. لطفاً با پشتیبانی تماس بگیرید",
                    path = request.requestURI
                )
            )
    }
}