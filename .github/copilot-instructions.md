# Alchemist Simulator Development Instructions

The Alchemist Simulator is a multi-module Gradle project written in Kotlin/Java for simulating pervasive, aggregate, and nature-inspired computing systems. It includes multiple UI components, simulation engines, and incarnations for different computing paradigms.

**ALWAYS reference these instructions first and fallback to search or bash commands only when you encounter unexpected information that does not match the info here.**

## Working Effectively

### Prerequisites and Environment Setup
- Requires Java 17 or higher (Java 17 is the minimum, project tested with up to Java 24)
- Uses Gradle 9.0.0 with Kotlin DSL
- Environment comes pre-configured with Java 17 OpenJDK
- **NEVER** try to install different Java versions - the environment already has what you need
- **Submodules required for Hugo builds**: Run `git submodule update --init --recursive` before building website

### Core Build Commands
Execute these commands from the repository root:

- **Build the project:**
  ```bash
  ./gradlew assemble --parallel
  ```
  **NEVER CANCEL** - Takes approximately 13 minutes. Set timeout to 20+ minutes.

- **Run all tests:**
  ```bash
  ./gradlew test --parallel
  ```
  **NEVER CANCEL** - Takes approximately 7 minutes. Set timeout to 15+ minutes.

- **Run quality assurance (includes tests, linting, static analysis):**
  ```bash
  ./gradlew check --parallel
  ```
  **NEVER CANCEL** - Takes approximately 20 minutes. Set timeout to 40+ minutes.

- **Build website documentation:**
  ```bash
  git submodule update --init --recursive
  ./gradlew hugoBuild --parallel
  ```
  **NOTE:** Requires submodules to be initialized first. Takes approximately 6-7 minutes.

- **Preview website locally:**
  ```bash
  git submodule update --init --recursive
  ./gradlew hugo --command=serve
  ```

### Running the Simulator
After building the project, you can run simulations:

- **Run a simulation:**
  ```bash
  java -jar ./build/shadow/alchemist-full-*-all.jar run [simulation-file.yml]
  ```
  
- **Headless mode (no GUI):**
  ```bash
  CI=true java -jar ./build/shadow/alchemist-full-*-all.jar run [simulation-file.yml]
  ```

- **Get help:**
  ```bash
  java -jar ./build/shadow/alchemist-full-*-all.jar --help
  java -jar ./build/shadow/alchemist-full-*-all.jar run --help
  ```

