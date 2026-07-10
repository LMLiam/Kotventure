package io.github.lmliam.kotventure.core.sound

import io.github.lmliam.kotventure.core.key.key
import io.github.lmliam.kotventure.test.sound.shouldHaveName
import io.github.lmliam.kotventure.test.sound.shouldHaveNoSeed
import io.github.lmliam.kotventure.test.sound.shouldHavePitch
import io.github.lmliam.kotventure.test.sound.shouldHaveSeed
import io.github.lmliam.kotventure.test.sound.shouldHaveSource
import io.github.lmliam.kotventure.test.sound.shouldHaveVolume
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound

class SoundDslTest :
    StringSpec(
        {
            "defaults produce MASTER source, volume 1, pitch 1, and no seed" {
                val cave = sound(key("minecraft:ambient.cave"))

                cave shouldHaveName Key.key("minecraft:ambient.cave")
                cave shouldHaveSource Sound.Source.MASTER
                cave shouldHaveVolume 1f
                cave shouldHavePitch 1f
                cave.shouldHaveNoSeed()
            }

            "defaults match the raw Adventure factory" {
                sound(key("minecraft:ambient.cave")) shouldBe
                        Sound.sound(
                            Key.key("minecraft:ambient.cave"),
                            Sound.Source.MASTER,
                            1f,
                            1f,
                        )
            }

            "builds a fully configured sound" {
                val result =
                    sound(key("minecraft:entity.experience_orb.pickup")) {
                        source(music)
                        volume(2f)
                        pitch(0.5f)
                        seed(42L)
                    }

                result shouldHaveName Key.key("minecraft:entity.experience_orb.pickup")
                result shouldHaveSource Sound.Source.MUSIC
                result shouldHaveVolume 2f
                result shouldHavePitch 0.5f
                result shouldHaveSeed 42L
            }

            "matches Adventure builder ground truth when every slot is set" {
                val name = Key.key("minecraft:entity.experience_orb.pickup")

                val fromDsl =
                    sound(key("minecraft:entity.experience_orb.pickup")) {
                        source(music)
                        volume(2f)
                        pitch(0.5f)
                        seed(42L)
                    }

                val fromAdventure =
                    Sound.sound {
                        it.type(name)
                        it.source(Sound.Source.MUSIC)
                        it.volume(2f)
                        it.pitch(0.5f)
                        it.seed(42L)
                    }

                fromDsl shouldBe fromAdventure
            }

            "scope-bound source vals match Adventure enums" {
                val result =
                    sound(key("minecraft:ui.button.click")) {
                        source(ui)
                    }

                result shouldHaveSource Sound.Source.UI
            }

            listOf(
                "rejects a duplicate source" to {
                    sound(key("minecraft:ambient.cave")) {
                        source(master)
                        source(music)
                    }
                },
                "rejects a duplicate volume" to {
                    sound(key("minecraft:ambient.cave")) {
                        volume(1f)
                        volume(2f)
                    }
                },
                "rejects a duplicate pitch" to {
                    sound(key("minecraft:ambient.cave")) {
                        pitch(0.5f)
                        pitch(1f)
                    }
                },
                "rejects a duplicate seed" to {
                    sound(key("minecraft:ambient.cave")) {
                        seed(1L)
                        seed(2L)
                    }
                },
            ).forEach { (name, action) ->
                name {
                    shouldThrow<IllegalStateException> { action() }
                }
            }
        },
    )
