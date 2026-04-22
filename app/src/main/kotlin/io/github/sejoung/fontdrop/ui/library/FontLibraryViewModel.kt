package io.github.sejoung.fontdrop.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.sejoung.fontdrop.data.font.FontAsset
import io.github.sejoung.fontdrop.data.font.FontFolderRepository
import io.github.sejoung.fontdrop.data.font.FontPrewarmer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FontLibraryViewModel(
    private val repository: FontFolderRepository,
    private val prewarmer: FontPrewarmer,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FontLibraryUiState())
    val uiState: StateFlow<FontLibraryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val uri = repository.selectedFolderUri.first()
            _uiState.update { it.copy(hasSelectedFolder = uri != null, folderUri = uri) }
            if (uri != null) refresh()
        }
        viewModelScope.launch {
            repository.defaultFontId.collect { id ->
                _uiState.update { it.copy(selectedFontId = id) }
            }
        }
    }

    fun onFolderSelected(uriString: String) {
        viewModelScope.launch {
            repository.setSelectedFolder(uriString)
            _uiState.update {
                it.copy(
                    hasSelectedFolder = true,
                    folderUri = uriString,
                    errorMessage = null,
                )
            }
            refresh()
        }
    }

    fun onRefresh() {
        viewModelScope.launch { refresh() }
    }

    fun onFontTapped(asset: FontAsset) {
        viewModelScope.launch {
            val current = repository.defaultFontId.first()
            val next = if (current == asset.id) null else asset.id
            repository.setDefaultFontId(next)
            if (next != null) prewarmer.ensureLoaded(asset)
        }
    }

    fun onClearFolder() {
        viewModelScope.launch {
            repository.clearSelectedFolder()
            repository.setDefaultFontId(null)
            _uiState.update { FontLibraryUiState() }
        }
    }

    private suspend fun refresh() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        val result = runCatching { repository.scan() }
        _uiState.update { current ->
            result.fold(
                onSuccess = { fonts ->
                    val selectionSurvives = fonts.any { it.id == current.selectedFontId }
                    current.copy(
                        isLoading = false,
                        fonts = fonts,
                        selectedFontId = current.selectedFontId.takeIf { selectionSurvives },
                    )
                },
                onFailure = { throwable ->
                    current.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Failed to scan fonts folder",
                    )
                },
            )
        }
        val fonts = result.getOrNull() ?: return
        viewModelScope.launch { prewarmer.prewarm(fonts) }
        val persistedDefault = repository.defaultFontId.first()
        if (persistedDefault != null && fonts.none { it.id == persistedDefault }) {
            repository.setDefaultFontId(null)
        }
    }

    companion object {
        fun factory(
            repository: FontFolderRepository,
            prewarmer: FontPrewarmer,
        ) = viewModelFactory {
            initializer { FontLibraryViewModel(repository, prewarmer) }
        }
    }
}
