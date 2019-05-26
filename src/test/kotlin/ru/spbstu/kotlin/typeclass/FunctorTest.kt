package ru.spbstu.kotlin.typeclass

import ru.spbstu.kotlin.typeclass.classes.Functor
import ru.spbstu.kotlin.typeclass.classes.exportDefaults
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class FunctorTest {
    @BeforeTest
    fun init() {
        once { Functor.exportDefaults() }
    }

    @Test
    fun smokey() {
        TypeClasses.provide { it: Functor<ListKind<*>> -> it.run {
            assertEquals(listOf(4, 8, 12), listOf(1,2,3).kind.fmap { it * 4 }.list)
        } }

        TypeClasses.provide { it: Functor<NullableKind<*>> -> it.run {
            assertEquals(8, 2.kind.fmap { it * 4 }.value)
            assertEquals(null, (null as String?).kind.fmap { it + "World" }.value)
        } }
    }
}