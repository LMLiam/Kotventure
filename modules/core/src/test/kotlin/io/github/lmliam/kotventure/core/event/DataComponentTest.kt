package io.github.lmliam.kotventure.core.event

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.types.shouldBeInstanceOf
import net.kyori.adventure.text.event.DataComponentValue

class DataComponentTest :
    StringSpec(
        {
            "removed returns the correct marker type" {
                removed().shouldBeInstanceOf<DataComponentValue.Removed>()
            }
        },
    )
