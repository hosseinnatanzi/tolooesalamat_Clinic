package ir.tolooesalamat.app.domain.enum


enum class TestCategory(val label: String) {
    PERSONALITY("شخصیت"),
    DEPRESSION("افسردگی"),
    ANXIETY("اضطراب"),
    INTELLIGENCE("هوش"),
    MEMORY("حافظه"),
    ATTENTION("توجه و تمرکز"),
    CHILD("کودک و نوجوان"),
    OTHER("سایر");

    companion object {
        fun fromString(value: String?): TestCategory? =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
    }
}