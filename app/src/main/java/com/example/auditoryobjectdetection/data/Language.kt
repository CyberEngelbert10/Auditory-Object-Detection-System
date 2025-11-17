package com.example.auditoryobjectdetection.data

enum class Language(val code: String) {
    ENGLISH("english"),
    NYANJA("nyanja"),
    BEMBA("bemba");

    override fun toString(): String = code

    companion object {
        fun fromString(value: String?): Language = when (value?.lowercase()) {
            "nyanja" -> NYANJA
            "bemba" -> BEMBA
            else -> ENGLISH
        }
    }
}
