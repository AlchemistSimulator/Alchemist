package it.unibo.alchemist.model

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class TestTerminationPredicateSerialization {
    @Test
    fun `termination predicates can be serialized and deserialized`() {
        // Create a predicate that checks if the environment has more than 100 nodes
        val originalPredicate: TerminationPredicate<Int, Position<*>> =
            TerminationPredicate { env -> env.nodeCount > 100 }
        // Serialize the predicate
        val serializedBytes = serialize(originalPredicate)
        // Deserialize the predicate
        val deserializedPredicate: TerminationPredicate<Int, Position<*>> =
            deserialize(serializedBytes)
        // Check that the deserialized predicate works as expected
        val mockEnvironment = mockk<Environment<Int, Position<*>>>()
        every { mockEnvironment.nodeCount } returns 101
        assertEquals(originalPredicate(mockEnvironment), deserializedPredicate(mockEnvironment)) {
            "Deserialized predicate should behave the same as the original"
        }
    }

    private fun serialize(obj: Any): ByteArray {
        val outputStream = ByteArrayOutputStream()
        ObjectOutputStream(outputStream).use { it.writeObject(obj) }
        return outputStream.toByteArray()
    }

    private fun <T> deserialize(bytes: ByteArray): T {
        val inputStream = ByteArrayInputStream(bytes)
        return ObjectInputStream(inputStream).use {
            @Suppress("UNCHECKED_CAST")
            it.readObject() as T
        }
    }
}
