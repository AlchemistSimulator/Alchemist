/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.fxui.effects;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.boundary.fxui.EffectFX;
import it.unibo.alchemist.boundary.fxui.util.ResourceLoader;
import it.unibo.alchemist.boundary.fxui.util.SVGImages;
import it.unibo.alchemist.util.ClassPathScanner;
import it.unibo.alchemist.model.Position2D;
import javafx.scene.control.ChoiceDialog;
import javafx.stage.Stage;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Class that lets the user choose the effect from all it can find.
 */
@SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST_OF_RETURN_VALUE", justification = "A ChoiceDialog is always in its own stage")
public class EffectBuilderFX {
    /**
     * Set of available {@link EffectFX effect}s found by reflection.
     */
    @SuppressWarnings("unchecked")
    private static final List<Class<? extends EffectFX<?>>> EFFECTS = ClassPathScanner.subTypesOf(
            (Class<EffectFX<?>>) (Class<?>) EffectFX.class,
            "it.unibo.alchemist"
    );
    private final ChoiceDialog<Class<? extends EffectFX<?>>> dialog;

    /**
     * Default constructor.
     */
    public EffectBuilderFX() {
        dialog = new ChoiceDialog<>(EFFECTS.get(0), EFFECTS);
        ((Stage) dialog.getDialogPane()
                .getScene()
                .getWindow())
                .getIcons()
                .add(SVGImages.getSvgImage(SVGImages.DEFAULT_ALCHEMIST_ICON_PATH));
        dialog.setTitle(ResourceLoader.getStringRes("add_effect_dialog_title"));
        dialog.setHeaderText(ResourceLoader.getStringRes("add_effect_dialog_msg"));
        dialog.setContentText(null);
    }

    /**
     * Asks the user to chose an effect and returns the related Class.
     *
     * @param <P> the position type
     * @param <C> the EffectFX type
     * @return the class of the effect
     */
    @SuppressWarnings("unchecked")
    public <P extends Position2D<? extends P>, C extends EffectFX<P>> Optional<Class<C>> getResult() {
        return dialog.showAndWait().map(it -> (Class<C>) it);
    }

    /**
     * Instantiates the desired effect.
     *
     * @param <P> the position type
     * @param clazz the class of the effect
     * @return the effect instantiated
     */
    public <P extends Position2D<? extends P>> EffectFX<P> instantiateEffect(final Class<? extends EffectFX<P>> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (final InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new IllegalStateException("Could not instantiate the effect", e);
        }
    }

    /**
     * Asks the user to chose an effect and returns a new instance of the
     * desired class.
     * <p>
     * Call this method is the same as calling
     * {@link EffectBuilderFX#getResult()} and {@link #instantiateEffect(Class)}}.
     *
     * @param <P> the position type
     * @return the effect chosen, or null if no effect was chosen
     */
    public <P extends Position2D<? extends P>> EffectFX<P> chooseAndLoad() {
        final Optional<Class<EffectFX<P>>> result = getResult();
        return result.map(this::instantiateEffect).orElse(null);
    }

    /**
     * Gets an unmodifiable view of the effects found during construction.
     *
     * @return the list of effects found
     */
    public List<Class<? extends EffectFX<?>>> getFoundEffects() {
        return Collections.unmodifiableList(EFFECTS);
    }
}
