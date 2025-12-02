package com.example.auditoryobjectdetection.integration

import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.util.regex.Pattern

class RawFilesExistTest {
    @Test
    fun labelsHaveFiles() {
        val labelFile = File("app/src/main/assets/labelmap.txt")
        assertTrue("labelmap exists", labelFile.exists())
        val labels = labelFile.readLines().map { it.trim() }.filter { it.isNotEmpty() }

        val rawDir = File("app/src/main/res/raw")
        assertTrue("raw dir exists", rawDir.exists())

        val missing = ArrayList<String>()
        val pattern = Pattern.compile("^[a-z0-9_]+_bemba\\.m4a$")
        for (label in labels) {
            val base = label.lowercase().replace('-', '_').replace(' ', '_').replace(Regex("[^a-z0-9_]"), "")
            val fname = "${base}_bemba.m4a"
            val file = File(rawDir, fname)
            if (!file.exists()) missing.add(fname)
        }
        assertTrue("All expected bemba files exist: missingCount=${missing.size}, sampleMissing=${if (missing.isNotEmpty()) missing[0] else "none"}", missing.isEmpty())
    }
}
