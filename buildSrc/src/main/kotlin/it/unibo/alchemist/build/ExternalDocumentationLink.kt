/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.build

import java.net.URI

@ConsistentCopyVisibility
data class ExternalDocumentationLink private constructor(
    val descriptor: String,
    val documentationUrl: URI,
    val packageListUrl: URI,
) : Comparable<ExternalDocumentationLink> {

    constructor(descriptor: ExternalDependency, documentationUrl: URI, packageListUrl: URI)
        : this(descriptor.toString(), documentationUrl, packageListUrl)

    constructor(descriptor: ExternalDependency, documentationUrl: URI, packageListUrl: String)
        : this(descriptor, documentationUrl, URI.create(packageListUrl))

    constructor(descriptor: ExternalDependency, documentationUrl: String, packageListUrl: URI)
        : this(descriptor, URI.create(documentationUrl), packageListUrl)

    constructor(descriptor: ExternalDependency, documentationUrl: String, packageListUrl: String)
        : this(descriptor, URI.create(documentationUrl), packageListUrl)

    override fun toString() = "$descriptor@($documentationUrl, $packageListUrl)"

    override fun compareTo(other: ExternalDocumentationLink): Int = descriptor.compareTo(other.descriptor)
}
