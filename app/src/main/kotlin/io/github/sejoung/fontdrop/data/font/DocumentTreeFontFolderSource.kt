package io.github.sejoung.fontdrop.data.font

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/** Thrown when Android no longer grants us access to the persisted tree URI
 *  (permission revoked, folder deleted, or the URI itself is malformed). */
class FontFolderAccessLostException(message: String) : IOException(message)

class DocumentTreeFontFolderSource(
    private val context: Context,
    private val treeUri: Uri,
) : FontFolderSource {
    override suspend fun listFontFiles(): List<FontFileEntry> = withContext(Dispatchers.IO) {
        val root = DocumentFile.fromTreeUri(context, treeUri)
            ?: throw FontFolderAccessLostException(
                "Fonts folder access was lost. Select the folder again."
            )
        root.listFiles()
            .asSequence()
            .filter { it.isFile }
            .mapNotNull { doc ->
                val name = doc.name ?: return@mapNotNull null
                FontFileEntry(
                    uriString = doc.uri.toString(),
                    displayName = name,
                    sizeBytes = doc.length(),
                )
            }
            .toList()
    }
}
