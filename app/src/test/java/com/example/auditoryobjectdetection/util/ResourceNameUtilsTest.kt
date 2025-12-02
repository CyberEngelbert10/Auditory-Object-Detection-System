package com.example.auditoryobjectdetection.util

import org.junit.Assert.*
import org.junit.Test

class ResourceNameUtilsTest {

    @Test
    fun normalize_examples() {
        assertEquals("person", ResourceNameUtils.normalizeLabel("person"))
        assertEquals("baseball_bat", ResourceNameUtils.normalizeLabel("Baseball bat"))
        assertEquals("floor_tile", ResourceNameUtils.normalizeLabel("floor-tile"))
        assertEquals("eye_glasses", ResourceNameUtils.normalizeLabel("eye glasses"))
        assertEquals("building_other", ResourceNameUtils.normalizeLabel("building-other"))
    }

    @Test
    fun resource_and_filename_examples() {
        assertEquals("person_bemba", ResourceNameUtils.toResourceName("person"))
        assertEquals("person_bemba.m4a", ResourceNameUtils.toFilename("person"))
        assertEquals("baseball_bat_bemba.m4a", ResourceNameUtils.toFilename("Baseball bat"))
    }
}
