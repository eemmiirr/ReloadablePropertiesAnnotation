package com.morgan.design.properties.internal;

/**
 * Interface to access and change properties.
 *
 * @author Emir Dizdarevic
 * @since 6/12/13
 */
public interface PropertyAccessor {

    /**
     * Sets the property to the new value.
     *
     * @param property Name of the property
     * @param value New value of the property
     *
     * @throws com.morgan.design.properties.exception.PropertyNotFoundException If the property is not found
     */
    void setProperty(String property, String newValue);

    /**
     *  Retrieves the property with the given name.
     *
     * @param property Name of the property
     * @return Value of the property
     *
     * @throws com.morgan.design.properties.exception.PropertyNotFoundException If the property is not found
     */
    String getProperty(String property);

    /**
     *  Retrieves the parsed property with the given name.
     *  If it's a Spring EL expression the return value will differ from the getProperty method
     *
     * @param property Name of the property
     * @return Parsed value of the property
     *
     * @throws com.morgan.design.properties.exception.PropertyNotFoundException If the property is not found
     */
    String getParsedProperty(String property);
}
