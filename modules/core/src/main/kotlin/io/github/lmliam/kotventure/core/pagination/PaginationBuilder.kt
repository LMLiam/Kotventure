package io.github.lmliam.kotventure.core.pagination

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.component.component
import io.github.lmliam.kotventure.core.dsl.once
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike

private const val DEFAULT_ITEMS_PER_PAGE = 6

internal class PaginationBuilder<T> : PaginationScope<T> {
    private var header: Component? by once()
    private var renderer: ((item: T) -> ComponentLike)? by once()
    private var itemsPerPage: Int? by once()
    private var nav: NavSettings? by once()

    override fun header(init: ComponentScope.() -> Unit) {
        header = component(init)
    }

    override fun <C : ComponentLike> header(component: C) {
        header = component.asComponent()
    }

    override fun renderer(render: (item: T) -> ComponentLike) {
        renderer = render
    }

    override fun itemsPerPage(count: Int) {
        require(count > 0) { "'itemsPerPage' must be positive, was $count." }
        itemsPerPage = count
    }

    override fun nav(init: NavScope.() -> Unit) {
        nav = NavBuilder().apply(init).build()
    }

    internal fun build(items: Iterable<T>): Pagination {
        val render = checkNotNull(renderer) { "'renderer' must be set." }
        return Pagination(
                items = items.map { item -> render(item).asComponent() },
                header = header,
                itemsPerPage = itemsPerPage ?: DEFAULT_ITEMS_PER_PAGE,
                nav = nav ?: NavBuilder().build(),
        )
    }
}
