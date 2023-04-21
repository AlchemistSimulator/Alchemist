package it.unibo.alchemist.boundary.gui.effects.json;

import com.google.common.base.Charsets;
import com.google.gson.reflect.TypeToken;
import it.unibo.alchemist.boundary.fxui.effects.serialization.impl.EffectSerializer;
import it.unibo.alchemist.boundary.fxui.effects.impl.DrawColoredDot;
import it.unibo.alchemist.boundary.fxui.effects.impl.DrawDot;
import it.unibo.alchemist.boundary.fxui.effects.api.EffectFX;
import it.unibo.alchemist.boundary.fxui.effects.api.EffectGroup;
import it.unibo.alchemist.boundary.fxui.effects.impl.EffectStack;
import it.unibo.alchemist.model.Position2D;
import it.unibo.alchemist.test.TemporaryFile;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * JUnit test for EffectSerializer class.
 */
class EffectSerializerTest {
    private static final String TEST_EFFECTS = "it/unibo/alchemist/gui/effects/json/TestEffects.json";
    private static final double TEST_SIZE = 6.0;
    private static final double TEST_SIZE_LIST = 7.0;

    /**
     * Tests methods {@link EffectSerializer#effectGroupsFromFile(File)} and
     * {@link EffectSerializer#effectGroupsToFile(File, List)}.
     *
     * @throws IOException if something goes wrong
     */
    @Test
    void testMultipleEffectGroupsSerialization() throws IOException {
        final File file = TemporaryFile.create();
        final List<EffectGroup<Position2D<? extends Position2D<?>>>> groups = initList();
        EffectSerializer.effectGroupsToFile(file, groups);
        final List<EffectGroup<Position2D<? extends Position2D<?>>>> deserialized = EffectSerializer.effectGroupsFromFile(file);
        assertEquals(groups, deserialized);
    }

    /**
     * Tests serialization of a list of {@link EffectFX effect}s.
     *
     * @throws IOException if something goes wrong
     */
    @Test
    void testListOfEffectSerialization() throws IOException {
        final File file = TemporaryFile.create();
        final Type type = new TypeToken<List<EffectFX<Position2D<? extends Position2D<?>>>>>() {
        }.getType();
        final List<EffectFX<Position2D<? extends Position2D<?>>>> effects = new ArrayList<>();
        effects.add(new DrawDot<>());
        effects.add(new DrawColoredDot<>("Test"));
        try (Writer writer = new FileWriter(file, Charsets.UTF_8)) {
            EffectSerializer.getGSON().toJson(effects, type, writer);
        }
        List<EffectFX<Position2D<? extends Position2D<?>>>> deserialized;
        try (Reader reader = new FileReader(file, Charsets.UTF_8)) {
            deserialized = EffectSerializer.getGSON().fromJson(reader, type);
        }
        assertEquals(effects, deserialized);
    }

    /**
     * Initializes and returns a list of {@link EffectGroup}s.
     *
     * @return a list of {@code EffectGroups}
     */
    private List<EffectGroup<Position2D<? extends Position2D<?>>>> initList() {
        final List<EffectGroup<Position2D<? extends Position2D<?>>>> groups = new ArrayList<>();
        groups.add(new EffectStack<>());
        groups.add(new EffectStack<>("Group 2"));
        final EffectGroup<Position2D<? extends Position2D<?>>> group3 = new EffectStack<>("Group 3");
        group3.setVisibility(false);
        groups.add(group3);
        final EffectGroup<Position2D<? extends Position2D<?>>> group4 = new EffectStack<>();
        group4.add(new DrawDot<>());
        group4.add(new DrawDot<>("TestDot"));
        groups.add(group4);
        final EffectGroup<Position2D<? extends Position2D<?>>> group5 = new EffectStack<>("Group 5");
        final DrawDot<Position2D<? extends Position2D<?>>> dot = new DrawDot<>("Dot 2");
        dot.setSize(TEST_SIZE_LIST);
        group5.add(dot);
        final DrawColoredDot<Position2D<?>> colorDot = new DrawColoredDot<>();
        colorDot.setColor(Color.ORANGE);
        colorDot.setVisibility(false);
        group5.add(colorDot);
        group5.setVisibility(false);
        groups.add(group5);
        return groups;
    }

    /**
     * Tests loading effects from resources.
     * @throws IOException if something goes wrong
     */
    @Test
    void testResourceSerialization() throws IOException {
        final EffectGroup<Position2D<? extends Position2D<?>>> group = new EffectStack<>("Default Effects");
        final DrawDot<Position2D<? extends Position2D<?>>> effect = new DrawDot<>("Draw the dots");
        effect.setSize(TEST_SIZE);
        group.add(effect);
        final EffectGroup<Position2D<? extends Position2D<?>>> deserialized = EffectSerializer.effectsFromResources(TEST_EFFECTS);
        assertEquals(group, deserialized);
        final File file = TemporaryFile.create();
        EffectSerializer.effectsToFile(file, group);
        assertEquals(deserialized, EffectSerializer.effectsFromFile(file));
    }
}
