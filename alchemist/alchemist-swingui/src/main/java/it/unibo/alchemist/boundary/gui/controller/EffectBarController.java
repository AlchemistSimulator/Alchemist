package it.unibo.alchemist.boundary.gui.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDrawer;
import com.jfoenix.controls.JFXDrawersStack;
import it.unibo.alchemist.boundary.gui.effects.EffectBuilderFX;
import it.unibo.alchemist.boundary.gui.effects.EffectFX;
import it.unibo.alchemist.boundary.gui.effects.EffectGroup;
import it.unibo.alchemist.boundary.gui.utility.FXResourceLoader;
import it.unibo.alchemist.boundary.gui.utility.SVGImageUtils;
import it.unibo.alchemist.boundary.gui.view.cells.EffectCell;
import it.unibo.alchemist.boundary.gui.view.cells.EffectGroupCell;
import it.unibo.alchemist.boundary.interfaces.FXOutputMonitor;
import it.unibo.alchemist.boundary.interfaces.OutputMonitor;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import jiconfont.icons.GoogleMaterialDesignIcons;
import org.jetbrains.annotations.Nullable;

import static it.unibo.alchemist.boundary.gui.utility.ResourceLoader.getStringRes;

/**
 * This class models a JavaFX controller for EffectBar.fxml.
 */
public class EffectBarController implements Initializable {
    /**
     * Layout path.
     */
    public static final String EFFECT_BAR_LAYOUT = "EffectBar";
    private final JFXDrawersStack stack;
    private final JFXDrawer thisDrawer;
    private final EffectGroupCell parentCell;
    @FXML
    @Nullable
    private ButtonBar topBar; // Value injected by FXMLLoader
    @FXML
    @Nullable
    private JFXButton addEffect;
    @FXML
    @Nullable
    private ListView<EffectFX> effectsList;
    @FXML
    @Nullable
    private Label groupName;
    @FXML
    @Nullable
    private JFXButton backToGroups;
    private ObservableList<EffectFX> observableList;
    private EffectBuilderFX effectBuilder;
    private Optional<FXOutputMonitor<?, ?>> displayMonitor = Optional.empty();

    /**
     * Default constructor.
     *
     * @param parentCell the cell that {@link EffectGroupCell} that will open this
     *                   drawer
     * @param stack      the stack where to open the effect properties
     * @param thisDrawer the drawer the layout this controller is assigned to is loaded
     *                   into
     */
    public EffectBarController(final EffectGroupCell parentCell, final JFXDrawersStack stack, final JFXDrawer thisDrawer) {
        this.stack = stack;
        this.thisDrawer = thisDrawer;
        this.parentCell = parentCell;
    }

    /**
     * Constructor.
     *
     * @param displayMonitor the graphical {@link OutputMonitor}
     * @param parentCell     the cell that {@link EffectGroupCell} that will open this
     *                       drawer
     * @param stack          the stack where to open the effect properties
     * @param thisDrawer     the drawer the layout this controller is assigned to is loaded
     *                       into
     */
    public EffectBarController(final @Nullable FXOutputMonitor<?, ?> displayMonitor, final EffectGroupCell parentCell, final JFXDrawersStack stack, final JFXDrawer thisDrawer) {
        this(parentCell, stack, thisDrawer);
        setDisplayMonitor(displayMonitor);
    }

    /**
     * Getter method for the graphical {@link OutputMonitor}.
     *
     * @return the graphical {@link OutputMonitor}, if any
     */
    public final Optional<FXOutputMonitor<?, ?>> getDisplayMonitor() {
        return displayMonitor;
    }

