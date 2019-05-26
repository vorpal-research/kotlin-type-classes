package ru.spbstu.kotlin.typeclass.classes

import ru.spbstu.kotlin.typeclass.*

interface Functor<HO: TCKind<HO, *>>: TCKind<Functor<*>, HO> {
    companion object

    fun <A, B> TCKind<HO, A>.fmap(body: (A) -> B): TCKind<HO, B>
}

object ListFunctor : Functor<ListKind<*>> {
    override fun <A, B> TCKind<ListKind<*>, A>.fmap(body: (A) -> B): TCKind<ListKind<*>, B> =
            list.map(body).kind
}

object NullableFunctor : Functor<NullableKind<*>> {
    override fun <A, B> TCKind<NullableKind<*>, A>.fmap(body: (A) -> B): TCKind<NullableKind<*>, B> =
            value?.let(body).kind
}

fun Functor.Companion.exportDefaults() {
    with(TypeClasses) {
        instance { -> ListFunctor }
        instance { -> NullableFunctor }
    }
}
