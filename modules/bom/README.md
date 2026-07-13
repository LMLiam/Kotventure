# `kotventure-bom`

Bill of materials aligning every Kotventure module — and the Adventure baseline they build against — to a single
version. Import it once as a `platform(...)`, then declare Kotventure (and Adventure) dependencies without version
numbers:

```kotlin
dependencies {
    implementation(platform("com.github.LMLiam.Kotventure:kotventure-bom:<tag>"))

    implementation("com.github.LMLiam.Kotventure:kotventure-core")
    implementation("com.github.LMLiam.Kotventure:kotventure-minimessage")

    testImplementation("com.github.LMLiam.Kotventure:kotventure-test")
}
```

Replace `<tag>` with a [released tag](https://github.com/LMLiam/Kotventure/releases). The BOM constrains all
`kotventure-*` artifacts to its own version and re-exports Adventure's BOM, so mixed Kotventure/Adventure version
skew cannot happen by accident.
