/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.gui.view.property

import com.google.common.base.Charsets
import com.google.gson.reflect.TypeToken
import it.unibo.alchemist.boundary.fxui.effects.serialization.impl.EffectSerializer
import it.unibo.alchemist.boundary.fxui.properties.internal.PropertyFactory
import it.unibo.alchemist.boundary.fxui.properties.internal.RangedIntegerProperty
import it.unibo.alchemist.boundary.fxui.properties.internal.SerializableBooleanProperty
import it.unibo.alchemist.boundary.fxui.properties.internal.SerializableEnumProperty
import it.unibo.alchemist.boundary.fxui.properties.internal.SerializableStringProperty
import it.unibo.alchemist.boundary.fxui.util.RangedDoubleProperty
import it.unibo.alchemist.test.TemporaryFile.create
import javafx.beans.property.Property
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.FileReader
import java.io.FileWriter
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.lang.reflect.Type

/**
 * A class that exposes functions for testing the serialization of serializable properties.
 * Given a number of [T] instances, it will run a test for java serialization (by using
 * ObjectOutputStream#writeObject) and a test for GSON serialization for every [T] instance.
 */
class PropertySerializationTester<T : Property<E>, E : Any>(
    private val gsonType: Type,
    private val toProperty: Any.() -> T,
    vararg serializables: T,
) {
    private val properties: List<T> = serializables.asList()

    companion object {
        private val GSON = EffectSerializer.getGSON()
    }

    /**
     * Attempts to serialize with the standard java.io method.
     */
    fun testJavaSerialization() {
        val file = create()
        properties.forEach { property ->
            file.outputStream().use { fout ->
                ObjectOutputStream(fout).use { oos ->
                    oos.writeObject(property)
                }
            }
            file.inputStream().use { fin ->
                ObjectInputStream(fin).use { ois ->
                    val deserialized = ois.readObject().toProperty()
                    Assertions.assertEquals(property, deserialized, message(property, deserialized))
                }
            }
        }
    }

    /**
     * Attempts to serialize with GSON.
     */
    fun testGsonSerialization() {
        val file = create()
        properties.forEach { property ->
            FileWriter(file, Charsets.UTF_8).use {
                GSON.toJson(property, gsonType, it)
            }
            FileReader(file, Charsets.UTF_8).use {
                val deserialized = GSON.fromJson<T>(it, gsonType)
                Assertions.assertEquals(property, deserialized, message(property, deserialized))
            }
        }
    }

    private fun <T> message(origin: Property<T>?, deserialized: Property<T>?): String {
        if (origin == null) {
            return "original property is null"
        }
        return if (deserialized == null) {
            "deserialized property is null"
        } else {
            "property \"${origin.name}: ${origin.value}\" " +
                "is different than property \"${deserialized.name}: ${deserialized.value}\""
        }
    }
}

/**
 * Abstract class that allows for testing of serializable properties by overriding the [tester] field.
 * See [PropertySerializationTester].
 */
abstract class PropertySerializationTest {

    protected abstract val tester: PropertySerializationTester<*, *>

    /**
     * Runs [tester]'s Java serialization test.
     */
    @Test
    fun testJavaSerialization() {
        tester.testJavaSerialization()
    }

    /**
     * Runs [tester]'s Json serialization test.
     */
    @Test
    fun testJsonSerialization() {
        tester.testGsonSerialization()
    }
}

/**
 * The [PropertySerializationTest] for [RangedDoubleProperty].
 */
class RangedDoublePropertySerializationTest : PropertySerializationTest() {

    companion object {
        private const val DOUBLE_PROPERTY = "Test double property name"
        private const val DOUBLE_INITIAL_VALUE = 5.0
        private const val DOUBLE_LOWER_BOUND = 0.0
        private const val DOUBLE_UPPER_BOUND = 100.0
        private const val DOUBLE_PERCENT_NAME = "Percent test"
        private const val DOUBLE_PERCENT_INITIAL_VALUE = 33.0
        private const val DOUBLE_COLOR_NAME = "RED"
        private const val DOUBLE_COLOR_INITIAL_VALUE = 0.5
    }

    override val tester = PropertySerializationTester(
        object : TypeToken<RangedDoubleProperty>() {}.type,
        { this as RangedDoubleProperty },
        RangedDoubleProperty(DOUBLE_PROPERTY, DOUBLE_INITIAL_VALUE, DOUBLE_LOWER_BOUND, DOUBLE_UPPER_BOUND),
        PropertyFactory.getFXColorChannelProperty(DOUBLE_COLOR_NAME, DOUBLE_COLOR_INITIAL_VALUE),
        PropertyFactory.getPercentageRangedProperty(DOUBLE_PERCENT_NAME, DOUBLE_PERCENT_INITIAL_VALUE),
    )
}

/**
 * The [PropertySerializationTest] for [RangedIntegerProperty].
 */
class RangedIntegerPropertySerializationTest : PropertySerializationTest() {
    companion object {
        private const val INTEGER_PROPERTY = "Test integer property name"
        private const val INTEGER_INITIAL_VALUE = 5
        private const val INTEGER_LOWER_BOUND = 0
        private const val INTEGER_UPPER_BOUND = 100
    }

    override val tester = PropertySerializationTester(
        object : TypeToken<RangedIntegerProperty>() {}.type,
        { this as RangedIntegerProperty },
        RangedIntegerProperty(
            INTEGER_PROPERTY,
            INTEGER_INITIAL_VALUE,
            INTEGER_LOWER_BOUND,
            INTEGER_UPPER_BOUND,
        ),
    )
}

/**
 * The [PropertySerializationTest] for [SerializableBooleanProperty].
 */
class SerializableBooleanPropertySerializationTest : PropertySerializationTest() {
    companion object {
        private const val BOOLEAN_PROPERTY = "Test boolean property name"
    }

    override val tester = PropertySerializationTester(
        object : TypeToken<SerializableBooleanProperty>() {}.type,
        { this as SerializableBooleanProperty },
        SerializableBooleanProperty(
            BOOLEAN_PROPERTY,
            true,
        ),
        SerializableBooleanProperty(
            BOOLEAN_PROPERTY,
            false,
        ),
    )
}

/**
 * The [PropertySerializationTest] for [SerializableEnumProperty].
 */
class SerializableEnumPropertySerializationTest : PropertySerializationTest() {
    companion object {
        private const val ENUM_PROPERTY = "Test enum property name"
    }

    @Suppress("unused")
    enum class TestEnum {
        FOO, BAR, TEST
    }

    @Suppress("unchecked_cast")
    override val tester = PropertySerializationTester(
        object : TypeToken<SerializableEnumProperty<TestEnum>>() {}.type,
        { this as SerializableEnumProperty<TestEnum> },
        SerializableEnumProperty(
            ENUM_PROPERTY,
            TestEnum.TEST,
        ),
    )
}

/**
 * The [PropertySerializationTest] for [SerializableStringProperty].
 */
class SerializableStringPropertySerializationTest : PropertySerializationTest() {
    companion object {
        private const val STRING_PROPERTY = "Test string property name"
        private const val STRING_INITIAL_VALUE = "Test string property value"
    }

    override val tester = PropertySerializationTester(
        object : TypeToken<SerializableStringProperty>() {}.type,
        { this as SerializableStringProperty },
        SerializableStringProperty(
            STRING_PROPERTY,
            STRING_INITIAL_VALUE,
        ),
    )
}
