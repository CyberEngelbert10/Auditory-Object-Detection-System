package com.example.auditoryobjectdetection.util

object ResourceNameUtils {
    // Normalize a detected label into a valid resource base name:
    // 1) lowercase
    // 2) replace spaces/hyphens with underscores
    // 3) remove non-alphanumeric/underscore characters
    // 4) return base (e.g., "person" -> "person")
    fun normalizeLabel(label: String): String {
        val step1 = label.lowercase().replace('-', '_').replace(' ', '_')
        return Regex("[^a-z0-9_]").replace(step1, "")
    }

    fun toResourceName(label: String, suffix: String = "bemba"): String {
        val base = normalizeLabel(label)
        return "${base}_${suffix}"
    }

    fun toFilename(label: String, suffix: String = "bemba"): String {
        return toResourceName(label, suffix) + ".m4a"
    }
}
