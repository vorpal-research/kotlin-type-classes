package ru.spbstu.kotlin.typeclass.classes

import ru.spbstu.kotlin.typeclass.TCKind
import ru.spbstu.kotlin.typeclass.TypeClasses

interface Default<out T> : TCKind<Default<*>, @kotlin.UnsafeVariance T> {
    companion object
    val default: T
}

fun <T> Default(value: T) = object : Default<T> {
    override val default: T = value
}

fun Default.Companion.exportDefaults() {
    with(TypeClasses) {
        instance { -> Default(0) }
        instance { -> Default(0.0) }
        instance { -> Default(0L) }
        instance { -> Default(0.0F) }
        instance { -> Default('\u0000') }
        instance { -> Default("") }
        instance { -> Default(listOf<Any>()) }
        instance { -> Default(setOf<Any>()) }
        instance { -> Default(mapOf<Any, Any>()) }
        instance { -> Default(null as Any?) }

        val df = Default::class

        instance { (a, b) -> Default(df[a].default to df[b].default) }
        instance { (a, b, c) -> Default(Triple(df[a].default, df[b].default, df[c].default)) }
    }
}
