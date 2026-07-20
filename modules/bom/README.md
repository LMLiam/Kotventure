# `kotventure-bom`

`kotventure-bom` is a bill of materials. It aligns all Kotventure modules and the Adventure baseline to one version.
Import it one time as a `platform(...)`. Then, declare Kotventure and Adventure dependencies without version numbers:

```kotlin
dependencies {
    implementation(platform("com.github.LMLiam.Kotventure:kotventure-bom:<tag>"))

    implementation("com.github.LMLiam.Kotventure:kotventure-core")
    implementation("com.github.LMLiam.Kotventure:kotventure-minimessage")

    testImplementation("com.github.LMLiam.Kotventure:kotventure-test")
}
```

Replace `<tag>` with a [released tag](https://github.com/LMLiam/Kotventure/releases). The BOM sets its version for all
`kotventure-*` artefacts. It also exports the Adventure BOM. This prevents unplanned version differences.
