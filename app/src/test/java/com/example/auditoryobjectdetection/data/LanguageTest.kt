package com.example.auditoryobjectdetection.data

import org.junit.Assert.*
import org.junit.Test

class LanguageTest {
    @Test
    fun fromString() {
        assertEquals(Language.ENGLISH, Language.fromString(null))
        assertEquals(Language.ENGLISH, Language.fromString("english"))

        assertEquals(Language.BEMBA, Language.fromString("bemba"))
        assertEquals(Language.ENGLISH, Language.fromString("something-else"))
    }
}
