/**
 * 
 */
package it.unibo.alchemist.loader;

/**
 * This exception is thrown when the Alchemist YAML does not conform to the
 * specification.
 */
public class IllegalAlchemistYAMLException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 0L;

    /**
     * @param reason
     *            a message detailing what's happened
     */
    public IllegalAlchemistYAMLException(final String reason) {
        super(reason);
    }

    /**
     * @param reason
     *            a message detailing what's happened
     * @param cause
     *            the exception that caused this exception from being raised
     */
    public IllegalAlchemistYAMLException(final String reason, final Throwable cause) {
        super(reason, cause);
    }

}
