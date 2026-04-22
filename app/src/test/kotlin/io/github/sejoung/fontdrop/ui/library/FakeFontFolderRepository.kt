package io.github.sejoung.fontdrop.ui.library

import io.github.sejoung.fontdrop.data.font.FontAsset
import io.github.sejoung.fontdrop.data.font.FontFolderRepository
import kotlinx.coroutines.flow.MutableStateFlow

class FakeFontFolderRepository(
    initialFolderUri: String? = null,
) : FontFolderRepository {

    private val folderUri = MutableStateFlow(initialFolderUri)
    override val selectedFolderUri = folderUri

    var scanResult: Result<List<FontAsset>> = Result.success(emptyList())
    var scanCount: Int = 0
        private set

    override suspend fun setSelectedFolder(uriString: String) {
        folderUri.value = uriString
    }

    override suspend fun clearSelectedFolder() {
        folderUri.value = null
    }

    override suspend fun scan(): List<FontAsset> {
        scanCount += 1
        return scanResult.getOrThrow()
    }
}
