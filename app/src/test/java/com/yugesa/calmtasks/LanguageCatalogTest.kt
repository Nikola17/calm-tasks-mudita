package com.yugesa.calmtasks

import org.junit.Assert.assertEquals
import org.junit.Test

class LanguageCatalogTest {
    @Test
    fun includesAllSupportedLanguageCodes() {
        assertEquals(
            listOf(
                "",
                "en",
                "fr",
                "pl",
                "es",
                "pt",
                "it",
                "de",
                "ru",
                "uk",
                "ro",
                "nl",
                "tr",
                "hu",
                "el",
                "cs",
                "sv",
                "ca",
                "sr-Latn",
                "bg",
                "sq",
                "da",
                "fi",
                "no",
            ),
            LanguageCatalog.codes,
        )
    }
}
