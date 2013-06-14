package com.morgan.design.properties.exception;

/**
 * Exception which is thrown when property SPEL can't be parsed.
 *
 * @author Emir Dizdarevic
 * @since 6/12/13
 */
public class PropertyParsingException extends RuntimeException {

    private final String property;

    public PropertyParsingException(String property) {
        super("Property " + property + " not found.");
        this.property = property;
    }

    public PropertyParsingException(Throwable cause, String property) {
        super("Property " + property + " not found.", cause);
        this.property = property;
    }

    public String getProperty() {
        return property;
    }
}
