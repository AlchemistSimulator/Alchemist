/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

const publishCmd = `
git -C build/website/ add . || exit 4
git -C build/website/ commit -m "chore: update website to version \${nextRelease.version}" || exit 5
git -C build/website/ push || exit 6
./gradlew kotlinUpgradeYarnLock -PstagingRepositoryId=\${process.env.STAGING_REPO_ID} releaseStagingRepositoryOnMavenCentral || exit 7
./publishToAUR.sh pkgbuild/PKGBUILD "$CUSTOM_SECRET_0" "$CUSTOM_SECRET_1" "$CUSTOM_SECRET_2" || exit 8
`;

// This is then used by the workflow to signal success to release steps that need other operating systems to be executed
const successCmd = `
touch RELEASED
`;

import config from 'semantic-release-preconfigured-conventional-commits' with {type: 'json'};

config.plugins.push(
    ["@semantic-release/exec", {
        "publishCmd": publishCmd,
        "successCmd": successCmd
    }],
    ["@semantic-release/github", {
        "assets": [
            { "path": "build/shadow/*-all.jar" },
            { "path": "build/package/*"}
         ]
    }],
    "@semantic-release/git",
);
export default config;
