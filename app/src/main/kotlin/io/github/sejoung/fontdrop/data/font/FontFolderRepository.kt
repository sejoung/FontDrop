package io.github.sejoung.fontdrop.data.font

import kotlinx.coroutines.flow.Flow

interface FontFolderRepository {
    val selectedFolderUri: Flow<String?>

    suspend fun setSelectedFolder(uriString: String)

    suspend fun clearSelectedFolder()

    suspend fun scan(): List<FontAsset>
}
