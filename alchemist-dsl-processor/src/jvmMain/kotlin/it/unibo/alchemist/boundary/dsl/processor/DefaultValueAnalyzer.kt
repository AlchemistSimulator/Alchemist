package it.unibo.alchemist.boundary.dsl.processor

import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import java.io.File
import java.io.IOException

/**
 * Analyzes and extracts default values from constructor parameters.
 */
object DefaultValueAnalyzer {
    private val MATH_CONSTANTS = setOf("PI", "E")
    private val MATH_FUNCTIONS = setOf(
        "sin", "cos", "tan", "asin", "acos", "atan", "atan2",
        "sinh", "cosh", "tanh", "exp", "log", "log10", "sqrt", "abs", "ceil", "floor",
        "round", "max", "min", "pow",
    )
    private val BUILTIN_TYPES = setOf("Double", "Int", "Long", "Float", "String", "Boolean", "List", "Map", "Set")

    /**
     * Extracts default values for all parameters that have them.
     *
     * @param remainingParams The parameters to extract default values from
     * @param classDecl The class declaration (unused, kept for API compatibility)
     * @return List of default value strings, null for parameters without defaults
     */
    @Suppress("UNUSED_PARAMETER")
    fun extractAllDefaultValues(remainingParams: List<KSValueParameter>, classDecl: KSClassDeclaration): List<String> =
        remainingParams.mapNotNull { param ->
            if (param.hasDefault) {
                tryExtractDefaultFromSource(param)
            } else {
                null
            }
        }

    /**
     * Extracts import statements needed for default value expressions.
     *
     * @param defaultValues List of default value strings
     * @param classDecl The class declaration to analyze imports from
     * @return Set of import statements needed
     */
    fun extractNeededImportsFromDefaults(defaultValues: List<String>, classDecl: KSClassDeclaration): Set<String> {
        val neededImports = mutableSetOf<String>()
        val defaultValueText = defaultValues.joinToString(" ")
        val sourceImports = getSourceFileImports(classDecl)
        neededImports.addAll(extractIdentifierImports(defaultValueText, sourceImports))
        return neededImports
    }

    private fun extractIdentifierImports(defaultValueText: String, sourceImports: List<String>): Set<String> {
        val neededImports = mutableSetOf<String>()
        // Use regex to find potential class/function identifiers so we can detect missing imports.
        val identifierPattern = Regex("""\b([A-Z][a-zA-Z0-9]*)\b""")
        val matches = identifierPattern.findAll(defaultValueText)
        for (match in matches) {
            val identifier = match.groupValues[1]
            if (!(identifier in MATH_CONSTANTS || identifier in MATH_FUNCTIONS || identifier in BUILTIN_TYPES)) {
                val qualifiedPattern = Regex("""\w+\.$identifier\b""")
                if (!qualifiedPattern.containsMatchIn(defaultValueText)) {
                    addImportIfNeeded(identifier, defaultValueText, sourceImports, null, neededImports)
                }
            }
        }
        return neededImports
    }

    private fun addImportIfNeeded(
        identifier: String,
        defaultValueText: String,
        sourceImports: List<String>,
        defaultPackage: String?,
        neededImports: MutableSet<String>,
    ) {
        val pattern = Regex("""\b$identifier\b""")
        if (pattern.containsMatchIn(defaultValueText)) {
            val qualifiedPattern = Regex("""\w+\.$identifier""")
            if (!qualifiedPattern.containsMatchIn(defaultValueText)) {
                val import = findImportForIdentifier(identifier, sourceImports, defaultPackage)
                if (import != null) {
                    neededImports.add(import)
                }
            }
        }
    }

    private fun getSourceFileImports(classDecl: KSClassDeclaration): List<String> = try {
        val file = classDecl.containingFile
        if (file != null) {
            val filePath = file.filePath
            run {
                val sourceCode = File(filePath).readText()
                val importPattern = Regex("""^import\s+(.+)$""", RegexOption.MULTILINE)
                importPattern.findAll(sourceCode).map { it.groupValues[1] }.toList()
            }
        } else {
            emptyList()
        }
    } catch (e: IOException) {
        throw IllegalStateException("Failed to read source file imports", e)
    }

    private fun findImportForIdentifier(
        identifier: String,
        sourceImports: List<String>,
        defaultPackage: String?,
    ): String? {
        val explicitImport = sourceImports.find { it.endsWith(".$identifier") }
        if (explicitImport != null) {
            return "import $explicitImport"
        }
        return if (defaultPackage != null) {
            "import $defaultPackage.$identifier"
        } else {
            null
        }
    }

