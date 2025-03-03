/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

import de.aaschmid.gradle.plugins.cpd.Cpd
import org.jlleitschuh.gradle.ktlint.tasks.BaseKtLintCheckTask
import Util.allVerificationTasks
import gradle.kotlin.dsl.accessors._1fb18e6b44f6c16f71a01af678f813e8.ktlint
import org.jlleitschuh.gradle.ktlint.tasks.GenerateReportsTask

plugins {
    id("org.danilopianini.gradle-kotlin-qa")
    id("org.danilopianini.gradle-java-qa")
}

javaQA {
    checkstyle {
        additionalConfiguration.set(rootProject.file("checkstyle-additional-config.xml").readText())
        additionalSuppressions.set(
            """
                <suppress files=".*[\\/]expressions[\\/]parser[\\/].*" checks=".*"/>
                <suppress files=".*[\\/]biochemistrydsl[\\/].*" checks=".*"/>
                """.trimIndent(),
        )
    }
    // enable PMD when this bug is fixed: https://github.com/pmd/pmd/issues/5096
    tasks.withType<Pmd>().configureEach {
        enabled = false
    }
}

fun PatternFilterable.excludeGenerated() {
    exclude { "generated" in it.file.absolutePath }
}

tasks.allVerificationTasks.configureEach {
    excludeGenerated()
}

ktlint {
    filter{
        excludeGenerated()
    }
}

private val generationTasks get(): TaskCollection<Task> = tasks.matching { task ->
    listOf("Actual", "Compose", "Expect").map { "generate$it" }.any {
        it in task.name
    }
}

tasks.withType<Cpd>().configureEach {
    dependsOn(generationTasks)
}
