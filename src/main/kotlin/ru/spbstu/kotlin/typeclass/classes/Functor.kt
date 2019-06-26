package ru.spbstu.kotlin.typeclass.classes

import ru.spbstu.kotlin.typeclass.*

interface Functor<HO: TCKind<HO, *>>: TCKind<Functor<*>, HO> {
    companion object {
        inline fun <reified HO: TCKind<HO, *>> getInstance(): Functor<HO> = TypeClasses.implicitly()
        inline fun <reified HO: TCKind<HO, *>, A, B> TCKind<HO, A>.fmap(noinline body: (A) -> B): TCKind<HO, B> =
                with(getInstance<HO>()) { fmap(body) }
    }

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
