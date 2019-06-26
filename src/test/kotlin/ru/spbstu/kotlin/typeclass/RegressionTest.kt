package ru.spbstu.kotlin.typeclass

import org.junit.Test
import ru.spbstu.kotlin.typeclass.classes.Default
import ru.spbstu.kotlin.typeclass.classes.exportDefaults
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@Target(AnnotationTarget.TYPE)
annotation class TestAnnotation

class RegressionTest {
    @BeforeTest
    fun init() {
        Default.exportDefaults()
    }

    @Test
    fun cachingBug() {

        /*
        * There was a bug where a generic type did not account for annotations
        * So that types having annotations on type parameters
        * */
        val d1: Default<Pair<@TestAnnotation Int, @TestAnnotation Double>> by TypeClasses
        val d2: Default<Pair<Int, Double>> by TypeClasses

        assertEquals(d1, d1)
        assertNotEquals(d2, d1)
        assertEquals(d2.default, d1.default)

        val d3: Default<Pair<@TestAnnotation Int, @TestAnnotation Double>> by TypeClasses
        assertEquals(d1, d3)
    }

    @Test
    fun cachingBugWithImpliticly() {
        val d1: Default<Pair<@TestAnnotation Int, @TestAnnotation Double>> = TypeClasses.implicitly()
        val d2: Default<Pair<Int, Double>> = TypeClasses.implicitly()

        assertEquals(d1, d1)
        /*
        * This unfortunately does not work yet, 'cause implicitly is based on typeOf<T>(), which does not support annotations yet
        * */
        // assertNotEquals(d2, d1) // =(
        assertEquals(d2.default, d1.default)

        val d3: Default<Pair<@TestAnnotation Int, @TestAnnotation Double>> = TypeClasses.implicitly()
        assertEquals(d1, d3)
    }
}
