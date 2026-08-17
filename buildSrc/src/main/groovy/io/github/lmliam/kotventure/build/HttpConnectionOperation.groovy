package io.github.lmliam.kotventure.build

import java.net.URI

interface HttpConnectionOperation {
    HttpConnection open(URI uri)
}