    /**
     * Attempts to extract a default value from the source code for a parameter.
     *
     * @param param The parameter to extract the default value for
     * @return The default value string, or null if not found or extraction fails
     */
    @Suppress("SwallowedException")
    fun tryExtractDefaultFromSource(param: KSValueParameter): String? = try {
        extractDefaultValueFromFile(param)
    } catch (e: IOException) {
        null
    }

    private fun extractDefaultValueFromFile(param: KSValueParameter): String? {
        val extractionData = prepareExtractionData(param) ?: return null
        val defaultValue = extractBalancedExpression(extractionData.sourceCode, extractionData.startIndex)
        val trimmed = defaultValue?.trim()?.takeIf { it.isNotEmpty() && !it.contains("\n") }
        val result = getDefaultValueResult(trimmed, param, extractionData.sourceCode)
        return result?.let { qualifyMathIdentifiers(it) }
    }

    private fun getDefaultValueResult(trimmed: String?, param: KSValueParameter, sourceCode: String): String? {
        if (trimmed != null) {
            return trimmed
        }
        return extractDefaultValueFromParam(param, sourceCode)
    }

    private fun extractDefaultValueFromParam(param: KSValueParameter, sourceCode: String): String? {
        if (!param.hasDefault) {
            return null
        }
        val paramName = param.name?.asString()
        return paramName?.let { tryExtractSimpleDefault(sourceCode, it) }
    }

    private fun qualifyMathIdentifiers(defaultValue: String): String {
        val replacements = mutableListOf<Pair<IntRange, String>>()
        replacements.addAll(findConstantReplacements(defaultValue))
        replacements.addAll(findFunctionReplacements(defaultValue))
        if (replacements.isEmpty()) {
            return defaultValue
        }
        return applyReplacements(defaultValue, replacements)
    }

    private fun findConstantReplacements(defaultValue: String): List<Pair<IntRange, String>> =
        findMathReplacements(defaultValue, MATH_CONSTANTS) { identifier -> """\b$identifier\b""" }

    private fun findFunctionReplacements(defaultValue: String): List<Pair<IntRange, String>> =
        findMathReplacements(defaultValue, MATH_FUNCTIONS) { identifier -> """\b$identifier\s*\(""" }

    private fun findMathReplacements(
        defaultValue: String,
        identifiers: Set<String>,
        patternBuilder: (String) -> String,
    ): List<Pair<IntRange, String>> {
        val replacements = mutableListOf<Pair<IntRange, String>>()
        // Patterns above match both qualified and
        // unqualified usages so we only rewrite the unqualified ones.
        for (identifier in identifiers) {
            val patternStr = patternBuilder(identifier)
            val pattern = Regex(patternStr)
            val qualifiedPattern = Regex("""\w+\.""" + patternStr.removePrefix("""\b"""))
            if (pattern.containsMatchIn(defaultValue) && !qualifiedPattern.containsMatchIn(defaultValue)) {
                pattern.findAll(defaultValue).forEach { match ->
                    if (!isQualifiedBefore(defaultValue, match.range.first)) {
                        replacements.add(match.range to "kotlin.math.${match.value}")
                    }
                }
            }
        }
        return replacements
    }

    private fun isQualifiedBefore(defaultValue: String, index: Int): Boolean {
        val before = defaultValue.take(index)
        return before.endsWith(".") || before.endsWith("::")
    }

    private fun applyReplacements(defaultValue: String, replacements: List<Pair<IntRange, String>>): String {
        val sortedReplacements = replacements.sortedByDescending { it.first.first }
        var result = defaultValue
        for ((range, replacement) in sortedReplacements) {
            result = result.replaceRange(range, replacement)
        }
        return result
    }

    // Fallback regex-based extractor when the AST information is not enough.
    // KSP doesn’t always expose the default-value expression
    // it parsed, especially when the parameter belongs to a library or
    // comes from metadata where the AST node isn’t available,
    // we can’t rely solely on the AST-based extractor.
    // In those cases we still want to keep generating the literal default
    // (for imports, signatures, etc.)
    private fun tryExtractSimpleDefault(sourceCode: String, paramName: String): String? {
        val escapedName = Regex.escape(paramName)
        val modifierPattern = """(?:private\s+|public\s+|protected\s+|internal\s+)?"""
        val valVarPattern = """(?:val\s+|var\s+)?"""
        val typePattern = """[^=,)]+"""
        val valuePattern = """([^,\n)]+)"""
        val patternWithModifiers = Regex(
            """$modifierPattern$valVarPattern$escapedName\s*:\s*$typePattern=\s*$valuePattern""",
            RegexOption.MULTILINE,
        )
        val patternSimple = Regex(
            """$escapedName\s*:\s*$typePattern=\s*$valuePattern""",
            RegexOption.MULTILINE,
        )
        val patterns = listOf(patternWithModifiers, patternSimple)
        for (pattern in patterns) {
            val match = pattern.find(sourceCode)
            if (match != null && match.groupValues.size > 1) {
                val value = match.groupValues[1].trim().trimEnd(',', ')')
                if (value.isNotEmpty()) {
                    return value
                }
            }
        }
        return null
    }

