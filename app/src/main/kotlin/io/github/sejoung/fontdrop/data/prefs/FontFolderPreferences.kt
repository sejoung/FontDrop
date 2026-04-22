package io.github.sejoung.fontdrop.data.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FontFolderPreferences(
    private val dataStore: DataStore<Preferences>,
) {
    val folderUri: Flow<String?> = dataStore.data.map { it[FolderUriKey] }

    suspend fun setFolderUri(uriString: String?) {
        dataStore.edit { prefs ->
            if (uriString == null) prefs.remove(FolderUriKey) else prefs[FolderUriKey] = uriString
        }
    }

    companion object {
        private val FolderUriKey = stringPreferencesKey("fonts_folder_uri")
    }
}
