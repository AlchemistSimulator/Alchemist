package it.unibo.alchemist.boundary.dsl.processor

import com.google.devtools.ksp.symbol.KSClassDeclaration
import java.io.PrintWriter

/**
 * Manages writing import statements for generated DSL code.
 */
internal object ImportManager {
    /**
     * Writes all necessary import statements to the generated code file.
     *
     * @param writer The PrintWriter to write imports to
     * @param defaultValues Default value expressions that may require imports
     * @param classDecl The class declaration being processed
     */
    fun writeImports(writer: PrintWriter, defaultValues: List<String>, classDecl: KSClassDeclaration) {
        val neededImports = DefaultValueAnalyzer.extractNeededImportsFromDefaults(defaultValues, classDecl)
        neededImports.forEach { writer.println(it) }
        writer.println()
    }
}
