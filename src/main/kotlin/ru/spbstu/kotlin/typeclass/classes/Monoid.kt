package ru.spbstu.kotlin.typeclass.classes

import ru.spbstu.kotlin.typeclass.TCKind
import ru.spbstu.kotlin.typeclass.TypeClasses

interface Monoid<T> : TCKind<Monoid<*>, @kotlin.UnsafeVariance T> {
    companion object {
        inline fun <reified T> getInstance(): Monoid<T> = TypeClasses.implicitly()

        inline fun <reified T> getEmpty(): T = getInstance<T>().empty

        inline operator fun <reified T> T.plus(rhv: T): T = let { lhv ->
            with(getInstance<T>()) { lhv + rhv }
        }

        inline fun <reified T> concat(vararg values: T) = with(getInstance<T>()) {
            var res = empty
            for(v in values) {
                res += v
            }
            res
        }

    }

    val empty: T
    operator fun T.plus(that: T): T
}

inline fun <T> Monoid<T>.plus(a: T, b: T) = a + b

private inline fun <T> Monoid(empty: T, crossinline plus: (T, T) -> T) = object: Monoid<T> {
    override val empty: T = empty
    override fun T.plus(that: T): T = plus(this, that)
}

fun defaultMonoids() {
    with(TypeClasses) {
        val monoid = Monoid::class

        instance {-> Monoid(0, Int::plus) }
        instance {-> Monoid(0L, Long::plus) }
        instance {-> Monoid("", String::plus) }
        instance {-> Monoid(emptyList<Any?>()) { a, b -> a + b } }
        instance {-> Monoid(emptySet<Any?>()) { a, b -> a + b } }

        instance { (x, y) ->
            val mx = monoid[x] as Monoid<Any?>
            val my = monoid[y] as Monoid<Any?>
            Monoid(Pair(mx.empty, my.empty)) { a, b ->
                Pair(mx.plus(a.first, b.first), my.plus(a.second, b.second))
            }
        }

        instance { (x, y, z) ->
            val mx = monoid[x] as Monoid<Any?>
            val my = monoid[y] as Monoid<Any?>
            val mz = monoid[z] as Monoid<Any?>
            Monoid(Triple(mx.empty, my.empty, mz.empty)) { a, b ->
                Triple(mx.plus(a.first, b.first), my.plus(a.second, b.second), mz.plus(a.third, b.third))
            }
        }

    }
}



fun main() {
    defaultMonoids()

    TypeClasses.provide { monoid: Monoid<Pair<Int, String>> ->
        monoid.run {
            println((0 to "Hello") + (2 to "World"))
        }
    }

    with(Monoid) {
        println((0 to "Hello") + (2 to "World"))
    }
}
