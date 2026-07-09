package io.github.lmliam.kotventure.core.audience

import io.github.lmliam.kotventure.core.key.key
import io.github.lmliam.kotventure.core.sound.sound
import io.github.lmliam.kotventure.test.sound.shouldHaveName
import io.github.lmliam.kotventure.test.sound.shouldHavePitch
import io.github.lmliam.kotventure.test.sound.shouldHaveVolume
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.sound.SoundStop

/**
 * Captures [playSound] and [stopSound] calls. Kept local to the test for isolation and readability.
 */
private class SoundRecordingAudience : Audience {
    data class Played(
        val sound: Sound,
        val emitter: Sound.Emitter? = null,
        val x: Double? = null,
        val y: Double? = null,
        val z: Double? = null,
    )

    val played = mutableListOf<Played>()
    val stopped = mutableListOf<SoundStop>()

    override fun playSound(sound: Sound) {
        played += Played(sound)
    }

    override fun playSound(
        sound: Sound,
        x: Double,
        y: Double,
        z: Double,
    ) {
        played += Played(sound, x = x, y = y, z = z)
    }

    override fun playSound(
        sound: Sound,
        emitter: Sound.Emitter,
    ) {
        played += Played(sound, emitter = emitter)
    }

    override fun stopSound(stop: SoundStop) {
        stopped += stop
    }
}

