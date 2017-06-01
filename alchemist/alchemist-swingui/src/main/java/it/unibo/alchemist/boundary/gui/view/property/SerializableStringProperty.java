package it.unibo.alchemist.boundary.gui.view.property;

import java.io.Serializable;

import javafx.beans.property.SimpleStringProperty;

/**
 * {@link SimpleStringProperty} that implements also {@link Serializable}.
 */
public class SerializableStringProperty extends SimpleStringProperty implements Serializable {
    /** Generated Serial Version UID. */
    private static final long serialVersionUID = -5784471079162714675L;

//    @Override
//    public int hashCode() {
//        final int prime = 31;
//        int result = 1;
//        result = prime * result + ((getBean() == null) ? 0 : getBean().hashCode());
//        result = prime * result + ((getValue() == null) ? 0 : getValue().hashCode());
//        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
//        return result;
//    }
//
//    @Override
//    public boolean equals(final Object obj) {
//        if (this == obj) {
//            return true;
//        }
//        if (obj == null) {
//            return false;
//        }
//        if (getClass() != obj.getClass()) {
//            return false;
//        }
//        final SerializableStringProperty other = (SerializableStringProperty) obj;
//        if (getBean() == null) {
//            if (other.getBean() != null) {
//                return false;
//            }
//        } else if (!getBean().equals(other.getBean())) {
//            return false;
//        }
//        if (getValue() == null) {
//            if (other.getValue() != null) {
//                return false;
//            }
//        } else if (!getValue().equals(other.getValue())) {
//            return false;
//        }
//        if (getName() == null) {
//            if (other.getName() != null) {
//                return false;
//            }
//        } else if (!getName().equals(other.getName())) {
//            return false;
//        }
//        return true;
//    }

}
