package it.unibo.alchemist.boundary.gui.effects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javafx.beans.value.ChangeListener;
import javafx.scene.input.DataFormat;

/**
 * The class models a group of effects, stored as a stack. It can manage
 * priority of visualization and visibility of each effect inside it.
 */
public class EffectStack extends Stack<Effect> implements EffectGroup {
    /** Default generated serial version UID. */
    private static final long serialVersionUID = 5721145068915147074L;
    /** Default IllegalArgumentException message */
    private static final String CANNOT_FIND_EFFECT = "Cannot find the effect in the stack";
    /** Default effect group name */
    private static final String DEFAULT_NAME = "New group";
    private static final DataFormat DATA_FORMAT = new DataFormat(EffectStack.class.getName());

    private final List<Effect> effects;
    private final List<Boolean> visibilities;
    private int topIndex;
    private String name;
    private boolean visibility;
    private int transparency;

    private ChangeListener<Number> transparencyUpdater;
    private ChangeListener<Boolean> visibilityUpdater;
    private ChangeListener<String> nameUpdater;

    /**
     * Default constructor. It creates an empty stack of effects.
     */
    public EffectStack() {
        this.effects = new ArrayList<>();
        this.visibilities = new ArrayList<>();
        this.topIndex = 0;
        this.name = DEFAULT_NAME;
        this.visibility = true;
        this.transparency = 100;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public Effect push(final Effect effect) {
        this.effects.add(effect);
        this.visibilities.add(true);
        this.topIndex++;
        return effect;
    }

    @Override
    public Effect pop() {
        final Effect e = this.effects.get(topIndex);
        this.effects.remove(topIndex);
        this.visibilities.remove(topIndex);
        this.topIndex--;
        return e;
    }

    @Override
    public Effect peek() {
        return this.effects.get(topIndex);
    }

    @Override
    public boolean empty() {
        return effects.isEmpty();
    }

    @Override
    public int search(final Object o) {
        if (o instanceof Effect) {
            return this.search((Effect) o);
        } else {
            return -1;
        }
    }

    @Override
    public int search(final Effect effect) {
        for (int i = topIndex; i >= 0; i--) {
            if (this.effects.get(i).equals(effect)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean getVisibilityOf(final Effect effect) {
        try {
            return this.visibilities.get(this.search(effect));
        } catch (final IndexOutOfBoundsException e) {
            throw new IllegalArgumentException(CANNOT_FIND_EFFECT);
        }
    }

    @Override
    public void setVisibilityOf(final Effect effect, final boolean visibility) {
        try {
            this.visibilities.set(this.search(effect), visibility);
        } catch (final IndexOutOfBoundsException e) {
            throw new IllegalArgumentException(CANNOT_FIND_EFFECT);
        }
    }

    @Override
    public void changePriority(final Effect effect, final int offset) {
        final int currentPos = this.search(effect);
        final int newPos = currentPos + offset;
        final Effect temp = this.effects.get(newPos);
        final boolean tmp = this.visibilities.get(newPos);

        this.visibilities.set(newPos, getVisibilityOf(effect));
        this.effects.set(newPos, effect);

        this.visibilities.set(currentPos, tmp);
        this.effects.set(currentPos, temp);

    }

    @Override
    public boolean isVisible() {
        return this.visibility;
    }

    @Override
    public void setVisibility(final boolean visibility) {
        this.visibility = visibility;
    }

    @Override
    public int getTransparency() {
        return this.transparency;
    }

    @Override
    public void setTransparency(final int transparency) {
        if (transparency >= 0 && transparency <= 100) {
            this.transparency = transparency;
        } else {
            throw new IllegalArgumentException("Invalid transparency value");
        }
    }

    @Override
    public ChangeListener<Number> getTransparencyUpdater() {
        if (transparencyUpdater == null) {
            this.transparencyUpdater = (ChangeListener<Number> & Serializable) (observable, oldValue, newValue) -> {
                this.setTransparency(newValue.intValue());
            };
        }
        return transparencyUpdater;
    }

    @Override
    public ChangeListener<Boolean> getVisibilityUpdater() {
        if (this.visibilityUpdater == null) {
            this.visibilityUpdater = (ChangeListener<Boolean> & Serializable) (observable, oldValue, newValue) -> {
                this.setVisibility(newValue);
            };
        }
        return visibilityUpdater;
    }

    @Override
    public ChangeListener<String> getNameUpdater() {
        if (this.nameUpdater == null) {
            this.nameUpdater = (ChangeListener<String> & Serializable) (observable, oldValue, newValue) -> {
                this.setName(newValue);
            };
        }
        return nameUpdater;
    }

    @Override
    public DataFormat getDataFormat() {
        return EffectStack.DATA_FORMAT;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((effects == null) ? 0 : effects.hashCode());
        result = prime * result + topIndex;
        result = prime * result + ((visibilities == null) ? 0 : visibilities.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final EffectStack other = (EffectStack) obj;
        if (effects == null) {
            if (other.effects != null) {
                return false;
            }
        } else if (!effects.equals(other.effects)) {
            return false;
        }
        if (topIndex != other.topIndex) {
            return false;
        }
        if (visibilities == null) {
            if (other.visibilities != null) {
                return false;
            }
        } else if (!visibilities.equals(other.visibilities)) {
            return false;
        }
        return true;
    }
}
