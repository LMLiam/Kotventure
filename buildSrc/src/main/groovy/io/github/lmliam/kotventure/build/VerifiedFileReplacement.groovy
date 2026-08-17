package io.github.lmliam.kotventure.build

import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.CopyOption
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

final class VerifiedFileReplacement {

    private static final FileMoveOperation FILES_MOVE = new FileMoveOperation() {
        @Override
        Path move(Path source, Path target, CopyOption... options) {
            Files.move(source, target, options)
        }
    }

    private VerifiedFileReplacement() {
    }

    static void replace(Path temporary, Path destination) {
        replace(temporary, destination, FILES_MOVE)
    }

    static void replace(Path temporary, Path destination, FileMoveOperation moveOperation) {
        try {
            moveOperation.move(
                temporary,
                destination,
                StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING,
            )
        } catch (AtomicMoveNotSupportedException ignored) {
            moveOperation.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING)
        }
    }
}
