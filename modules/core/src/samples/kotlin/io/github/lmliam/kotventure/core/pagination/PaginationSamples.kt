package io.github.lmliam.kotventure.core.pagination

import io.github.lmliam.kotventure.core.audience.emptyAudience
import io.github.lmliam.kotventure.core.audience.paginate
import io.github.lmliam.kotventure.core.color.gold
import io.github.lmliam.kotventure.core.text.text

internal fun paginateSample() {
    val warps = listOf("hub", "arena", "shop")

    val pagination =
        paginate(warps) {
            header { text("Warps") { color(gold) } }
            renderer { warp -> text(warp) }
            itemsPerPage(8)
        }

    emptyAudience().sendMessage(pagination.page(1))
}

internal fun audiencePaginateSample() {
    val audience = emptyAudience()

    audience.paginate(listOf("hub", "arena", "shop")) {
        header { text("Warps") }
        renderer { warp -> text(warp) }
        nav {
            previous { text("« Back") }
            next { text("More »") }
            indicator { page, pageCount -> text("page $page of $pageCount") }
        }
    }
}
