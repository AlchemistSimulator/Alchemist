/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

var publishCmd = `
./gradlew kotlinUpgradeYarnLock || exit 2
./gradlew performWebsiteStringReplacements || exit 3
git -C build/website/ add . || exit 4
git -C build/website/ commit -m "chore: update website to version \${nextRelease.version}" || exit 5
git -C build/website/ push || exit 6
RELEASE_ON_CENTRAL="./gradlew -PstagingRepositoryId=\${process.env.STAGING_REPO_ID} releaseStagingRepositoryOnMavenCentral"
eval "$RELEASE_ON_CENTRAL" || eval "$RELEASE_ON_CENTRAL" || eval "$RELEASE_ON_CENTRAL" || exit 7
./publishToAUR.sh pkgbuild/PKGBUILD "$CUSTOM_SECRET_0" "$CUSTOM_SECRET_1" "$CUSTOM_SECRET_2" || exit 8
`
var config = require('semantic-release-preconfigured-conventional-commits');
config.plugins.push(
    ["@semantic-release/exec", {
        "publishCmd": publishCmd,
        "successCmd": "export CONTINUE_RELEASE=true"
    }],
    ["@semantic-release/github", {
        "assets": [ 
            { "path": "build/shadow/*-all.jar" },
            { "path": "build/package/*"}
         ]
    }],
    "@semantic-release/git",
)
module.exports = config
