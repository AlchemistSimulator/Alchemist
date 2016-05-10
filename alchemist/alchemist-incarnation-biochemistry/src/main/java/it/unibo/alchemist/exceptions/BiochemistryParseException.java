package it.unibo.alchemist.exceptions;

/**
 * Represents an exception thrown when parse errors are encountered. 
 */
public class BiochemistryParseException extends RuntimeException {

    private static final long serialVersionUID = -5287091656680353238L;

    /**
     * Construct the exception with the given message.
     * @param message the error message.
     */
    public BiochemistryParseException(final String message) {
        super(message);
    }
}
