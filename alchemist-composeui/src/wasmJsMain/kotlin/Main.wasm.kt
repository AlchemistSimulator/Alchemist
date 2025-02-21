import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import org.jetbrains.compose.resources.configureWebResources

/**
 * Main entrypoint for the WebAssembly module.
 */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    configureWebResources {
        resourcePathMapping { path -> "./$path" }
    }
    ComposeViewport(checkNotNull(document.body)) {
        catComposable("Kiri", "Kiri is a cute Korat cat.")
    }
}
