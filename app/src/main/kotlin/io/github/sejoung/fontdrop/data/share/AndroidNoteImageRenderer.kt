package io.github.sejoung.fontdrop.data.share

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Typeface
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt
import androidx.core.graphics.withTranslation
import io.github.sejoung.fontdrop.data.font.FontAsset
import io.github.sejoung.fontdrop.data.font.FontFileMaterializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Renders a note to a PNG that preserves the user-selected typography. The
 * output lives under `cacheDir/share/` so FileProvider can stream it to any
 * app that handles `image/png`.
 */
class AndroidNoteImageRenderer(
    private val context: Context,
    private val materializer: FontFileMaterializer,
) : NoteImageRenderer {

    override suspend fun render(
        title: String,
        content: String,
        fontSizeSp: Int,
        lineHeightMultiplier: Float,
        asset: FontAsset?,
    ): File? = withContext(Dispatchers.IO) {
        runCatching {
            val typeface = asset
                ?.let { materializer.materialize(it) }
                ?.let { runCatching { Typeface.createFromFile(it) }.getOrNull() }
                ?: Typeface.DEFAULT

            val contentWidth = IMAGE_WIDTH - PADDING * 2
            val displayTitle = title.ifBlank { "Untitled" }
            val displayContent = content.ifBlank { " " }

            val titlePx = fontSizeSp * SP_TO_PX_SCALE * TITLE_RATIO
            val bodyPx = fontSizeSp * SP_TO_PX_SCALE

            val titlePaint = TextPaint().apply {
                this.typeface = typeface
                textSize = titlePx
                color = COLOR_INK_900
                isAntiAlias = true
            }
            val bodyPaint = TextPaint().apply {
                this.typeface = typeface
                textSize = bodyPx
                color = COLOR_INK_900
                isAntiAlias = true
            }

            val titleLayout = StaticLayout.Builder
                .obtain(displayTitle, 0, displayTitle.length, titlePaint, contentWidth)
                .setLineSpacing(0f, lineHeightMultiplier)
                .build()
            val bodyLayout = StaticLayout.Builder
                .obtain(displayContent, 0, displayContent.length, bodyPaint, contentWidth)
                .setLineSpacing(0f, lineHeightMultiplier)
                .build()

            val metaPaint: TextPaint? = asset?.let {
                TextPaint().apply {
                    this.typeface = Typeface.DEFAULT
                    textSize = META_PX
                    color = COLOR_INK_500
                    isAntiAlias = true
                }
            }
            val metaText = asset?.familyName
            val metaHeight = if (metaText != null) (META_PX * 1.6f).toInt() + PADDING else 0

            val textHeight = titleLayout.height + BLOCK_SPACING + bodyLayout.height
            val imageHeight = PADDING + textHeight + PADDING + metaHeight

            val bitmap = createBitmap(IMAGE_WIDTH, imageHeight)
            val canvas = Canvas(bitmap).apply { drawColor(COLOR_PAPER_50) }

            canvas.withTranslation(PADDING.toFloat(), PADDING.toFloat()) {
                titleLayout.draw(this)
                translate(0f, (titleLayout.height + BLOCK_SPACING).toFloat())
                bodyLayout.draw(this)
            }

            if (metaText != null && metaPaint != null) {
                val baseline = (imageHeight - PADDING).toFloat()
                canvas.drawText(metaText, PADDING.toFloat(), baseline, metaPaint)
            }

            val dir = File(context.cacheDir, ShareDir).apply { mkdirs() }
            val file = File(dir, "note-${System.currentTimeMillis()}.png")
            file.outputStream().use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            bitmap.recycle()
            file
        }.getOrNull()
    }

    companion object {
        const val ShareDir: String = "share"
        private const val IMAGE_WIDTH = 1080
        private const val PADDING = 72
        private const val BLOCK_SPACING = 40
        private const val SP_TO_PX_SCALE = 3f
        private const val TITLE_RATIO = 1.6f
        private const val META_PX = 26f
        private val COLOR_PAPER_50 = "#FFF9F0".toColorInt()
        private val COLOR_INK_900 = "#FF173C34".toColorInt()
        private val COLOR_INK_500 = "#FF4E786C".toColorInt()
    }
}
