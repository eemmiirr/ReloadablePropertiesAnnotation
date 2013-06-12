package com.morgan.design.properties.exception;

/**
 * Exception which is thrown when the property is not found.
 *
 * @author Emir Dizdarevic
 * @since 6/12/13
 */
public class PropertyNotFoundException extends RuntimeException {

    private final String property;

    public PropertyNotFoundException(String property) {
        super("Property " + property + " not found.");
        this.property = property;
    }

    public PropertyNotFoundException(Throwable cause, String property) {
        super("Property " + property + " not found.", cause);
        this.property = property;
    }

    public String getProperty() {
        return property;
    }
}
