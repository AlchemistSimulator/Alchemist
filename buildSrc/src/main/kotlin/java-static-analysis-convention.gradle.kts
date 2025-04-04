/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

plugins {
    id("common-static-analysis-convention")
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
