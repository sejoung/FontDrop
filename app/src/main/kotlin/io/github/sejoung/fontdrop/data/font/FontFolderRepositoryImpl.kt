package io.github.sejoung.fontdrop.data.font

import android.content.Context
import android.net.Uri
import io.github.sejoung.fontdrop.data.prefs.FontFolderPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class FontFolderRepositoryImpl(
    private val context: Context,
    private val preferences: FontFolderPreferences,
    private val sourceFactory: (Uri) -> FontFolderSource = { uri ->
        DocumentTreeFontFolderSource(context.applicationContext, uri)
    },
) : FontFolderRepository {

    override val selectedFolderUri: Flow<String?> = preferences.folderUri

    override val defaultFontId: Flow<String?> = preferences.defaultFontId

    override suspend fun setSelectedFolder(uriString: String) {
        preferences.setFolderUri(uriString)
    }

    override suspend fun clearSelectedFolder() {
        preferences.setFolderUri(null)
    }

    override suspend fun setDefaultFontId(id: String?) {
        preferences.setDefaultFontId(id)
    }

    override suspend fun scan(): List<FontAsset> {
        val uriString = preferences.folderUri.first() ?: return emptyList()
        val source = sourceFactory(Uri.parse(uriString))
        return FontScanner.scan(source.listFontFiles())
    }
}
