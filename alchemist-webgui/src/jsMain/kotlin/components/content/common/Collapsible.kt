package components.content.common

import io.kvision.core.Container
import io.kvision.core.TextAlign
import io.kvision.html.ButtonStyle
import io.kvision.html.button
import io.kvision.panel.SimplePanel
import io.kvision.panel.simplePanel
import io.kvision.panel.vPanel
import io.kvision.utils.perc

fun Container.collapsible(
    className: String = "",
    title: String = "",
    content: (SimplePanel.() -> Unit)? = null,
) {
    vPanel(className = className, spacing = 10) {
        width = 100.perc
        height = 100.perc
        val contentPanel = simplePanel(className = "collapsible-content", init = content).apply {
            width = 100.perc
            hide()
        }
        button(className = "collapsible-button", text = title) {
            textAlign = TextAlign.LEFT

            style = when (contentPanel.visible) {
                true -> ButtonStyle.LIGHT
                false -> ButtonStyle.DARK
            }

            icon = when (contentPanel.visible) {
                true -> "fa-solid fa-caret-up"
                false -> "fa-solid fa-caret-down"
            }

            onClick {
                if (contentPanel.visible) {
                    contentPanel.hide()
                    style = ButtonStyle.DARK
                    icon = "fa-solid fa-caret-down"
                } else {
                    contentPanel.show()
                    style = ButtonStyle.LIGHT
                    icon = "fa-solid fa-caret-up"
                }
            }
        }
        add(contentPanel)
    }
}
