package io.github.sejoung.fontdrop.data.share

import io.github.sejoung.fontdrop.data.font.FontAsset
import java.io.File

class FakeNoteImageRenderer(
    private val produces: (FakeRenderRequest) -> File? = { request ->
        File.createTempFile("fake-note-${request.title.ifBlank { "untitled" }}-", ".png").apply { deleteOnExit() }
    },
) : NoteImageRenderer {

    val renderCalls = mutableListOf<FakeRenderRequest>()

    override suspend fun render(
        title: String,
        content: String,
        fontSizeSp: Int,
        lineHeightMultiplier: Float,
        asset: FontAsset?,
    ): File? {
        val request = FakeRenderRequest(title, content, fontSizeSp, lineHeightMultiplier, asset)
        renderCalls += request
        return produces(request)
    }
}

data class FakeRenderRequest(
    val title: String,
    val content: String,
    val fontSizeSp: Int,
    val lineHeightMultiplier: Float,
    val asset: FontAsset?,
)
