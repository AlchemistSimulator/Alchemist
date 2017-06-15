package it.unibo.alchemist.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import it.unibo.alchemist.boundary.gui.effects.DrawColoredDot;
import it.unibo.alchemist.boundary.gui.effects.DrawDot;
import it.unibo.alchemist.boundary.gui.effects.EffectFX;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

public class DragboardTest {

    @Test
    public void test() {
        final List<EffectFX> effects = new ArrayList<>();

        final DrawDot drawDot = new DrawDot("First effect");
        drawDot.setSize(10.0);

        final DrawColoredDot drawColoredDot = new DrawColoredDot();
        drawColoredDot.setName("Second effect");
        drawColoredDot.setSize(4.0);
        
        effects.add(drawDot);
        effects.add(drawColoredDot);
        
//        final Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
//        final ClipboardContent content = new ClipboardContent();
//        content.put(getDataFormat(), getItem());
//        dragboard.setDragView(this.snapshot(null, null));
//        dragboard.setContent(content);
        
        
    }

}
