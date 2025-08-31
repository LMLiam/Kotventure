package com.github.eventhorizonlab.kyoriadventuredsl.api

import com.github.eventhorizonlab.core.api.TextComponentScope
import com.github.eventhorizonlab.core.api.textComponent
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.instanceOf
import net.kyori.adventure.text.format.NamedTextColor
import java.util.*

class ServiceLoaderE2ETest :
    StringSpec({
        "ServiceLoader should discover and use TextComponentScope.Factory" {
            val loader =
                ServiceLoader.load(
                    TextComponentScope.Factory::class.java,
                    Thread.currentThread().contextClassLoader
                )
            val factories = loader.toList()

            factories.size shouldBe 1

            val factory = factories.first()

            factory shouldBe instanceOf<TextComponentScope.Factory>()
            factory.javaClass.classLoader shouldBe TextComponentScope::class.java.classLoader
            factory.javaClass.isInterface shouldBe false

            val factoryComponent =
                factory.create {
                    content("Hello E2E")
                    color(NamedTextColor.RED)
                }

            factoryComponent.content() shouldBe "Hello E2E"
            factoryComponent.color() shouldBe NamedTextColor.RED

            val component =
                textComponent {
                    content("Hello E2E")
                    color(NamedTextColor.RED)
                }

            component.content() shouldBe "Hello E2E"
            component.color() shouldBe NamedTextColor.RED
        }
    })