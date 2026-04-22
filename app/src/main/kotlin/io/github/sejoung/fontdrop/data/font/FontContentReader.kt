package io.github.sejoung.fontdrop.data.font

import java.io.InputStream

interface FontContentReader {
    fun openInputStream(uriString: String): InputStream?
}
