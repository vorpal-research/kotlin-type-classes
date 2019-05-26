package ru.spbstu.kotlin.typeclass

interface TCKind2<C: TCKind2<C, *, *>, A, B> : TCKind<TCKind2<C, A, *>, B>

inline class ListKind<T>(val list: List<T>): TCKind<ListKind<*>, T>
val <T> TCKind<ListKind<*>, T>.list
    get() = (this as ListKind<T>).list
val <T> List<T>.kind: TCKind<ListKind<*>, T> get() = ListKind(this)

inline class SetKind<T>(val set: Set<T>): TCKind<SetKind<*>, T>
val <T> TCKind<SetKind<*>, T>.set
    get() = (this as SetKind<T>).set
val <T> Set<T>.kind: TCKind<SetKind<*>, T> get() = SetKind(this)

inline class MapKind<K, V>(val map: Map<K, V>): TCKind2<MapKind<*, *>, K, V>
val <K, V> TCKind2<MapKind<*, *>, K, V>.map
    get() = (this as MapKind<K, V>).map
val <K, V> Map<K, V>.kind: TCKind2<MapKind<*, *>, K, V> get() = MapKind(this)

class NullableKind<T>(val data: T?): TCKind<NullableKind<*>, T>
val <T> TCKind<NullableKind<*>, T>.value get() = (this as NullableKind<T>).data
val <T> T?.kind: TCKind<NullableKind<*>, T> get() = NullableKind(this)

