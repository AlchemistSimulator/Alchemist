/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

import io.kvision.BootstrapCssModule
import io.kvision.BootstrapIconsModule
import io.kvision.BootstrapModule
import io.kvision.BootstrapUploadModule
import io.kvision.ChartModule
import io.kvision.CoreModule
import io.kvision.DatetimeModule
import io.kvision.FontAwesomeModule
import io.kvision.ImaskModule
import io.kvision.MapsModule
import io.kvision.RichTextModule
import io.kvision.TabulatorCssBootstrapModule
import io.kvision.TabulatorModule
import io.kvision.ToastifyModule
import io.kvision.TomSelectModule
import io.kvision.module
import io.kvision.startApplication
import ui.App

fun main() {
    startApplication(
        ::App,
        module.hot,
        BootstrapModule,
        BootstrapCssModule,
        DatetimeModule,
        RichTextModule,
        TomSelectModule,
        BootstrapUploadModule,
        ImaskModule,
        ToastifyModule,
        FontAwesomeModule,
        BootstrapIconsModule,
        ChartModule,
        TabulatorModule,
        TabulatorCssBootstrapModule,
        MapsModule,
        CoreModule,
    )
}

/*fun main() {
    val container = document.getElementById("root") ?: error("Couldn't find container!")
    createRoot(container.unsafeCast<Element>()).render(App.create())

}*/
