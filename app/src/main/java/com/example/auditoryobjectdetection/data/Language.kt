package com.example.auditoryobjectdetection.data

enum class Language(val code: String) {
    ENGLISH("english"),
    BEMBA("bemba");

    override fun toString(): String = code

    companion object {
        fun fromString(value: String?): Language = when (value?.lowercase()) {
            "bemba" -> BEMBA
            else -> ENGLISH
        }
    }
}
