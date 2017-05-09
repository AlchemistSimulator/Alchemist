# Alchemist

Alchemist is a simulator built upon the kinetic Monte Carlo idea, but heavily enriched in order to work as a developing platform for pervasive computing. Please refer to [the Alchemist main website][Alchemist].


## Status Badges

#### Stable branch
[![Build Status](https://travis-ci.org/AlchemistSimulator/Alchemist.svg?branch=master)](https://travis-ci.org/AlchemistSimulator/Alchemist)
[![Javadocs](https://www.javadoc.io/badge/it.unibo.alchemist/alchemist.svg)](https://www.javadoc.io/doc/it.unibo.alchemist/alchemist)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/c7304e8bd4044aa5955c6d5c844f39a4)](https://www.codacy.com/app/Alchemist/Alchemist?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=AlchemistSimulator/Alchemist&amp;utm_campaign=Badge_Grade)

#### Development branch
[![Build Status](https://travis-ci.org/AlchemistSimulator/Alchemist.svg?branch=develop)](https://travis-ci.org/AlchemistSimulator/Alchemist)

## Import via Maven / Gradle

Alchemist is available on Maven Central. You can import all the components by importing the `it.unibo.alchemist:alchemist` artifact.

### Maven

Add this dependency to your build, substitute `ALCHEMIST_VERSION` with the version you want to use.

```xml
<dependency>
    <groupId>it.unibo.alchemist</groupId>
    <artifactId>alchemist</artifactId>
    <version>ALCHEMIST_VERSION</version>
</dependency>
```

### Gradle

Add this dependency to your build, substituting `ALCHEMIST_VERSION` with the version you want to use (change the scope appropriately if you need Alchemist only for runtime or testing).

```groovy
compile 'it.unibo.alchemist:alchemist:ALCHEMIST_VERSION'
```

### Importing a subset of the modules

If you do not need the whole Alchemist machinery but just a sub-part of it, you can restrict the set of imported artifacts by using as dependencies the modules you are actually in need of.

### Javadocs 

Javadocs are available for both [the latest stable version][Javadoc] and [the latest development snapshot][Javadoc-unstable].
If you need to access the documentation for any older stable version, [javadoc.io](https://www.javadoc.io/doc/it.unibo.alchemist/alchemist/) is probably the right place to search in.


## Notes for Developers

### Importing the project
The project has been developed using Eclipse, and can be easily imported in such IDE.

#### Recommended configuration
* Download [the latest Eclipse for Java][eclipse]. For a smooth import, Gradle Buildship is needed (starting from Eclipse Mars.1, it is included by default)
  * Arch Linux users can use the package extra/eclipse-java
  * Ubuntu-based Linux users can install it using [ubuntu-make](https://wiki.ubuntu.com/ubuntu-make): 
    ```bash
    sudo apt-add-repository ppa:ubuntu-desktop/ubuntu-make
    sudo apt-get update
    sudo apt-get install ubuntu-make
    umake ide eclipse 
    ```
* Install the code quality plugins:
  * In Eclipse, click "Help" -> "Eclipse Marketplace..."
  * In the search field enter "findbugs", then press Enter
  * One of the retrieved entries should be "FindBugs Eclipse Plugin", click Install
  * Click "< Install More"
  * In the search field enter "checkstyle", then press Enter
  * One of the retrieved entries should be "Checkstyle Plug-in" with a written icon whose text is "eclipse-cs", click Install
  * Click "< Install More"
  * In the search field enter "xtext", then press Enter
  * One of the retrieved entries should be "Eclipse Xtext", click Install
  * Click "Install Now >"
  * Wait for Eclipse to resolve all the features
  * Click "Confirm >"
  * Follow the instructions, accept the license, wait for Eclipse to download and install the product, accept the installation and restart the IDE
  * When restarted, click "Help" -> "Install New Software..."
  * Click "Add..."
  * In "Location" field, enter `https://dl.bintray.com/pmd/pmd-eclipse-plugin/updates/`
  * The "Name" field is not mandatory (suggested: "PMD")
  * Click OK.
  * If not already selected, in "Work with:" dropdown menu choose the just added update site
  * Select "PMD for Eclipse 4" and click next
  * Follow the instructions, accept the license, wait for Eclipse to download and install the product, accept the installation and restart the IDE.
* Set the line delimiter to LF (only for Windows users)
  * In Eclipse, click window -> preferences
  * In the search form enter "encoding", then press Enter
  * Go to General -> Workspace
  * In the section "New text file line delimiter" check "Other" and choose Unix
  * Apply
* Use space instead of tabs
  * In Eclipse, click window -> preferences
  * Go to General -> Editors -> Text Editors
  * Check "insert spaces for tabs" option.
  * Apply.
  * Go to Java -> Code style -> Formatter
  * Click Edit button
  * In Indentation tab, under "General Settings", set "tab policy" to "Spaces only"
  * Apply (you should probably rename the formatter settings).

#### Import Procedure
* Install git on your system, if you haven't yet
* Pull up a terminal, and `cd` to the folder where you want the project to be cloned (presumably, your Eclipse workspace)
* Clone the project with `git clone git@github.com:AlchemistSimulator/alchemist.git`
  * If you are a Windows user, you might find easier to import via HTTPS: `git clone https://github.com/AlchemistSimulator/Alchemist.git`
  * If the cloning ends with `Permission denied (publickey)` error, please, follow [this](https://help.github.com/articles/error-permission-denied-publickey/) guide.
* In terminal type `git branch`. This shows you all the branches. If you only have the master branch type `git branch -a` to see local and remote branches. For add a remote branch to your local repository type `git checkout -b <branch-name> origin/<branch-name>`. For switch between branches use `git checkout <branch-name>`.
* Open Eclipse
* Click File -> Import -> Gradle -> Gradle Project -> Next
* Select the project root directory, namely, the `alchemist` folder located inside the folder where you have cloned the repository. Do not point to the folder containing this `README.md` file, but to the `alchemist` folder on the same level.
* Next
* Make sure that "Gradle wrapper (recommended)" is selected
* Next
* Wait for Eclipse to scan the project, then make sure that the Gradle project structure can be expanded, and contains an external `alchemist` project and many `alchemist-*` subprojects. If it does not, you have pointed to the wrong folder while importing, go back and select the correct one.
* Finish
* When asked about the existing Eclipse configuration, select "Keep" (so that all the default development options are imported)
* The projects will appear in your projects list.
* Checkstyle, PMD and FindBugs should be pre-configured.
* If you have errors in `alchemist-incarnation-biochemistry` project open a terminal in alchemist folder (do not point to the folder containing this `README.md` file, but to the `alchemist` folder on the same level) and run:
  * If you are a Linux or Mac user `./gradlew alchemist-incarnation-biochemistry:generateGrammarSource`
  * If you are a Windows user: `gradlew.bat alchemist-incarnation-biochemistry:generateGrammarSource`

  Go to Eclipse, right click on `alchemist-incarnation-biochemistry` project -> Refresh
* If you have errors in `alchemist-projectview` project, make sure Eclipse Xtext plugin is correctly installed



### Developing the project
Contributions to this project are welcome. Just some rules:
0. We use [git flow](https://github.com/nvie/gitflow), so if you write new features, please do so in a separate `feature-` branch.
0. We recommend forking the project, developing your stuff, then contributing back via pull request directly from GitHub
0. Commit often. Do not throw at me pull requests with a single giant commit adding or changing the world. Split it in multiple commits and request a merge to the mainline often.
0. Do not introduce low quality code. All the new code must comply with the checker rules (that are quite strict) and must not introduce any other warning. Resolutions of existing warnings (if any is present) are very welcome instead.


#### Building the project
While developing, you can rely on Eclipse to build the project, it will generally do a very good job.
If you want to generate the artifacts, you can rely on Gradle. Just point a terminal on the project's root and issue

```bash
./gradlew
```

This will trigger the creation of the artifacts the executions of the tests, the generation of the documentation and of the project reports.

#### Build reports

Every Alchemist build triggers the creation of a set of reports, that provide hints regarding the current status of quality of the code base. Such reports are available for both [the latest stable][reports] and [the latest development][reports-unstable] versions.

#### Run Alchemist
Alchemist uses YAML for writing simulations. If you want to write your own simulation please follow [this](https://alchemistsimulator.github.io/pages/tutorial/simulations/) guide.<br/>
The complete documentation of alchemist graphical interface can be found [here](https://alchemistsimulator.github.io/pages/tutorial/swingui/)
* In Eclipse
  * Right click on the `alchemist` project -> Run As -> Java Application.
  * Select `Alchemist - it.unibo.alchemist` and click Ok.
* If you have the JAR file
  * Open a terminal
  * Go into the directory where you have downloaded alchemist-redist-VERSION.jar
  * Launch `java -jar alchemist-redist-VERSION.jar`, the graphical interface should pop up (remember to substitute VERSION with the actual version you have downloaded).

#### Release numbers explained
We release often. We are not scared of high version numbers, they are just numbers in the end.
We use a three levels numbering:

* **Update of the minor number**: there are some small changes, and no backwards compatibility is broken. Probably, it is better saying that there is nothing suggesting that any project that depends on this one may have any problem compiling or running. Raise the minor version if there is just a bug fix, or a code improvement, such that no interface, constructor, or non-private member of a class is modified either in syntax or in semantics. Also, no new classes should be provided.
	* Example: switch from 1.2.3 to 1.2.4
* **Update of the middle number**: there are changes that should not break any backwards compatibility, but the possibility exists. Raise the middle version number if there is a remote probability that projects that depend upon this one may have problems compiling if they update. For instance, if you have added a new class, since a depending project may have already defined it, that is enough to trigger a mid-number change. Also updating the version ranges of a dependency, or adding a new dependency, should cause the mid-number to raise. As for minor numbers, no changes to interfaces, constructors or non-private member of classes are allowed. If mid-number is update, minor number should be reset to 0.
	* Example: switch from 1.2.3 to 1.3.0
* **Update of the major number**: *non-backwards-compatible change*. If a change in interfaces, constructors, or public member of some class have happened, a new major number should be issued. This is also the case if the semantics of some method has changed. In general, if there is a high probability that projects depending upon this one may experience compile-time or run-time issues if they switch to the new version, then a new major number should be adopted. If the major version number is upgraded, the mid and minor numbers should be reset to 0.
	* Example: switch from 1.2.3 to 2.0.0


[Alchemist]: http://alchemistsimulator.github.io/
[Javadoc]: http://alchemist-doc.surge.sh/
[Javadoc-unstable]: http://alchemist-unstable-doc.surge.sh/
[reports-unstable]: http://alchemist-unstable-reports.surge.sh/build/reports/buildDashboard/
[reports]: http://alchemist-reports.surge.sh/build/reports/buildDashboard/
[eclipse]: https://eclipse.org/downloads/
