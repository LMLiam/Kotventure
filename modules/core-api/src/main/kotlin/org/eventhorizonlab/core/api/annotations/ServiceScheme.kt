package org.eventhorizonlab.core.api.annotations

import kotlin.reflect.KClass

/**
 * Marks a class as a ServiceLoader provider for the given contract
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ServiceProvider(
    val value: KClass<*>
)

/**
 * Marks an interface as a ServiceLoader contract
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
internal annotation class ServiceContract