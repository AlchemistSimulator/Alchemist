+++
pre = ""
title = "Import Alchemist as a project dependencie"
weight = 5
tags = ["import", "depencencie", "Gradle", "Maven"]
summary = ""
+++

## Gradle

You need to add the alchemist core dependency, plus the modules you need for your simulation.
Add this dependency to your build, substituting `ALCHEMIST_VERSION` with the version you want to use
(change the scope appropriately if you need Alchemist only for runtime or testing).

```kotlin
dependencies {
    // Alchemist core dependency
    implementation("it.unibo.alchemist:alchemist:ALCHEMIST_VERSION")
    // Example incarnation
    implementation("it.unibo.alchemist:alchemist-incarnation-protelis:ALCHEMIST_VERSION")
    // Example additional module
    implementation("it.unibo.alchemist:alchemist-cognitive-agents:ALCHEMIST_VERSION")
}
```

## Maven

Add this dependency to your build, substitute `ALCHEMIST_VERSION` with the version you want to use. If you do not need the whole Alchemist machinery but just a sub-part of it, you can restrict the set of imported artifacts by using as dependencies the modules you are actually in need of.

```xml
<dependencies>
    <dependency>
        <groupId>it.unibo.alchemist</groupId>
        <artifactId>alchemist</artifactId>
        <version>ALCHEMIST_VERSION</version>
    </dependency>
    <dependency>
        <groupId>it.unibo.alchemist</groupId>
        <artifactId>alchemist-incarnation-protelis</artifactId>
        <version>ALCHEMIST_VERSION</version>
    </dependency>
    <dependency>
        <groupId>it.unibo.alchemist</groupId>
        <artifactId>alchemist-cognitive-agents</artifactId>
        <version>ALCHEMIST_VERSION</version>
    </dependency>
</dependencies>
```