    private data class ExtractionData(val sourceCode: String, val startIndex: Int)

    private fun prepareExtractionData(param: KSValueParameter): ExtractionData? {
        val validationResult = validateExtractionPrerequisites(param) ?: return null
        val startIndex = findParameterDefaultStart(validationResult.sourceCode, validationResult.paramName)
        return if (startIndex != null) {
            ExtractionData(validationResult.sourceCode, startIndex)
        } else {
            null
        }
    }

    private data class ValidationResult(val sourceCode: String, val paramName: String)

    private fun validateExtractionPrerequisites(param: KSValueParameter): ValidationResult? {
        val file = param.containingFile
        val paramName = param.name?.asString()
        return if (file != null && paramName != null) {
            readSourceFile(file.filePath)?.let { ValidationResult(it, paramName) }
        } else {
            null
        }
    }

    @Suppress("SwallowedException")
    private fun readSourceFile(filePath: String): String? = try {
        File(filePath).readText()
    } catch (e: IOException) {
        null
    }

    private fun findParameterDefaultStart(sourceCode: String, paramName: String): Int? {
        val escapedName = Regex.escape(paramName)
        val pattern = Regex(
            """(?:private\s+|public\s+|protected\s+|internal\s+)?(?:val\s+|var\s+)?$escapedName\s*:\s*[^=,)]+=\s*""",
            RegexOption.MULTILINE,
        )
        val match = pattern.find(sourceCode) ?: return null
        return match.range.last + 1
    }

    private fun extractBalancedExpression(sourceCode: String, startIndex: Int): String? {
        if (startIndex >= sourceCode.length) return null
        val state = ExtractionState(startIndex)
        val result = StringBuilder()
        while (state.index < sourceCode.length) {
            val char = sourceCode[state.index]
            val shouldContinue = processCharacter(char, sourceCode, state, result)
            if (!shouldContinue) {
                break
            }
            state.index++
        }
        return result.toString().trim().takeIf { it.isNotEmpty() }
    }

    private data class ExtractionState(
        var index: Int,
        var depth: Int = 0,
        var inString: Boolean = false,
        var stringChar: Char? = null,
        val bracketStack: MutableList<Char> = mutableListOf(),
    )

    private fun processCharacter(
        char: Char,
        sourceCode: String,
        state: ExtractionState,
        result: StringBuilder,
    ): Boolean = when {
        handleStringStart(char, state, result) -> true
        handleStringEnd(char, sourceCode, state, result) -> true
        state.inString -> {
            result.append(char)
            true
        }
        handleOpeningBracket(char, state, result) -> true
        handleClosingBracket(char, state, result) -> false
        handleParameterSeparator(char, state) -> false
        else -> {
            result.append(char)
            true
        }
    }

    private fun handleStringStart(char: Char, state: ExtractionState, result: StringBuilder): Boolean {
        if (!state.inString && (char == '"' || char == '\'')) {
            state.inString = true
            state.stringChar = char
            result.append(char)
            return true
        }
        return false
    }

    private fun handleStringEnd(
        char: Char,
        sourceCode: String,
        state: ExtractionState,
        result: StringBuilder,
    ): Boolean {
        if (!state.inString || char != state.stringChar) {
            return false
        }
        val isEscaped = state.index > 0 && sourceCode[state.index - 1] == '\\'
        return if (!isEscaped) {
            state.inString = false
            state.stringChar = null
            result.append(char)
            true
        } else {
            false
        }
    }

    private fun handleOpeningBracket(char: Char, state: ExtractionState, result: StringBuilder): Boolean {
        val bracketType = when (char) {
            '(' -> '('
            '[' -> '['
            '{' -> '{'
            else -> return false
        }
        state.depth++
        state.bracketStack.add(bracketType)
        result.append(char)
        return true
    }

    private fun handleClosingBracket(char: Char, state: ExtractionState, result: StringBuilder): Boolean {
        if (state.depth == 0) {
            return true
        }
        val matchingBracket = when (char) {
            ')' -> '('
            ']' -> '['
            '}' -> '{'
            else -> null
        }
        val isValidMatch = matchingBracket != null &&
            state.bracketStack.isNotEmpty() &&
            state.bracketStack.last() == matchingBracket
        return if (isValidMatch) {
            state.bracketStack.removeAt(state.bracketStack.size - 1)
            state.depth--
            result.append(char)
            false
        } else {
            true
        }
    }

    private fun handleParameterSeparator(char: Char, state: ExtractionState): Boolean = char == ',' && state.depth == 0
}
