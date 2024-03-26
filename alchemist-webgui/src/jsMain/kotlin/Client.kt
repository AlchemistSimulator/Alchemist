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
import io.kvision.CoreModule
import io.kvision.FontAwesomeModule
import io.kvision.ToastifyModule
import io.kvision.TomSelectModule
import io.kvision.module
import io.kvision.startApplication
import ui.AppMain

/**
 * The application main entry point.
 *
 * */
fun main() {
    startApplication(
        ::AppMain,
        module.hot,
        BootstrapModule,
        BootstrapCssModule,
        TomSelectModule,
        BootstrapUploadModule,
        ToastifyModule,
        FontAwesomeModule,
        BootstrapIconsModule,
        CoreModule,
    )
}
