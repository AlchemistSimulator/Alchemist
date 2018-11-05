package it.unibo.alchemist.boundary.gui.effects;

import it.unibo.alchemist.boundary.gui.utility.ResourceLoader;
import it.unibo.alchemist.boundary.gui.utility.SVGImageUtils;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.scene.control.ChoiceDialog;
import javafx.stage.Stage;
import org.reflections.Reflections;

/**
 * Class that lets the user choose the effect from all it can find.
 */
public class EffectBuilderFX {
    /**
     * Reflection object for main Alchemist package.
     */
    private static final Reflections REFLECTIONS = new Reflections("it.unibo.alchemist");
    /**
     * Set of available {@link EffectFX effect}s found by reflection.
     */
    private static final Set<Class<? extends EffectFX>> EFFECTS = REFLECTIONS.getSubTypesOf(EffectFX.class)
            .stream()
            .filter(c -> !Modifier.isAbstract(c.getModifiers()))
            .collect(Collectors.toSet());
    private final List<Class<? extends EffectFX>> effects;
    private final ChoiceDialog<Class<? extends EffectFX>> dialog;

    /**
     * Default constructor.
     */
    public EffectBuilderFX() {
        effects = new ArrayList<>(EFFECTS);
        dialog = new ChoiceDialog<>(effects.get(0), effects);
        ((Stage) dialog.getDialogPane()
                .getScene()
                .getWindow())
                .getIcons()
                .add(SVGImageUtils.getSvgImage(SVGImageUtils.DEFAULT_ALCHEMIST_ICON_PATH));
        dialog.setTitle(ResourceLoader.getStringRes("add_effect_dialog_title"));
        dialog.setHeaderText(ResourceLoader.getStringRes("add_effect_dialog_msg"));
        dialog.setContentText(null);
    }

    /**
     * Asks the user to chose an effect and returns the related Class.
     *
     * @return the class of the effect
     */
    public Optional<Class<? extends EffectFX>> getResult() {
        return dialog.showAndWait();
    }

    /**
     * Instantiates the desired effect.
     *
     * @param clazz the class of the effect
     * @return the effect instantiated
     */
    public EffectFX instantiateEffect(final Class<? extends EffectFX> clazz) {
        try {
            return clazz.newInstance();
        } catch (final InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException("Could not instantiate the effect", e);
        }
    }

    /**
     * Asks the user to chose an effect and returns a new instance of the
     * desired class.
     * <p>
     * Call this method is the same as calling
     * {@link EffectBuilderFX#getResult()} and {@link #chooseAndLoad()}.
     *
     * @return the effect chosen, or null if no effect was chosen
     */
    public EffectFX chooseAndLoad() {
        final Optional<Class<? extends EffectFX>> result = getResult();
        if (result.isPresent()) {
            return this.instantiateEffect(result.get());
        } else {
            return null;
        }
    }

    /**
     * Gets an unmodifiable view of the effects found during construction.
     *
     * @return the list of effects found
     */
    public List<Class<? extends EffectFX>> getFoundEffects() {
        return Collections.unmodifiableList(this.effects);
    }
}
