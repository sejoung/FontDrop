package io.github.sejoung.fontdrop

import android.app.Application
import io.github.sejoung.fontdrop.di.AppContainer
import io.github.sejoung.fontdrop.di.DefaultAppContainer

class FontDropApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
