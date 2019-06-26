package ru.spbstu.kotlin.typeclass

import kotlin.reflect.KClass

private val executed: MutableSet<KClass<out () -> Unit>> = mutableSetOf()

fun once(body: () -> Unit) {
    if (body::class !in executed) body()
    executed += (body::class)
}

