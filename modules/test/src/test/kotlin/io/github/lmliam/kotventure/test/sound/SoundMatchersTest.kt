package io.github.lmliam.kotventure.test.sound

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.string.shouldContain
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound

class SoundMatchersTest :
    StringSpec(
        {
            val caveKey = Key.key("minecraft:ambient.cave")
            val cave =
                Sound.sound(
                    caveKey,
                    Sound.Source.AMBIENT,
                    0.5f,
                    2f,
                )
            val caveWithSeed =
                Sound
                    .sound()
                    .type(caveKey)
                    .source(Sound.Source.AMBIENT)
                    .volume(0.5f)
                    .pitch(2f)
                    .seed(42L)
                    .build()

            "matches name, source, volume, and pitch" {
                cave shouldHaveName caveKey
                cave shouldHaveSource Sound.Source.AMBIENT
                cave shouldHaveVolume 0.5f
                cave shouldHavePitch 2f
            }

            "matches the absence of a given name" {
                cave shouldNotHaveName Key.key("minecraft:entity.pig.ambient")
            }

            "matches the absence of a given source" {
                cave shouldNotHaveSource Sound.Source.MASTER
            }

            "matches the absence of a given volume" {
                cave shouldNotHaveVolume 1f
            }

            "matches the absence of a given pitch" {
                cave shouldNotHavePitch 1f
            }

            "matches a present seed value" {
                caveWithSeed shouldHaveSeed 42L
            }

            "matches the absence of a given seed value" {
                caveWithSeed shouldNotHaveSeed 99L
            }

            "matches an empty seed" {
                cave.shouldHaveNoSeed()
            }

            "reports a name mismatch with expected and actual values" {
                val failure =
                    shouldThrow<AssertionError> {
                        cave shouldHaveName Key.key("minecraft:entity.pig.ambient")
                    }

                failure.message shouldContain
                        "Expected sound name <minecraft:entity.pig.ambient>, but was <minecraft:ambient.cave>."
            }

            "reports a source mismatch with expected and actual values" {
                val failure =
                    shouldThrow<AssertionError> {
                        cave shouldHaveSource Sound.Source.MASTER
                    }

                failure.message shouldContain
                        "Expected sound source <MASTER>, but was <AMBIENT>."
            }

            "reports a volume mismatch with expected and actual values" {
                val failure =
                    shouldThrow<AssertionError> {
                        cave shouldHaveVolume 1f
                    }

                failure.message shouldContain
                        "Expected sound volume <1.0>, but was <0.5>."
            }

            "reports a pitch mismatch with expected and actual values" {
                val failure =
                    shouldThrow<AssertionError> {
                        cave shouldHavePitch 1f
                    }

                failure.message shouldContain
                        "Expected sound pitch <1.0>, but was <2.0>."
            }

            "reports a seed mismatch with expected and actual values" {
                val failure =
                    shouldThrow<AssertionError> {
                        caveWithSeed shouldHaveSeed 99L
                    }

                failure.message shouldContain
                        "Expected sound seed <99>, but was <OptionalLong[42]>."
            }

            "reports when seed unexpectedly matches" {
                val failure =
                    shouldThrow<AssertionError> {
                        caveWithSeed shouldNotHaveSeed 42L
                    }

                failure.message shouldContain
                        "Expected sound seed not to be <42>."
            }

            "reports seed present when expecting no seed" {
                val failure =
                    shouldThrow<AssertionError> {
                        caveWithSeed.shouldHaveNoSeed()
                    }

                failure.message shouldContain
                        "Expected sound to have no seed, but was <OptionalLong[42]>."
            }

            "reports when name unexpectedly matches" {
                val failure =
                    shouldThrow<AssertionError> {
                        cave shouldNotHaveName caveKey
                    }

                failure.message shouldContain
                        "Expected sound name not to be <minecraft:ambient.cave>."
            }
        },
    )
