var publishCmd = `
git tag -a -f \${nextRelease.version} \${nextRelease.version} -F CHANGELOG.md
git --git-dir build/website/.git commit -m "chore: update website to version \${nextRelease.version}" || exit 3
git --git-dir build/website/.git push || exit 4
./gradlew shadowJar --parallel || ./gradlew shadowJar --parallel || exit 1
./gradlew releaseKotlinMavenOnMavenCentralNexus --parallel || exit 2
git push --force origin \${nextRelease.version}
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
