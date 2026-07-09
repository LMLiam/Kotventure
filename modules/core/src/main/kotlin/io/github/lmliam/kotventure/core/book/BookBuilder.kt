package io.github.lmliam.kotventure.core.book

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.component.component
import io.github.lmliam.kotventure.core.component.emptyComponent
import io.github.lmliam.kotventure.core.dsl.once
import net.kyori.adventure.inventory.Book
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike

internal class BookBuilder : BookScope {
    private var title: Component? by once()
    private var author: Component? by once()
    private val pages = mutableListOf<Component>()

    override fun title(init: ComponentScope.() -> Unit): Unit = title(component(init))

    override fun <T : ComponentLike> title(component: T) {
        title = component.asComponent()
    }

    override fun author(init: ComponentScope.() -> Unit): Unit = author(component(init))

    override fun <T : ComponentLike> author(component: T) {
        author = component.asComponent()
    }

    override fun page(init: ComponentScope.() -> Unit): Unit = page(component(init))

    override fun <T : ComponentLike> page(component: T) {
        pages += component.asComponent()
    }

    override fun pages(vararg pages: ComponentLike) {
        pages.forEach { page(it) }
    }

    override fun pages(pages: Iterable<ComponentLike>) {
        pages.forEach { page(it) }
    }

    internal fun build(): Book =
        Book.book(
            title ?: emptyComponent(),
            author ?: emptyComponent(),
            pages.toList(),
        )
}