### Development Workflow
- **Bootstrap sequence:** The project is self-contained - just run the build commands above
- **Clean build:** If needed, run `./gradlew clean` before building
- **Gradle daemon:** The first build will be slower as it downloads dependencies and sets up the Gradle daemon
- **Pre-commit hooks:** The project automatically installs Git hooks that run ktlint, checkScalafmt on every commit
- **Commit format:** All commits must follow [Conventional Commits](https://www.conventionalcommits.org/) format (fix:, feat:, etc.)
- **Commit timing:** Commits take extra time due to automatic linting/formatting checks

## Validation

### Manual Testing Requirements
- **ALWAYS** build the project before making any code changes: `./gradlew assemble --parallel`
- **ALWAYS** run tests after making changes: `./gradlew test --parallel`
- **ALWAYS** run quality checks before committing: `./gradlew check --parallel`

### Simulation Testing
- **Test basic simulation execution:**
  ```bash
  CI=true java -jar ./build/shadow/alchemist-full-*-all.jar run [simulation-file.yml] --verbosity info
  ```
- **Example simulation files are located in:**
  - `alchemist-*/src/test/resources/` directories
  - Look for `*.yml` files in test directories
  - SAPERE incarnation simulations generally work better than Protelis for basic testing

### Pre-Commit Validation
- The project uses extensive quality checks including:
  - Checkstyle for Java code style
  - ktlint for Kotlin code style
  - SpotBugs for static analysis
  - Copy/Paste Detection (CPD)
  - Unit and integration tests across multiple JVM versions
- **CRITICAL:** All these checks run as part of `./gradlew check --parallel`
- **NEVER** skip the check task - the CI will fail if these don't pass

### Known Build Characteristics
- **First build** will download many dependencies and may take longer
- **Incremental builds** are faster but still substantial due to project size
- **Gradle build cache** is enabled for performance
- **Parallel execution** is enabled and recommended (always use `--parallel`)
- **Memory requirements:** Build requires significant memory (configured for 4GB max heap)
- **Website builds:** Require submodules to be initialized first (`git submodule update --init --recursive`)

## Project Structure

### Key Modules
- `alchemist-api` - Core API definitions
- `alchemist-engine` - Simulation engine
- `alchemist-loading` - Configuration loading and parsing
- `alchemist-swingui` - Swing-based user interface
- `alchemist-composeui` - Compose Multiplatform UI
- `alchemist-web-renderer` - Web-based rendering
- `alchemist-incarnation-*` - Different computing paradigm implementations:
  - `biochemistry` - Chemical reaction networks
  - `protelis` - Aggregate computing
  - `sapere` - Self-organizing systems
  - `scafi` - Scala-based aggregate computing
- `alchemist-cognitive-agents` - Cognitive agent modeling
- `alchemist-maps` - Geographic mapping support
- `alchemist-physics` - Physics simulation
- `site` - Hugo-based documentation website

### Important Files and Locations
- `gradle.properties` - Project configuration including Java version requirements
- `build.gradle.kts` - Root build configuration
- `settings.gradle.kts` - Multi-module project structure
- `.github/workflows/` - CI/CD pipeline definitions
- `site/content/` - Website documentation source

## Common Tasks

### Running Simulations
The project builds a complete simulator. After building:
- Executable JAR files are created in `build/` directories
- The main simulator can be run from the built artifacts
- **Note:** GUI components require X11 forwarding in headless environments

### Working with Documentation
- Documentation source is in `site/content/` using Hugo format
- **Initialize submodules first:** `git submodule update --init --recursive`
- Build with `./gradlew hugoBuild --parallel` (takes ~6-7 minutes)
- Serve locally with `./gradlew hugo --command=serve`
- Documentation includes tutorials, how-to guides, and API references

### Git Workflow and Commits
- **Pre-commit hooks:** Automatically installed, run ktlint and scalafmt on every commit
- **Commit timing:** Commits take additional time due to automatic linting/formatting checks
- **Commit format:** Must follow [Conventional Commits](https://www.conventionalcommits.org/) format:
  - `fix:` for bug fixes
  - `feat:` for new features  
  - `chore:` for maintenance tasks
  - `docs:` for documentation changes
  - Example: `fix(api): resolve null pointer exception in loading module`
- **Submodules:** Initialize with `git submodule update --init --recursive` when needed for website builds

## Performance Expectations

### Build Times (Validated Measurements)
- **Clean assemble:** 13 minutes
- **Test execution:** 7 minutes
- **Full quality check:** 20 minutes
- **Website build:** 6-7 minutes (requires submodules initialized)
- **Incremental builds:** Significantly faster with Gradle caching

### Resource Requirements
- **Memory:** 4GB heap space configured
- **Disk:** Substantial space needed for dependencies and build artifacts
- **CPU:** Benefits from multiple cores (parallel execution enabled)

## Troubleshooting

### Common Issues
- **Out of memory errors:** The build is configured for 4GB heap space - this should be sufficient
- **Long build times:** This is normal for this large project - do not cancel builds prematurely
- **Test failures:** Some tests may be timing-sensitive; re-run if you encounter intermittent failures
- **Gradle daemon issues:** If builds behave strangely, try `./gradlew --stop` to restart the daemon
- **Simulation execution errors:** Some incarnations (like Protelis) may have dependency issues in certain environments
- **Hugo website build failures:** Require submodules initialization first with `git submodule update --init --recursive`
- **Slow commits:** Pre-commit hooks automatically run ktlint and scalafmt - this is normal and required
- **Commit message format errors:** All commits must follow [Conventional Commits](https://www.conventionalcommits.org/) format (e.g., `fix:`, `feat:`, `chore:`)

### Simulation-Specific Issues
- **GUI in headless environments:** Always use `CI=true` environment variable to disable GUI components
- **Protelis simulations:** May fail with missing resource errors - use SAPERE or biochemistry incarnations for testing
- **Port conflicts:** GraphQL monitor may fail if ports are in use - this is normal in test environments

### CI/CD Pipeline
- The project uses GitHub Actions with extensive testing on multiple platforms
- Tests run on Windows, macOS, and Linux
- Multiple JVM versions are tested (17, 21, 24)
- Quality gates include code coverage, static analysis, and style checks

**CRITICAL REMINDERS:**
- **NEVER CANCEL** long-running build or test commands
- **ALWAYS** use `--parallel` flag for better performance
- **ALWAYS** run full validation (`check`) before committing changes
- Set appropriate timeouts: 20+ minutes for builds, 15+ minutes for tests, 40+ minutes for QA checks
- Use `CI=true` for headless simulation execution