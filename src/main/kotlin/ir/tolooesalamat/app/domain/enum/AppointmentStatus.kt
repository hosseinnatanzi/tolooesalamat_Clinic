package ir.tolooesalamat.app.domain.enum

enum class AppointmentStatus(val label: String) {
    PENDING("در انتظار تأیید"),
    CONFIRMED("تأیید شده"),
    CHECKED_IN("پذیرش شده"),
    IN_PROGRESS("در حال ویزیت"),
    COMPLETED("انجام شده"),
    CANCELLED("لغو شده"),
    NO_SHOW("عدم مراجعه")
}