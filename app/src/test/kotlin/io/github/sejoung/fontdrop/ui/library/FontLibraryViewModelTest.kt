package io.github.sejoung.fontdrop.ui.library

import io.github.sejoung.fontdrop.data.font.FakeFontPrewarmer
import io.github.sejoung.fontdrop.data.font.FontAsset
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FontLibraryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `initial state has no folder selected when repo is empty`() = runTest {
        val repo = FakeFontFolderRepository()

        val vm = FontLibraryViewModel(repo, FakeFontPrewarmer())

        val state = vm.uiState.value
        assertFalse(state.hasSelectedFolder)
        assertNull(state.folderUri)
        assertTrue(state.fonts.isEmpty())
        assertEquals(0, repo.scanCount)
    }

    @Test
    fun `initial scan runs when repo has persisted folder uri`() = runTest {
        val repo = FakeFontFolderRepository(initialFolderUri = "content://tree/fonts")
        repo.scanResult = Result.success(listOf(asset("Inter")))

        val vm = FontLibraryViewModel(repo, FakeFontPrewarmer())

        val state = vm.uiState.value
        assertTrue(state.hasSelectedFolder)
        assertEquals("content://tree/fonts", state.folderUri)
        assertEquals(listOf("Inter"), state.fonts.map { it.familyName })
        assertEquals(1, repo.scanCount)
        assertFalse(state.isLoading)
    }

    @Test
    fun `onFolderSelected persists uri and triggers scan`() = runTest {
        val repo = FakeFontFolderRepository()
        repo.scanResult = Result.success(listOf(asset("Roboto"), asset("Inter")))
        val vm = FontLibraryViewModel(repo, FakeFontPrewarmer())

        vm.onFolderSelected("content://tree/new")

        val state = vm.uiState.value
        assertTrue(state.hasSelectedFolder)
        assertEquals("content://tree/new", state.folderUri)
        assertEquals(setOf("Roboto", "Inter"), state.fonts.map { it.familyName }.toSet())
        assertEquals(1, repo.scanCount)
    }

    @Test
    fun `onRefresh triggers another scan`() = runTest {
        val repo = FakeFontFolderRepository(initialFolderUri = "content://tree/fonts")
        repo.scanResult = Result.success(emptyList())
        val vm = FontLibraryViewModel(repo, FakeFontPrewarmer())

        vm.onRefresh()

        assertEquals(2, repo.scanCount)
    }

    @Test
    fun `scan failure surfaces error message and clears loading`() = runTest {
        val repo = FakeFontFolderRepository()
        repo.scanResult = Result.failure(IllegalStateException("boom"))
        val vm = FontLibraryViewModel(repo, FakeFontPrewarmer())

        vm.onFolderSelected("content://tree/any")

        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.errorMessage)
        assertEquals("boom", state.errorMessage)
    }

    @Test
    fun `onFontTapped toggles selection on and off and persists it`() = runTest {
        val repo = FakeFontFolderRepository(initialFolderUri = "content://tree/fonts")
        val inter = asset("Inter")
        repo.scanResult = Result.success(listOf(inter, asset("Roboto")))
        val vm = FontLibraryViewModel(repo, FakeFontPrewarmer())

        vm.onFontTapped(inter)
        assertEquals("Inter", vm.uiState.value.selectedFontId)
        assertEquals("Inter", repo.defaultFontId.value)

        vm.onFontTapped(inter)
        assertNull(vm.uiState.value.selectedFontId)
        assertNull(repo.defaultFontId.value)
    }

    @Test
    fun `persisted default font id seeds state on construction`() = runTest {
        val repo = FakeFontFolderRepository(
            initialFolderUri = "content://tree/fonts",
            initialDefaultFontId = "Inter",
        )
        repo.scanResult = Result.success(listOf(asset("Inter")))

        val vm = FontLibraryViewModel(repo, FakeFontPrewarmer())

        assertEquals("Inter", vm.uiState.value.selectedFontId)
    }

    @Test
    fun `onFontTapped switches selection between assets`() = runTest {
        val repo = FakeFontFolderRepository(initialFolderUri = "content://tree/fonts")
        val inter = asset("Inter")
        val roboto = asset("Roboto")
        repo.scanResult = Result.success(listOf(inter, roboto))
        val vm = FontLibraryViewModel(repo, FakeFontPrewarmer())

        vm.onFontTapped(inter)
        vm.onFontTapped(roboto)

        assertEquals("Roboto", vm.uiState.value.selectedFontId)
    }

    @Test
    fun `refresh drops selection when selected font disappears`() = runTest {
        val repo = FakeFontFolderRepository(initialFolderUri = "content://tree/fonts")
        val inter = asset("Inter")
        repo.scanResult = Result.success(listOf(inter))
        val vm = FontLibraryViewModel(repo, FakeFontPrewarmer())
        vm.onFontTapped(inter)
        assertEquals("Inter", vm.uiState.value.selectedFontId)

        repo.scanResult = Result.success(listOf(asset("Roboto")))
        vm.onRefresh()

        assertNull(vm.uiState.value.selectedFontId)
    }

    @Test
    fun `scan result is prewarmed so previews do not stall on render`() = runTest {
        val repo = FakeFontFolderRepository(initialFolderUri = "content://tree/fonts")
        repo.scanResult = Result.success(listOf(asset("Inter"), asset("Roboto")))
        val prewarmer = FakeFontPrewarmer()

        FontLibraryViewModel(repo, prewarmer)

        assertEquals(listOf(listOf("Inter", "Roboto")), prewarmer.prewarmed)
    }

    @Test
    fun `tapping a font nudges it to the top of the warm up queue`() = runTest {
        val repo = FakeFontFolderRepository(initialFolderUri = "content://tree/fonts")
        val inter = asset("Inter")
        repo.scanResult = Result.success(listOf(inter))
        val prewarmer = FakeFontPrewarmer()
        val vm = FontLibraryViewModel(repo, prewarmer)

        vm.onFontTapped(inter)

        assertEquals(listOf("Inter"), prewarmer.ensured)
    }

    @Test
    fun `onClearFolder resets state to empty`() = runTest {
        val repo = FakeFontFolderRepository(initialFolderUri = "content://tree/fonts")
        repo.scanResult = Result.success(listOf(asset("Inter")))
        val vm = FontLibraryViewModel(repo, FakeFontPrewarmer())

        vm.onClearFolder()

        val state = vm.uiState.value
        assertFalse(state.hasSelectedFolder)
        assertNull(state.folderUri)
        assertTrue(state.fonts.isEmpty())
        assertNull(repo.selectedFolderUri.value)
    }

    private fun asset(family: String) = FontAsset(
        id = family,
        uriString = "content://tree/$family",
        displayName = "$family.ttf",
        familyName = family,
        extension = "ttf",
        sizeBytes = 0,
    )
}
