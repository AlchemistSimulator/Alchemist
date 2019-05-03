package it.unibo.alchemist.boundary.gui.effects.json;

import it.unibo.alchemist.model.interfaces.Position2D;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.gson.reflect.TypeToken;

import it.unibo.alchemist.boundary.gui.effects.DrawColoredDot;
import it.unibo.alchemist.boundary.gui.effects.DrawDot;
import it.unibo.alchemist.boundary.gui.effects.EffectFX;
import it.unibo.alchemist.boundary.gui.effects.EffectGroup;
import it.unibo.alchemist.boundary.gui.effects.EffectStack;
import javafx.scene.paint.Color;

/**
 * JUnit test for EffectSerializer class.
 */
public class EffectSerializerTest {
    private static final String TEST_EFFECTS = "/it/unibo/alchemist/gui/effects/json/TestEffects.json";
    private static final double TEST_SIZE = 6.0;

    /**
     * Temporary folder created before each test method, and deleted after each.
     */
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    /**
     * Tests methods {@link EffectSerializer#effectGroupsFromFile(File)} and
     * {@link EffectSerializer#effectGroupsToFile(File, List)}.
     *
     * @throws IOException if something goes wrong
     */
    @Test
    public void testMultipleEffectGroupsSerialization() throws IOException {
        final File file = folder.newFile();

        final List<EffectGroup<Position2D<? extends Position2D>>> groups = initList();

        EffectSerializer.effectGroupsToFile(file, groups);

        final List<EffectGroup<Position2D<? extends Position2D>>> deserialized = EffectSerializer.effectGroupsFromFile(file);

        Assert.assertTrue(groups.equals(deserialized));
    }

    /**
     * Tests serialization of a list of {@link EffectFX effect}s.
     *
     * @throws IOException if something goes wrong
     */
    @Test
    public void testListOfEffectSerialization() throws IOException {
        final File file = folder.newFile();
        final Type type = new TypeToken<List<EffectFX<Position2D<? extends Position2D>>>>() {
        }.getType();
        final List<EffectFX<Position2D<? extends Position2D>>> effects = new ArrayList<>();
        effects.add(new DrawDot<>());
        effects.add(new DrawColoredDot("Test"));
        final Writer writer = new FileWriter(file);
        EffectSerializer.getGSON().toJson(effects, type, writer);
        writer.close();
        final Reader reader = new FileReader(file);
        final List<EffectFX<Position2D<? extends Position2D>>> deserialized = EffectSerializer.getGSON().fromJson(reader, type);
        reader.close();
        Assert.assertTrue(effects.equals(deserialized));
    }

    /**
     * Initializes and returns a list of {@link EffectGroup}s.
     *
     * @return a list of {@code EffectGroups}
     */
    private List<EffectGroup<Position2D<? extends Position2D>>> initList() {
        final List<EffectGroup<Position2D<? extends Position2D>>> groups = new ArrayList<>();

        groups.add(new EffectStack<>());
        groups.add(new EffectStack<>("Group 2"));

        final EffectGroup<Position2D<? extends Position2D>> group3 = new EffectStack<>("Group 3");
        group3.setVisibility(false);
        groups.add(group3);

        final EffectGroup<Position2D<? extends Position2D>> group4 = new EffectStack<>();
        group4.add(new DrawDot<>());
        group4.add(new DrawDot<>("TestDot"));
        groups.add(group4);

        final EffectGroup<Position2D<? extends Position2D>> group5 = new EffectStack<>("Group 5");
        final DrawDot<Position2D<? extends Position2D>> dot = new DrawDot<>("Dot 2");
        // CHECKSTYLE:OFF
        dot.setSize(7.0);
        // CHECKSTYLE:ON
        group5.add(dot);
        final DrawColoredDot colorDot = new DrawColoredDot();
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
    public void testResourceSerialization() throws IOException {
        final EffectGroup<Position2D<? extends Position2D>> group = new EffectStack<>("Default Effects");
        final DrawDot<Position2D<? extends Position2D>> effect = new DrawDot<>("Draw the dots");
        effect.setSize(TEST_SIZE);
        group.add(effect);
        final EffectGroup<Position2D<? extends Position2D>> deserialized = EffectSerializer.effectsFromResources(TEST_EFFECTS);
        Assert.assertEquals(group, deserialized);
        final File file = folder.newFile();
        EffectSerializer.effectsToFile(file, group);
        Assert.assertEquals(deserialized, EffectSerializer.effectsFromFile(file));
    }
}
