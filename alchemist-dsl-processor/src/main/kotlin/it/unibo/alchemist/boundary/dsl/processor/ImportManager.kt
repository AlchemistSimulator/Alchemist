package it.unibo.alchemist.boundary.dsl.processor

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import java.io.PrintWriter

/**
 * Manages writing import statements for generated DSL code.
 */
object ImportManager {
    /**
     * Writes all necessary import statements to the generated code file.
     *
     * @param writer The PrintWriter to write imports to
     * @param typeParamBounds Type parameter bounds that may require imports
     * @param paramTypes Parameter types that may require imports
     * @param defaultValues Default value expressions that may require imports
     * @param classDecl The class declaration being processed
     * @param needsMapEnvironment Whether MapEnvironment import is needed
     * @param injectedParamTypes Types of injected parameters that may require imports
     */
    fun writeImports(
        writer: PrintWriter,
        typeParamBounds: List<String>,
        paramTypes: List<String>,
        defaultValues: List<String>,
        classDecl: KSClassDeclaration,
        needsMapEnvironment: Boolean = false,
        injectedParamTypes: List<String> = emptyList(),
    ) {
        val neededImports = DefaultValueAnalyzer.extractNeededImportsFromDefaults(defaultValues, classDecl)
        neededImports.forEach { writer.println(it) }

        writer.println()
    }
}
