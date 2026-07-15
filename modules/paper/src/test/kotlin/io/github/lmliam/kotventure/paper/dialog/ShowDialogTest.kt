package io.github.lmliam.kotventure.paper.dialog

import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.paper.dialog.fixture.FakeDialog
import io.github.lmliam.kotventure.test.text.shouldHaveContent
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.dialog.DialogLike

class ShowDialogTest :
    StringSpec(
        {
            "builds and shows the dialog through the audience" {
                val shown = slot<DialogLike>()
                val audience = mockk<Audience>()
                every { audience.showDialog(capture(shown)) } just Runs

                audience.dialog(notice) {
                    title { text("Welcome") }
                }

                verify { audience.showDialog(any()) }
                shown.captured
                    .shouldBeInstanceOf<FakeDialog>()
                    .base
                    .title() shouldHaveContent "Welcome"
            }
        },
    )
