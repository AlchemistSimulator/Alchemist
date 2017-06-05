package it.unibo.alchemist.boundary.gui.view.property;

import java.io.Serializable;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;

/**
 * {@link Property} designed to wrap an {@link Enum enum}.
 * <p>
 * It is based on {@code SimpleObjectProperty} and is {@code Serializable}.
 * 
 * @param <T>
 *            the enumeration wrapped
 */
public class EnumProperty<T extends Enum<?>> extends SimpleObjectProperty<T> implements Serializable {
    /** Generated Serial Version UID. */
    private static final long serialVersionUID = 1511564747464877877L;

    // @Override
    // public int hashCode() {
    // final int prime = 31;
    // int result = 1;
    // result = prime * result + ((this.getBean() == null) ? 0 :
    // this.getBean().hashCode());
    // result = prime * result + ((this.getValue() == null) ? 0 :
    // this.getValue().hashCode());
    // result = prime * result + ((this.getName() == null) ? 0 :
    // this.getName().hashCode());
    // return result;
    // }
    //
    // @Override
    // public boolean equals(final Object obj) {
    // if (this == obj) {
    // return true;
    // }
    // if (obj == null) {
    // return false;
    // }
    // if (getClass() != obj.getClass()) {
    // return false;
    // }
    // final EnumProperty<?> other = (EnumProperty<?>) obj;
    // if (this.getBean() == null) {
    // if (other.getBean() != null) {
    // return false;
    // }
    // } else if (!this.getBean().equals(other.getBean())) {
    // return false;
    // }
    // if (this.getValue() == null) {
    // if (other.getValue() != null) {
    // return false;
    // }
    // } else if (!this.getValue().equals(other.getValue())) {
    // return false;
    // }
    // if (this.getName() == null) {
    // if (other.getName() != null) {
    // return false;
    // }
    // } else if (!getName().equals(other.getName())) {
    // return false;
    // }
    // return true;
    // }
}