    /**
     * Setter method for the graphical {@link OutputMonitor}.
     *
     * @param displayMonitor the graphical {@link OutputMonitor} to set; if null, it will be {@link Optional#empty() unset}
     */
    public final void setDisplayMonitor(final @Nullable FXOutputMonitor<?, ?> displayMonitor) {
        this.displayMonitor = Optional.ofNullable(displayMonitor);
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        assert topBar != null : FXResourceLoader.getInjectionErrorMessage("topBar", EFFECT_BAR_LAYOUT);
        assert addEffect != null : FXResourceLoader.getInjectionErrorMessage("add", EFFECT_BAR_LAYOUT);
        assert effectsList != null : FXResourceLoader.getInjectionErrorMessage("effectsList", EFFECT_BAR_LAYOUT);
        assert groupName != null : FXResourceLoader.getInjectionErrorMessage("groupName", EFFECT_BAR_LAYOUT);
        assert backToGroups != null : FXResourceLoader.getInjectionErrorMessage("backToGroups", EFFECT_BAR_LAYOUT);

        this.addEffect.setText("");
        this.addEffect.setGraphic(FXResourceLoader.getWhiteIcon(GoogleMaterialDesignIcons.ADD));

        this.backToGroups.setText("");
        this.backToGroups.setGraphic(FXResourceLoader.getWhiteIcon(GoogleMaterialDesignIcons.ARROW_BACK));

        this.effectBuilder = new EffectBuilderFX();

        this.addEffect.setOnAction(e -> addEffectToList());

        this.backToGroups.setOnAction(e -> this.stack.toggle(thisDrawer));

        this.groupName.setOnMouseClicked(click -> {
            if (click.getClickCount() == 2) {
                final Object source = click.getSource();
                final Label label;

                if (source instanceof Label) {
                    label = (Label) source;
                } else {
                    throw new IllegalStateException("EventHandler for label rename not associated to a label");
                }

                final TextInputDialog dialog = new TextInputDialog(label.getText());
                dialog.setTitle(getStringRes("rename_group_dialog_title"));
                dialog.setHeaderText(getStringRes("rename_group_dialog_msg"));
                dialog.setContentText(null);
                ((Stage) dialog.getDialogPane()
                        .getScene()
                        .getWindow())
                        .getIcons()
                        .add(SVGImageUtils.getSvgImage(SVGImageUtils.DEFAULT_ALCHEMIST_ICON_PATH));

                dialog.showAndWait().ifPresent(s -> Platform.runLater(() -> label.setText(s)));
            }
        });

        this.topBar.widthProperty().addListener((observable, oldValue, newValue) -> this.groupName.setPrefWidth(newValue.doubleValue()));
    }

    /**
     * Add the {@link EffectFX Effect} to the {@link ListView} controlled by this class and to the {@link EffectGroup} that the GUI controlled by this claass is representation of.
     *
     * @param effect the effect to add
     */
    public void addEffectToGroup(final EffectFX effect) {
        this.getObservableList().add(effect);
        this.parentCell.getItem().add(effect);
        if (this.effectsList != null) {
            this.effectsList.refresh();
        }
    }

    /**
     * Opens a {@link Dialog}, and when user choose an {@link EffectFX effect},
     * adds it to the {@link ObservableList list}.
     */
    private void addEffectToList() {
        final EffectFX choice = effectBuilder.chooseAndLoad();
        if (choice != null) {
            this.addEffectToGroup(choice);
            this.getObservableList().get(this.getObservableList().size() - 1).setName(choice.getName() + " " + getObservableList().size());
        }
    }

    /**
     * Getter method and lazy initializer for the internal
     * {@link ObservableList}.
     *
     * @return the {@code ObservableList} associated to the controlled
     * {@link ListView}
     */
    private ObservableList<EffectFX> getObservableList() {
        if (this.observableList == null) {
            this.observableList = FXCollections.observableArrayList();
            if (this.effectsList != null) {
            this.effectsList.setItems(observableList);
                this.effectsList.setCellFactory(lv -> {
                    if (getDisplayMonitor().isPresent()) {
                        return new EffectCell(getDisplayMonitor().get(), this.stack);
                    } else {
                        return new EffectCell(this.stack);
                    }
                });
            }
        }
        return this.observableList;
    }

    /**
     * The name property of this representation of the group.
     *
     * @return the name property
     */
    public StringProperty groupNameProperty() {
        assert this.groupName != null;
        return this.groupName.textProperty();
    }
}
