package com.morgan.design.properties.conversion;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.Maps;
import com.morgan.design.util.JodaUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Default implementation of {@link PropertyConversionService}, attempting to convert an object otherwise utilising {@link SimpleTypeConverter} if no matching
 * converter is found.
 * 
 * @author James Morgan
 */
@Component
public class DefaultPropertyConversionService implements PropertyConversionService {

    private static final ExpressionParser expressionParser = new SpelExpressionParser();

	private static Map<Class<? extends Object>, Function<Object, ?>> CONVERTS = Maps.newHashMap();
	static {
		CONVERTS.put(Period.class, new PeriodConverter());
		CONVERTS.put(LocalDateTime.class, new LocalDateTimeConverter());
		CONVERTS.put(LocalDate.class, new LocalDateConverter());
		CONVERTS.put(LocalTime.class, new LocalTimeConverter());
	}

	private static SimpleTypeConverter DEFAULT = new SimpleTypeConverter();

	@Override
	public Object convertPropertyForField(final Class<?> type, final String property) throws Throwable {
        return Functions.forMap(CONVERTS, new DefaultConverter(type))
                .apply(type)
                .apply(parseSPELIfPresent(property));
    }

    private Object parseSPELIfPresent(String property) {

        final Object parsedProperty;
        if (property.startsWith("#{") && property.endsWith("}")) parsedProperty = expressionParser.parseExpression(property.substring(2, property.length() - 2).trim()).getValue();
        else parsedProperty = property;

        return parsedProperty;
    }

	private static class DefaultConverter implements Function<Object, Object> {
		private final Class<?> type;

		public DefaultConverter(final Class<?> type) {
			this.type = type;
		}

		@Override
		public Object apply(final Object input) {
			return DEFAULT.convertIfNecessary(input, this.type);
		}
	}

	private static class PeriodConverter implements Function<Object, Period> {
		@Override
		public Period apply(final Object input) {
			return JodaUtils.timeStringToPeriodOrNull((String) input);
		}
	}

	private static class LocalDateTimeConverter implements Function<Object, LocalDateTime> {
		@Override
		public LocalDateTime apply(final Object input) {
			return JodaUtils.timestampStringToLocalDateTimeOrNull((String) input);
		}
	}

	private static class LocalDateConverter implements Function<Object, LocalDate> {
		@Override
		public LocalDate apply(final Object input) {
			return JodaUtils.dateStringToLocalDateOrNull((String) input);
		}
	}

	private static class LocalTimeConverter implements Function<Object, LocalTime> {
		@Override
		public LocalTime apply(final Object input) {
			return JodaUtils.timeStringToLocalTimeOrNull((String) input);
		}
	}
}
