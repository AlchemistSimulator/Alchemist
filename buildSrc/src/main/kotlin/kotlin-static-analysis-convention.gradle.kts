/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

import de.aaschmid.gradle.plugins.cpd.Cpd
import Util.excludeGenerated
import gradle.kotlin.dsl.accessors._0bab511c0467aa19fa2193ea27917418.ktlint

plugins {
    id("common-static-analysis-convention")
    id("org.danilopianini.gradle-kotlin-qa")
}

ktlint {
    filter{
        excludeGenerated()
    }
}

private val kmpGenerationTasks get(): TaskCollection<Task> = tasks.matching { task ->
    listOf("Actual", "Compose", "Expect").map { "generate$it" }.any {
        it in task.name
    }
}

tasks.withType<Cpd>().configureEach {
    dependsOn(kmpGenerationTasks)
}
