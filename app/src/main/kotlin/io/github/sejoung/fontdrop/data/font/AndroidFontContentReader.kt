package io.github.sejoung.fontdrop.data.font

import android.content.Context
import androidx.core.net.toUri
import java.io.InputStream

class AndroidFontContentReader(
    private val context: Context,
) : FontContentReader {
    override fun openInputStream(uriString: String): InputStream? {
        return context.contentResolver.openInputStream(uriString.toUri())
    }
}
