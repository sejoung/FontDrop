package io.github.sejoung.fontdrop.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import io.github.sejoung.fontdrop.data.font.AndroidFontContentReader
import io.github.sejoung.fontdrop.data.font.FontFileMaterializer
import io.github.sejoung.fontdrop.data.font.FontFolderRepository
import io.github.sejoung.fontdrop.data.font.FontFolderRepositoryImpl
import io.github.sejoung.fontdrop.data.prefs.FontFolderPreferences

interface AppContainer {
    val fontFolderRepository: FontFolderRepository
    val fontFileMaterializer: FontFileMaterializer
}

private val Context.fontDropDataStore: DataStore<Preferences> by preferencesDataStore(name = "fontdrop_prefs")

class DefaultAppContainer(private val context: Context) : AppContainer {
    private val appContext = context.applicationContext

    private val fontFolderPreferences by lazy {
        FontFolderPreferences(appContext.fontDropDataStore)
    }

    override val fontFolderRepository: FontFolderRepository by lazy {
        FontFolderRepositoryImpl(
            context = appContext,
            preferences = fontFolderPreferences,
        )
    }

    override val fontFileMaterializer: FontFileMaterializer by lazy {
        FontFileMaterializer(
            reader = AndroidFontContentReader(appContext),
            cacheDirProvider = { appContext.cacheDir },
        )
    }
}
