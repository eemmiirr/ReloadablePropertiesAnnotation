package com.morgan.design.properties.conversion;

/**
 * Interface intended for use by any class willing to convert the given property {@link Object} which potentially requires conversion before being set on the
 * given Field
 * 
 * @author James Morgan
 */
public interface PropertyConversionService {

	/**
	 * @param type the type of the filed to set the property on
	 * @param property the property to be converted for the given field
	 * @return the potentially converted field
	 */
    Object convertPropertyForField(final Class<?> type, final String property) throws Throwable;
}
