package ru.spbstu.kotlin.typeclass

import kotlin.reflect.*
import kotlin.reflect.full.createType
import kotlin.reflect.full.defaultType
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.jvm.reflect

inline fun <reified T> typeOf(noinline body: () -> T) = body.reflect()!!.returnType

fun KType.copy(classifier: KClassifier? = this.classifier,
               arguments: List<KTypeProjection> = this.arguments,
               isNullable: Boolean = this.isMarkedNullable,
               annotations: List<Annotation> = this.annotations) =
        when (classifier) {
            null -> throw IllegalArgumentException("Illegal classifier: $classifier")
            else -> classifier.createType(arguments, isNullable, annotations)
        }

val KType.supertypes: List<KType>
    get() = when (val classifier = classifier) {
        is KClass<*> -> {
            val defaulted = classifier.defaultType // yes, we need this deprecated guy here
            val mapping = (defaulted.arguments.map { it.type } zip this.arguments.map { it.type }).toMap()

            classifier.supertypes.map {
                it.copy(arguments = it.arguments.map { it.copy(type = mapping[it.type] ?: it.type) })
            }
        }
        is KTypeParameter -> classifier.upperBounds
        else -> throw IllegalArgumentException("Unknown classifier: $classifier")
    }

object TypeBuilder {
    operator fun Type.unaryPlus() = KTypeProjection.covariant(this)
    operator fun Type.unaryMinus() = KTypeProjection.contravariant(this)

    operator fun <T: Any> KClass<T>.invoke(vararg arguments: Type) =
            createType(arguments.map { KTypeProjection.invariant(it) })

    operator fun <T: Any> KClass<T>.invoke(vararg arguments: KTypeProjection) =
            createType(arguments.asList())

    operator fun <T: Any> KClass<T>.invoke() = starProjectedType

}