class SoundDslTest :
    StringSpec(
        {
            "builds and plays a plain one-shot sound at the recipient" {
                val audience = SoundRecordingAudience()

                val returned =
                    audience.sound(key("minecraft:ui.button.click")) {
                        volume(2f)
                        pitch(0.5f)
                    }

                audience.played shouldHaveSize 1
                val received = audience.played.single()
                received.emitter shouldBe null
                received.x shouldBe null
                received.sound shouldBe returned
                returned shouldHaveName Key.key("minecraft:ui.button.click")
                returned shouldHaveVolume 2f
                returned shouldHavePitch 0.5f
                returned shouldBe
                    Sound.sound(
                        Key.key("minecraft:ui.button.click"),
                        Sound.Source.MASTER,
                        2f,
                        0.5f,
                    )
            }

            "plays a one-shot sound with emitter(self)" {
                val audience = SoundRecordingAudience()

                audience.sound(key("minecraft:entity.pig.ambient")) {
                    emitter(self)
                }

                audience.played shouldHaveSize 1
                val received = audience.played.single()
                received.emitter shouldBe Sound.Emitter.self()
                received.x shouldBe null
                received.sound shouldHaveName Key.key("minecraft:entity.pig.ambient")
            }

            "plays a one-shot sound at world position" {
                val audience = SoundRecordingAudience()

                audience.sound(key("minecraft:block.bell.use")) {
                    at(100.0, 64.0, 200.0)
                }

                audience.played shouldHaveSize 1
                val received = audience.played.single()
                received.emitter shouldBe null
                received.x shouldBe 100.0
                received.y shouldBe 64.0
                received.z shouldBe 200.0
                received.sound shouldHaveName Key.key("minecraft:block.bell.use")
            }

            "play forwards a prebuilt sound to playSound(Sound)" {
                val audience = SoundRecordingAudience()
                val alert =
                    sound(key("minecraft:block.bell.use")) {
                        source(music)
                    }

                audience.play(alert)

                audience.played shouldHaveSize 1
                audience.played.single() shouldBe SoundRecordingAudience.Played(alert)
            }

            "play forwards a prebuilt sound with an emitter" {
                val audience = SoundRecordingAudience()
                val alert = sound(key("minecraft:block.bell.use"))
                val emitter = Sound.Emitter.self()

                audience.play(alert, emitter)

                audience.played shouldHaveSize 1
                audience.played.single() shouldBe
                    SoundRecordingAudience.Played(alert, emitter = emitter)
            }

            "play forwards a prebuilt sound at world position" {
                val audience = SoundRecordingAudience()
                val alert = sound(key("minecraft:block.bell.use"))

                audience.play(alert, 100.0, 64.0, 200.0)

                audience.played shouldHaveSize 1
                audience.played.single() shouldBe
                    SoundRecordingAudience.Played(alert, x = 100.0, y = 64.0, z = 200.0)
            }

            "forwards sound build-and-play to every member of a composite audience" {
                val first = SoundRecordingAudience()
                val second = SoundRecordingAudience()

                audienceOf(first, second).sound(key("minecraft:ui.button.click"))

                first.played shouldHaveSize 1
                second.played shouldHaveSize 1
                first.played.single().sound shouldBe second.played.single().sound
            }

            "stopSound named produces SoundStop.named" {
                val audience = SoundRecordingAudience()
                val name = key("minecraft:music.game")

                audience.stopSound { named(name) }

                audience.stopped shouldHaveSize 1
                audience.stopped.single() shouldBe SoundStop.named(Key.key("minecraft:music.game"))
            }

            "stopSound named + source produces SoundStop.namedOnSource" {
                val audience = SoundRecordingAudience()
                val name = key("minecraft:music.game")

                audience.stopSound {
                    named(name)
                    source(music)
                }

                audience.stopped shouldHaveSize 1
                audience.stopped.single() shouldBe
                    SoundStop.namedOnSource(Key.key("minecraft:music.game"), Sound.Source.MUSIC)
            }

            "stopSound source produces SoundStop.source" {
                val audience = SoundRecordingAudience()

                audience.stopSound { source(music) }

                audience.stopped shouldHaveSize 1
                audience.stopped.single() shouldBe SoundStop.source(Sound.Source.MUSIC)
            }

            "stopSound all produces SoundStop.all" {
                val audience = SoundRecordingAudience()

                audience.stopSound { all() }

                audience.stopped shouldHaveSize 1
                audience.stopped.single() shouldBe SoundStop.all()
            }

            listOf(
                "rejects emitter twice" to {
                    SoundRecordingAudience().sound(key("minecraft:ui.button.click")) {
                        emitter(self)
                        emitter(self)
                    }
                },
                "rejects at twice" to {
                    SoundRecordingAudience().sound(key("minecraft:ui.button.click")) {
                        at(1.0, 2.0, 3.0)
                        at(4.0, 5.0, 6.0)
                    }
                },
                "rejects emitter then at" to {
                    SoundRecordingAudience().sound(key("minecraft:ui.button.click")) {
                        emitter(self)
                        at(1.0, 2.0, 3.0)
                    }
                },
                "rejects at then emitter" to {
                    SoundRecordingAudience().sound(key("minecraft:ui.button.click")) {
                        at(1.0, 2.0, 3.0)
                        emitter(self)
                    }
                },
                "rejects all twice" to {
                    SoundRecordingAudience().stopSound {
                        all()
                        all()
                    }
                },
                "rejects named twice" to {
                    SoundRecordingAudience().stopSound {
                        named(key("minecraft:music.game"))
                        named(key("minecraft:music.creative"))
                    }
                },
                "rejects source twice" to {
                    SoundRecordingAudience().stopSound {
                        source(music)
                        source(ui)
                    }
                },
                "rejects all then named" to {
                    SoundRecordingAudience().stopSound {
                        all()
                        named(key("minecraft:music.game"))
                    }
                },
                "rejects named then all" to {
                    SoundRecordingAudience().stopSound {
                        named(key("minecraft:music.game"))
                        all()
                    }
                },
                "rejects all then source" to {
                    SoundRecordingAudience().stopSound {
                        all()
                        source(music)
                    }
                },
                "rejects an empty stopSound block" to {
                    SoundRecordingAudience().stopSound { }
                },
            ).forEach { (name, action) ->
                name {
                    shouldThrow<IllegalStateException> { action() }
                }
            }
        },
    )
