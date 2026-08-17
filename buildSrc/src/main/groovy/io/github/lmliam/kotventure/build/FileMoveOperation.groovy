package io.github.lmliam.kotventure.build

import java.nio.file.CopyOption
import java.nio.file.Path

interface FileMoveOperation {
    Path move(Path source, Path target, CopyOption... options)
}
