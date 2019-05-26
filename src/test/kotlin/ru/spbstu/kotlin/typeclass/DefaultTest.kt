package ru.spbstu.kotlin.typeclass

import ru.spbstu.kotlin.typeclass.classes.Default
import ru.spbstu.kotlin.typeclass.classes.exportDefaults
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultTest {

    @BeforeTest
    fun init() {
        once { Default.exportDefaults() }
    }

    @Test
    fun foo() {

        run {
            val d: Default<List<Map<Int, Int>>> by TypeClasses
            assertEquals(d.default, listOf())
        }

        run {
            val d: Default<Triple<String, Int, Pair<Double, Double?>>> by TypeClasses
            assertEquals(d.default, Triple("", 0, Pair(0.0, null)))
        }

    }

}