package it.unibo.alchemist.boundary.gui.view.properties

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import it.unibo.alchemist.boundary.gui.view.properties.PropertyTypeAdapter.NAME
import it.unibo.alchemist.boundary.gui.view.properties.PropertyTypeAdapter.VALUE
import javafx.beans.property.DoublePropertyBase
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.lang.reflect.Type

private const val LOWER_BOUND = "lower bound"
private const val UPPER_BOUND = "upper bound"

open class RangedDoubleProperty @JvmOverloads constructor(private var name: String,
                           initialValue: Double = 0.0,
                           lowerBound: Double = -Double.MAX_VALUE,
                           upperBound: Double = Double.MAX_VALUE) : DoublePropertyBase(initialValue), Serializable {

    var lowerBound: Double = lowerBound
        private set
    var upperBound: Double = upperBound
        private set

    override fun set(newValue: Double) = when {
        newValue < lowerBound -> throw IllegalArgumentException("Provided value is bigger than the upper bound")
        newValue > upperBound -> throw IllegalArgumentException("Provided value is smaller than the lower bound")
        else -> super.set(newValue)
    }

    override fun setValue(v: Number) {
        set(v.toDouble())
    }

    override fun getBean() = null

    override fun getName() = name

    private fun writeObject(out: ObjectOutputStream) {
        out.writeUTF(name)
        out.writeDouble(lowerBound)
        out.writeDouble(upperBound)
        out.writeDouble(value)
    }

    private fun readObject(input: ObjectInputStream) {
        name = input.readUTF()
        lowerBound = input.readDouble()
        upperBound = input.readDouble()
        value = input.readDouble()
    }

    override fun hashCode() =
            lowerBound.hashCode() xor upperBound.hashCode() xor value.hashCode() xor (name.hashCode() ?: 0)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as RangedDoubleProperty
        if (name != other.name) return false
        if (lowerBound != other.lowerBound) return false
        if (upperBound != other.upperBound) return false
        return true
    }

    companion object {
        @JvmStatic fun getTypeAdapter() : PropertyTypeAdapter<RangedDoubleProperty> {
            return object : PropertyTypeAdapter<RangedDoubleProperty> {

                override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): RangedDoubleProperty {
                    val jObj = json as JsonObject
                    return RangedDoubleProperty(
                            jObj.get(NAME).asString,
                            jObj.get(VALUE).asDouble,
                            jObj.get(LOWER_BOUND).asDouble,
                            jObj.get(UPPER_BOUND).asDouble)
                }

                override fun serialize(src: RangedDoubleProperty, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
                    val jObj = JsonObject()
                    jObj.addProperty(NAME, src.name)
                    jObj.addProperty(VALUE, src.value)
                    jObj.addProperty(LOWER_BOUND, src.lowerBound)
                    jObj.addProperty(UPPER_BOUND, src.upperBound)
                    return jObj
                }
            }
        }
    }
}