package ir.tolooesalamat.app.domain

enum class Gender(val label: String) {
    MALE("مرد"),
    FEMALE("زن");

    companion object {
        fun fromString(value: String?): Gender? =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
    }
}