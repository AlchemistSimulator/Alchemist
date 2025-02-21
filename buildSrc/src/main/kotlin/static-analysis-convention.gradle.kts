/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

import Util.allVerificationTasks
import de.aaschmid.gradle.plugins.cpd.Cpd

plugins {
    id("org.danilopianini.gradle-kotlin-qa")
    id("org.danilopianini.gradle-java-qa")
}

tasks.allVerificationTasks.configureEach {
    exclude("**/generated/**")
}

private val generationTaskNames = listOf(
    "Actual",
    "Compose",
    "Expect",
).map {
    "generate$it"
}

tasks.withType<Cpd>().configureEach {
    dependsOn(
        tasks.matching { task ->
            generationTaskNames.any {
                it in task.name
            }
        }
    )
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
    // TODO: enable PMD when this bug is fixed: https://github.com/pmd/pmd/issues/5096
    tasks.withType<Pmd>().configureEach {
        enabled = false
    }
}




