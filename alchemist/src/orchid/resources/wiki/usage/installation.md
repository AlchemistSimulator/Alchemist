---

title: Requirements and installation

---

Alchemist requires java 11+, if you don't have a Java Development Kit version 11+ installed we may recommend you to install the version of AdoptOpenJDK, which can be found [here](https://adoptopenjdk.net/index.html?variant=openjdk11&jvmVariant=hotspot). If you prefer, you can install it via [Jabba](https://github.com/shyiko/jabba) - a cross-platform Java Version Manager.

For further information, the JDKs and environments tested [here](https://travis-ci.org/AlchemistSimulator/Alchemist) are those for which executing Alchemist is supported.

## Importing Alchemist

In this section you will be given information about how to import the Alchemist project in [IntelliJ IDEA](https://www.jetbrains.com/idea/) and [Eclipse](https://www.eclipse.org/eclipseide/).

### Importing in IntelliJ

#### Recommended configuration

Install the following plugins (use <kbd>Ctrl</kbd>+<kbd>Shift</kbd>+<kbd>A</kbd>, then search for "Plugins"):
* From the main list:
    * Scala
    * Kotlin
* From "Browse Repositories":
    * [ANTLR v4 grammar plugin](https://plugins.jetbrains.com/plugin/7358-antlr-v4-grammar-plugin)
    * [Checkstyle-IDEA](https://plugins.jetbrains.com/plugin/1065-checkstyle-idea)
    * [FindBugs-IDEA](https://plugins.jetbrains.com/plugin/3847-findbugs-idea)
    * [PMDPlugin](https://plugins.jetbrains.com/plugin/1137-pmdplugin)

#### Import procedure

0. Windows user should perform an additional first step: before cloning the repository, make sure that the autocrlf feature of git is disabled, by issuing `git config --global core.autocrlf false`. If the step is not performed, git will automatically insert CRLF line endings, violating the project's Checkstyle rules (which are rather strict, and prevent the build from succeeding).
0. Clone this repository in a folder of your preference using `git clone` appropriately
0. Open IntellJ. If a project opens automatically, select "Close project". You should be on the welcome screen of IntelliJ idea, with an aspect similar to this image: ![IntelliJ Welcome Screen](https://www.jetbrains.com/help/img/idea/2018.2/ideaWelcomeScreen.png)
0. Select "Import Project"
0. Navigate your file system and find the folder where you cloned the repository. **Do not select it**. Open the folder, and you should find a lowercase `alchemist` folder. That is the correct project folder, not the outermost `Alchemist` folder (created by `git` in case you cloned without specifying a different folder name). Once the correct folder has been selected, click <kbd>Ok</kbd>
0. Select "Import Project from external model"
0. Make sure "Gradle" is selected as external model tool
0. Click <kbd>Finish</kbd>
0. If prompted to override any .idea file, try to answer <kbd>No</kbd>. It's possible that IntelliJ refuses to proceed, in which case click <kbd>Finish</kbd> again, then select <kbd>Yes</kbd>.
0. A dialog stating that "IntelliJ IDEA found a Gradle build script" may appear, in such case answer <kbd>Import Gradle Project</kbd>.
0. Wait for the IDE to import the project from Gradle. The process may take several minutes, due to the amount of dependencies. Should the synchronization fail, make sure that the IDE's Gradle is configured correctly:
0. In 'Settings -> Build, Execution, Deployment -> Build Tools > Gradle', for the option 'Use Gradle from' select 'gradle-wrapper.properties file'. Enabling auto-import is also recommended. 
0. **Important:** Alchemist requires java 11+, so make sure the 'Gradle JVM' option points to such a version (if you don't have a JDK 11+ installed make sure to get one).
0. Once imported, the project may still be unable to compile, due to missing sources in incarnation-biochemistry. This problem can be solved by opening the IntelliJ terminal (e.g. with <kbd>Ctrl</kbd>+<kbd>Shift</kbd>+<kbd>A</kbd>, typing "terminal" and pressing <kbd>Enter</kbd>), and issue:
  - *On Unix: * `./gradlew alchemist-incarnation-biochemistry:generateGrammarSource`
  - *On Windows: * `gradlew.bat alchemist-incarnation-biochemistry:generateGrammarSource`

### Importing in Eclipse

#### Recommended configuration
* Download [the latest Eclipse for Java][eclipse]. For a smooth import, Gradle Buildship is needed (starting from Eclipse Mars.1, it is included by default)
  * Arch Linux users can use the package `eclipse-java`
  * Ubuntu-based Linux users can install it using [ubuntu-make](https://wiki.ubuntu.com/ubuntu-make): 
    ```bash
    sudo apt-add-repository ppa:ubuntu-desktop/ubuntu-make
    sudo apt-get update
    sudo apt-get install ubuntu-make
    umake ide eclipse 
    ```
* Install the Scala interpreter:
  * Arch Linux users can use the package `scala`
  * Ubuntu-based Linux users can use the package scala:
    ```bash
    sudo apt-get update
    sudo apt-get install scala
    ```
  * Users of other OS can follow the [official installation guide](https://www.scala-lang.org/download/install.html)
* Install the required eclipse plugins:
  * In Eclipse, click "Help" -> "Eclipse Marketplace..."
  * In the search field enter "findbugs", then press Enter
  * One of the retrieved entries should be "FindBugs Eclipse Plugin", click Install
  * Click "< Install More"
  * In the search field enter "checkstyle", then press Enter
  * One of the retrieved entries should be "Checkstyle Plug-in" with a written icon whose text is "eclipse-cs", click Install
  * Click "< Install More"
  * In the search field enter "xtext", then press Enter
  * One of the retrieved entries should be "Eclipse Xtext", click Install
  * Click "< Install More"
  * In the search field enter "scala", then press Enter
  * One of the retrieved entries should be "Scala IDE 4.2.x", click Install
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
