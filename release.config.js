var publishCmd = `
git tag -a -f \${nextRelease.version} \${nextRelease.version} -F CHANGELOG.md
git push --force origin \${nextRelease.version}
./gradlew shadowJar --parallel || ./gradlew shadowJar --parallel || exit 1
./gradlew releaseKotlinMavenOnMavenCentralNexus --parallel || exit 2
./gradlew orchidDeploy || ./gradlew orchidDeploy || exit 3
./gradlew publishKotlinMavenPublicationToGithubRepository --continue || true
`
var config = require('semantic-release-preconfigured-conventional-commits');
config.plugins.push(
    ["@semantic-release/exec", {
        "publishCmd": publishCmd,
    }],
    ["@semantic-release/github", {
        "assets": [ 
            { "path": "build/shadow/*-all.jar" },
         ]
    }],
    "@semantic-release/git",
)
module.exports = config
