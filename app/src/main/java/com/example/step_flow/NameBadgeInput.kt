import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import java.time.format.TextStyle
import kotlin.math.roundToInt

/**
 * Подбор fontSize под ширину (binary search).
 * Работает стабильно и реально уменьшает текст при длинных именах.
 */
private fun findBestFontSizeSp(
    text: String,
    maxWidthPx: Float,
    textMeasurer: TextMeasurer,
    maxSp: Float,
    minSp: Float
): Int {
    if (text.isBlank()) return maxSp.roundToInt()

    var low = minSp
    var high = maxSp
    var best = minSp

    repeat(12) {
        val mid = (low + high) / 2f

        val result = textMeasurer.measure(
            text = text,
            style = androidx.compose.ui.text.TextStyle(
                fontSize = mid.sp,
                fontWeight = FontWeight.Black
            ),
            maxLines = 1
        )

        val fits = result.size.width <= maxWidthPx
        if (fits) {
            best = mid
            low = mid
        } else {
            high = mid
        }
    }

    return best.roundToInt()
}