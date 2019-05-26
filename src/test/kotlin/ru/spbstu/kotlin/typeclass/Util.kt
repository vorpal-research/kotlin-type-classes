package ru.spbstu.kotlin.typeclass

private val executed: MutableSet<() -> Unit> = mutableSetOf()

fun once(body: () -> Unit) {
    if (body !in executed) body()
    executed += body
}