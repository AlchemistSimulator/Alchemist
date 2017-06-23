package it.unibo.alchemist.boundary.gui.effects;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Assert;
import org.junit.Test;

import it.unibo.alchemist.boundary.gui.effects.json.EffectSerializer;

/**
 * JUint test for {@link EffectFX effect} serialization.
 */
public class DrawShapeSerializationTest extends AbstractEffectSerializationTest<DrawShapeFX> {

    @Test
    @Override
    public void testJavaSerialization() throws Exception {
        final File file = folder.newFile();

        final FileOutputStream fout = new FileOutputStream(file);
        final ObjectOutputStream oos = new ObjectOutputStream(fout);

        final DrawShapeFX effect = new DrawShapeFX("TestDot");

        oos.writeObject(effect);

        final FileInputStream fin = new FileInputStream(file);
        final ObjectInputStream ois = new ObjectInputStream(fin);

        final DrawShapeFX deserialized = (DrawShapeFX) ois.readObject();

        Assert.assertTrue(getMessage(effect, deserialized), effect.equals(deserialized));

        oos.close();
        ois.close();
    }

    @Test
    @Override
    public void testGsonSerialization() throws Exception {
        final File file = folder.newFile();

        final DrawShapeFX effect = new DrawShapeFX("TestEffect");

        EffectSerializer.effectToFile(file, effect);

        final DrawShapeFX deserialized = (DrawShapeFX) EffectSerializer.effectFromFile(file);

        Assert.assertTrue(getMessage(effect, deserialized), effect.equals(deserialized));
    }

    @Override
    protected String getMessage(final DrawShapeFX origin, final DrawShapeFX deserialized) {
        if (origin == null || deserialized == null) {
            return super.getMessage(origin, deserialized);
        }

        return super.getMessage(origin, deserialized)
                + System.lineSeparator() + "Origin mode: " + origin.getMode()
                + System.lineSeparator() + "Deserialized mode: " + deserialized.getMode()
                + System.lineSeparator() + "Origin size: " + origin.getSize()
                + System.lineSeparator() + "Deserialized size: " + deserialized.getSize()
                + System.lineSeparator() + "Origin alpha: " + origin.getAlpha()
                + System.lineSeparator() + "Deserialized alpha: " + deserialized.getAlpha()
                + System.lineSeparator() + "Origin red: " + origin.getRed()
                + System.lineSeparator() + "Deserialized red: " + deserialized.getRed()
                + System.lineSeparator() + "Origin green: " + origin.getGreen()
                + System.lineSeparator() + "Deserialized green: " + deserialized.getGreen()
                + System.lineSeparator() + "Origin blue: " + origin.getBlue()
                + System.lineSeparator() + "Deserialized blue: " + deserialized.getBlue()
                + System.lineSeparator() + "Origin scale factor: " + origin.getScaleFactor()
                + System.lineSeparator() + "Deserialized scale factor: " + deserialized.getScaleFactor()
                + System.lineSeparator() + "Origin filters by molecule: " + origin.isMoleculeFilter()
                + System.lineSeparator() + "Deserialized filters by molecule: " + deserialized.isMoleculeFilter()
                + System.lineSeparator() + "Origin molecule name: " + origin.getMoleculeName()
                + System.lineSeparator() + "Deserialized molecule name: " + deserialized.getMoleculeName()
                + System.lineSeparator() + "Origin uses molecule property: " + origin.isUseMoleculeProperty()
                + System.lineSeparator() + "Deserialized uses molecule property: " + deserialized.isUseMoleculeProperty()
                + System.lineSeparator() + "Origin molecule property name: " + origin.getMoleculePropertyName()
                + System.lineSeparator() + "Deserialized molecule property name: " + deserialized.getMoleculePropertyName()
                + System.lineSeparator() + "Origin writes property value: " + origin.isWritePropertyValue()
                + System.lineSeparator() + "Deserialized writes property value: " + deserialized.isWritePropertyValue()
                + System.lineSeparator() + "Origin uses color channel: " + origin.getColorChannel()
                + System.lineSeparator() + "Deserialized uses color channel: " + deserialized.getColorChannel()
                + System.lineSeparator() + "Origin reverses effect: " + origin.isReverse()
                + System.lineSeparator() + "Deserialized reverses effect: " + deserialized.isReverse()
                + System.lineSeparator() + "Origin order of magnitude: " + origin.getOrderOfMagnitude()
                + System.lineSeparator() + "Deserialized order of magnitude: " + deserialized.getOrderOfMagnitude()
                + System.lineSeparator() + "Origin minimum property value: " + origin.getMinprop()
                + System.lineSeparator() + "Deserialized minimum property value: " + deserialized.getMinprop()
                + System.lineSeparator() + "Origin maximum property vaule: " + origin.getMaxprop()
                + System.lineSeparator() + "Deserialized maximum property value: " + deserialized.getMaxprop();
    }

}
