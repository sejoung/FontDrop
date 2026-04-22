package io.github.sejoung.fontdrop.data.font

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DocumentTreeFontFolderSource(
    private val context: Context,
    private val treeUri: Uri,
) : FontFolderSource {
    override suspend fun listFontFiles(): List<FontFileEntry> = withContext(Dispatchers.IO) {
        val root = DocumentFile.fromTreeUri(context, treeUri) ?: return@withContext emptyList()
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
