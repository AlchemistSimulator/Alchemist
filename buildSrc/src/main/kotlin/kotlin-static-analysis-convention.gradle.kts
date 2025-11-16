/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

import de.aaschmid.gradle.plugins.cpd.Cpd
import it.unibo.alchemist.build.allVerificationTasks
import it.unibo.alchemist.build.excludeGenerated
import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
    id("org.danilopianini.gradle-kotlin-qa")
}

extensions.getByName<KtlintExtension>("ktlint").apply { filter { excludeGenerated() } }

private val kmpGenerationTasks get(): TaskCollection<Task> = tasks.matching { task ->
    listOf("Actual", "Compose", "Expect").map { "generate$it" }.any {
        it in task.name
    }
}

private val kspTasks get(): TaskCollection<Task> = tasks.matching { task ->
    task.name.startsWith("ksp")
}
tasks.withType<Cpd>().configureEach {
    dependsOn(kmpGenerationTasks)
    dependsOn(kspTasks)
    excludeGenerated()
    exclude("**/build/generated/**")
    exclude("**/generated/**")
}

tasks.allVerificationTasks.configureEach { excludeGenerated() }
