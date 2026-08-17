package io.github.lmliam.kotventure.build

import java.io.InputStream

interface HttpConnection {
    void setFollowRedirects(boolean followRedirects)

    void setConnectTimeout(int timeoutMillis)

    void setReadTimeout(int timeoutMillis)

    int responseCode()

    String header(String name)

    InputStream inputStream()

    void disconnect()
}
