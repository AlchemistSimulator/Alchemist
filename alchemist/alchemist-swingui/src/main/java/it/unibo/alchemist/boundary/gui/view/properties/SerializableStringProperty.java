package it.unibo.alchemist.boundary.gui.view.properties;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringPropertyBase;

/**
 * {@link SimpleStringProperty} that implements also {@link Serializable}.
 */
public class SerializableStringProperty extends StringPropertyBase implements Serializable {
    /** Generated Serial Version UID. */
    private static final long serialVersionUID = -3684192876864701055L;

    private String name;

    /**
     * The constructor of {@code SimpleStringProperty}.
     */
    public SerializableStringProperty() {
        super();
        this.name = "";
    }

    /**
     * The constructor of {@code SimpleStringProperty}.
     *
     * @param initialValue
     *            the initial value of the wrapped value
     */
    public SerializableStringProperty(final String initialValue) {
        this();
        this.setValue(initialValue);
    }

    /**
     * The constructor of {@code SimpleStringProperty}.
     *
     * @param name
     *            the name of this {@code SimpleStringProperty}
     * @param initialValue
     *            the initial value of the wrapped value
     */
    public SerializableStringProperty(final String name, final String initialValue) {
        this();
        this.name = name;
        this.setValue(initialValue);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getValue() == null) ? 0 : getValue().hashCode());
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SerializableStringProperty other = (SerializableStringProperty) obj;
        if (getValue() == null) {
            if (other.getValue() != null) {
                return false;
            }
        } else if (!getValue().equals(other.getValue())) {
            return false;
        }
        if (getName() == null) {
            if (other.getName() != null) {
                return false;
            }
        } else if (!getName().equals(other.getName())) {
            return false;
        }
        return true;
    }

    @Override
    public Object getBean() {
        return null;
    }

    /**
     * Getter method for the name.
     * 
     * @return the name to give to the property
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Setter method for the name.
     * 
     * @param name
     *            the name to give to the property
     */
    public void setName(final String name) {
        this.name = name;
    }

    private void writeObject(final ObjectOutputStream out) throws IOException {
        out.writeUTF(this.getName());
        out.writeUTF(this.getName());
    }

    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.setName(in.readUTF());
        this.setValue(in.readUTF());
    }

}
