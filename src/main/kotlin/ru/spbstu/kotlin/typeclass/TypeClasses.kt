package ru.spbstu.kotlin.typeclass

import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.full.withNullability
import kotlin.reflect.jvm.reflect

interface TCKind<C : TCKind<C, *>, T>
typealias Type = KType

@Suppress("UNCHECKED_CAST")
class ClassMap<C : TCKind<C, *>>(private val data: MutableMap<KClass<*>, C> = mutableMapOf()) {
    operator fun <T : Any> get(klass: KClass<T>): TCKind<C, T>? =
            data[klass] as? TCKind<C, T>

    operator fun <T : Any> set(klass: KClass<T>, kind: TCKind<C, T>) {
        data[klass] = kind as C
    }

    inline fun <T : Any> getOrPut(klass: KClass<T>, body: () -> TCKind<C, T>): TCKind<C, T> = run {
        when (val existing = get(klass)) {
            null -> body().also { set(klass, it) }
            else -> existing
        }
    }
}

interface TClassProvider<TC : TCKind<TC, *>, T> : TCKind<TClassProvider<TC, *>, T> {
    operator fun invoke(genericArguments: List<Type>, annotations: List<Annotation>): TCKind<TC, T>
}

inline fun <TC : TCKind<TC, *>, T> TClassProvider(
        crossinline body: (genericArguments: List<Type>, annotations: List<Annotation>) -> TCKind<TC, T>
) = object : TClassProvider<TC, T> {
    override fun invoke(genericArguments: List<Type>, annotations: List<Annotation>): TCKind<TC, T> =
            body(genericArguments, annotations)
}

inline val <TC : TCKind<TC, *>, T> TCKind<TClassProvider<TC, *>, T>.instance get() = this as TClassProvider<TC, T>

@PublishedApi
internal class TCRegistry<C : TCKind<C, *>> : TCKind<TCRegistry<*>, C> {
    val data = ClassMap<TClassProvider<C, *>>()

    operator fun <T : Any> get(klass: KClass<T>): TClassProvider<C, T>? = data[klass]?.instance

    operator fun <T : Any> set(klass: KClass<T>, kind: TClassProvider<C, T>) {
        data[klass] = kind
    }

    @PublishedApi
    @Suppress("UNCHECKED_CAST")
    internal fun <T> setUnsafe(klass: KClass<*>, kind: TClassProvider<C, T>) {
        data[klass as KClass<Any>] = kind as TClassProvider<C, Any>
    }

    inline fun <reified C : TCKind<C, *>> get() = get(C::class)!!
}

@Suppress("UNCHECKED_CAST")
internal inline val <TC : TCKind<TCRegistry<*>, T>, T : TCKind<T, *>> TC.instance: TCRegistry<T>
    get() = this as TCRegistry<T>

object TypeClasses {
    private val data = ClassMap<TCRegistry<*>>()

    @PublishedApi
    internal object Nullable

    @PublishedApi
    internal operator fun <TC : TCKind<TC, *>> get(klass: KClass<TC>): TCRegistry<TC> =
            data.getOrPut(klass) { TCRegistry() }.instance

    @PublishedApi
    internal inline fun <reified TC : TCKind<TC, *>, reified T : Any> get() = get(TC::class).get(T::class)

    inline fun <reified TC : TCKind<TC, *>, reified T : Any> instance(provider: TClassProvider<TC, T>) {
        this[TC::class][T::class] = provider
    }

    inline fun <reified TC : TCKind<TC, *>, reified T> instance(
            crossinline body: (genericArguments: List<Type>, annotations: List<Annotation>) -> TCKind<TC, T>
    ) {
        val bd = TClassProvider(body)
        @Suppress("UNCHECKED_CAST")
        if (null is T) instance(bd as TClassProvider<TC, Any?>)
        else this[TC::class].setUnsafe(T::class as KClass<*>, bd)
    }

    inline fun <reified TC : TCKind<TC, *>, reified T> instance(
            crossinline body: (genericArguments: List<Type>) -> TCKind<TC, T>
    ) = instance { types, _ -> body(types) }

    inline fun <reified TC : TCKind<TC, *>, reified T> instance(crossinline body: () -> TCKind<TC, T>) =
            instance { _, _ -> body() }


    @JvmName("setNullable")
    inline fun <reified TC : TCKind<TC, *>> instance(provider: TClassProvider<TC, Any?>) {
        this[TC::class][Nullable::class] = TClassProvider { args, anno ->
            @Suppress("UNCHECKED_CAST")
            provider(args, anno) as TCKind<TC, Nullable>
        }
    }

    // TODO: make this a proper cache
    @PublishedApi
    internal val cache = mutableMapOf<KClass<*>, MutableMap<Type, Any?>>()

    inline fun <reified TC : TCKind<TC, *>> get(type: Type): TC =
            cache.getOrPut(TC::class, { mutableMapOf() }).getOrPut(type) {
                when {
                    type.isMarkedNullable ->
                        get(TC::class)[Nullable::class]
                                ?.invoke(listOf(type.withNullability(false)), type.annotations)
                    else ->
                        get(TC::class)[type.classifier as KClass<*>]
                                ?.invoke(type.arguments.map { it.type!! }, type.annotations)
                }
            } as? TC ?: throw IllegalArgumentException("No ${TC::class.qualifiedName} instance found for type $type")

    fun deduceElementType(tcType: Type) = tcType.arguments.last().type!!

    inline operator fun <reified TC : TCKind<C, E>, reified C : TCKind<C, *>, E> getValue(thisRef: Any?, prop: KProperty<*>) =
            get<C>(deduceElementType(prop.returnType)) as TC

    inline fun <reified TC : TCKind<C, E>, reified C : TCKind<C, *>, E, R> provide(noinline body: (TC) -> R) =
            body(get<C>(deduceElementType(body.reflect()!!.parameters.first().type)) as TC)
}
