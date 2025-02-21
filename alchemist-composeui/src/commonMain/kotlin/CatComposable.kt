import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private const val PADDING = 16
private const val BACKGROUND = 0xFFFFF0F5

/**
 * Example cat component.
 */
@Composable
fun catComposable(
    name: String,
    funFact: String,
) {
    Card(
        modifier = Modifier.padding(PADDING.dp).fillMaxWidth(),
        shape = RoundedCornerShape(PADDING.dp),
        backgroundColor = Color(BACKGROUND),
    ) {
        Column(modifier = Modifier.padding(PADDING.dp)) {
            Text(
                text = "🐱 $name 🐾",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = "💖 $funFact 💖",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
