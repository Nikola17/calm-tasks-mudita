package com.yugesa.calmtasks

data class LanguageOption(val code: String, val label: String)

object LanguageCatalog {
    val options = listOf(
        LanguageOption("en", "English"),
        LanguageOption("fr", "Français"),
        LanguageOption("pl", "Polski"),
        LanguageOption("es", "Español"),
        LanguageOption("pt", "Português"),
        LanguageOption("it", "Italiano"),
        LanguageOption("de", "Deutsch"),
        LanguageOption("ru", "Русский"),
        LanguageOption("uk", "Українська"),
        LanguageOption("ro", "Română"),
        LanguageOption("nl", "Nederlands"),
        LanguageOption("tr", "Türkçe"),
        LanguageOption("hu", "Magyar"),
        LanguageOption("el", "Ελληνικά"),
        LanguageOption("cs", "Čeština"),
        LanguageOption("sv", "Svenska"),
        LanguageOption("ca", "Català"),
        LanguageOption("sr-Latn", "Srpskohrvatski"),
        LanguageOption("bg", "Български"),
        LanguageOption("sq", "Shqip"),
        LanguageOption("da", "Dansk"),
        LanguageOption("fi", "Suomi"),
        LanguageOption("no", "Norsk"),
    )

    val codes: List<String> = listOf(LanguageStore.SYSTEM) + options.map { it.code }
}
