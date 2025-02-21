import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import org.jetbrains.skiko.wasm.onWasmReady

/**
 * Main entrypoint for the Javascript module.
 */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    onWasmReady {
        ComposeViewport(checkNotNull(document.body)) {
            catComposable("Kiri", "Kiri is a cute Korat cat.")
        }
    }
}
