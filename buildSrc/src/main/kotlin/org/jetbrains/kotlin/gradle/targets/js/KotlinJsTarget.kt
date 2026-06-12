package org.jetbrains.kotlin.gradle.targets.js

import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrTarget

/**
 * Compatibility type for plugins still checking the removed legacy Kotlin JS target.
 */
abstract class KotlinJsTarget : KotlinTarget {
    abstract val irTarget: KotlinJsIrTarget?
}
