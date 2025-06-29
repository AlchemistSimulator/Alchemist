import it.unibo.alchemist.boundary.webui.common.model.surrogate.MoleculeSurrogate
import it.unibo.alchemist.boundary.webui.common.model.surrogate.NodeSurrogate
import it.unibo.alchemist.boundary.webui.common.model.surrogate.Position2DSurrogate
import it.unibo.alchemist.boundary.webui.common.model.surrogate.PositionSurrogate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSerializationApi::class)
class NodeSurrogateTest {

    private val mapping = mapOf(
        MoleculeSurrogate("test-0") to 0,
        MoleculeSurrogate("test-1") to 1,
    )

    private val nodePosition = Position2DSurrogate(5.6, 1.2)

    private val nodeSurrogate = NodeSurrogate(
        id = 29,
        contents = mapping,
        position = nodePosition,
    )

    @Test
    fun `node surrogates should have the correct id`() {
        assertEquals(29, nodeSurrogate.id, "NodeSurrogate.id should match constructor argument")
    }

    @Test
    fun `node surrogates should have the correct contents`() {
        assertEquals(0, nodeSurrogate.contents[MoleculeSurrogate("test-0")], "Content for 'test-0' should be 0")
        assertEquals(1, nodeSurrogate.contents[MoleculeSurrogate("test-1")], "Content for 'test-1' should be 1")
        assertEquals(2, nodeSurrogate.contents.size, "Contents map size should be 2")
    }

    @Test
    fun `node surrogates should have the correct position`() {
        assertEquals(nodePosition, nodeSurrogate.position, "NodeSurrogate.position should match constructor argument")
    }

    @Test
    fun `node surrogates should serialize and deserialize correctly`() {
        val descriptorName = NodeSurrogate
            .serializer(Int.serializer(), PositionSurrogate.serializer())
            .descriptor
            .serialName
        assertEquals("Node", descriptorName, "Serializer serialName should be 'Node'")
        val serialized = Json.encodeToString(nodeSurrogate)
        val deserialized: NodeSurrogate<Int, Position2DSurrogate> =
            Json.decodeFromString(serialized)
        assertEquals(nodeSurrogate, deserialized, "Deserialized surrogate should equal the original")
    }
}
