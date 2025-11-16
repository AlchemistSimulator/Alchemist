package it.unibo.alchemist.boundary.dsl.processor

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeReference

/**
 * Checks type hierarchies using KSP's type resolution instead of string matching.
 * This provides more accurate type detection by checking actual type relationships.
 */
object TypeHierarchyChecker {
    /**
     * Checks if a type implements or extends a target type by qualified name.
     *
     * @param type The type to check
     * @param targetQualifiedName The fully qualified name of the target type
     * @return True if the type implements or extends the target type
     */
    fun isAssignableTo(type: KSType, targetQualifiedName: String): Boolean {
        val declaration = type.declaration
        val typeQualifiedName = declaration.qualifiedName?.asString()

        if (typeQualifiedName == targetQualifiedName) {
            return true
        }

        return declaration is KSClassDeclaration && checkSuperTypes(declaration, targetQualifiedName, mutableSetOf())
    }

    /**
     * Checks if a type reference resolves to a type that implements or extends a target type.
     *
     * @param typeRef The type reference to check
     * @param targetQualifiedName The fully qualified name of the target type
     * @return True if the type implements or extends the target type
     */
    @Suppress("SwallowedException", "TooGenericExceptionCaught")
    fun isAssignableTo(typeRef: KSTypeReference, targetQualifiedName: String): Boolean = try {
        val resolved = typeRef.resolve()
        isAssignableTo(resolved, targetQualifiedName)
    } catch (e: RuntimeException) {
        false
    }

    private fun checkSuperTypes(
        classDecl: KSClassDeclaration,
        targetQualifiedName: String,
        visited: MutableSet<String>,
    ): Boolean {
        val qualifiedName = classDecl.qualifiedName?.asString() ?: return false

        return when {
            qualifiedName in visited -> false
            qualifiedName == targetQualifiedName -> true
            else -> {
                visited.add(qualifiedName)
                checkSuperTypesRecursive(classDecl, targetQualifiedName, visited)
            }
        }
    }

    private fun checkSuperTypesRecursive(
        classDecl: KSClassDeclaration,
        targetQualifiedName: String,
        visited: MutableSet<String>,
    ): Boolean {
        val superTypes = classDecl.superTypes.toList()
        for (superType in superTypes) {
            if (checkSuperType(superType, targetQualifiedName, visited)) {
                return true
            }
        }
        return false
    }

    @Suppress("SwallowedException", "TooGenericExceptionCaught")
    private fun checkSuperType(
        superType: KSTypeReference,
        targetQualifiedName: String,
        visited: MutableSet<String>,
    ): Boolean = try {
        val resolved = superType.resolve()
        val superDecl = resolved.declaration

        val superQualifiedName = superDecl.qualifiedName?.asString()
        superQualifiedName == targetQualifiedName ||
            (superDecl is KSClassDeclaration && checkSuperTypes(superDecl, targetQualifiedName, visited))
    } catch (e: RuntimeException) {
        false
    }

    /**
     * Checks if a type's qualified name matches any of the provided patterns.
     * Falls back to string matching when type hierarchy checking is not possible.
     *
     * @param type The type to check
     * @param targetQualifiedNames Set of fully qualified names to match against
     * @return True if the type matches any target name
     */
    fun matchesAny(type: KSType, targetQualifiedNames: Set<String>): Boolean {
        val declaration = type.declaration
        val qualifiedName = declaration.qualifiedName?.asString() ?: return false

        return targetQualifiedNames.contains(qualifiedName) ||
            targetQualifiedNames.any { targetName -> isAssignableTo(type, targetName) }
    }

    /**
     * Checks if a type's qualified name matches a pattern or is in a package.
     *
     * @param type The type to check
     * @param targetQualifiedName The target qualified name
     * @param packagePatterns Set of package patterns to check
     * @return True if the type matches
     */
    fun matchesTypeOrPackage(type: KSType, targetQualifiedName: String, packagePatterns: Set<String>): Boolean {
        val declaration = type.declaration
        val qualifiedName = declaration.qualifiedName?.asString() ?: return false

        return qualifiedName == targetQualifiedName ||
            isAssignableTo(type, targetQualifiedName) ||
            packagePatterns.any { pattern -> qualifiedName.startsWith(pattern) }
    }
}
