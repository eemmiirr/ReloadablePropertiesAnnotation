package com.morgan.design.properties.internal;

import com.morgan.design.properties.bean.PropertyModifiedEvent;
import com.morgan.design.properties.conversion.PropertyConversionService;
import com.morgan.design.properties.event.PropertyChangedEventNotifier;
import com.morgan.design.properties.exception.PropertyNotFoundException;
import com.morgan.design.properties.exception.PropertyParsingException;
import com.morgan.design.properties.internal.PropertiesWatcher.EventPublisher;
import com.morgan.design.properties.resolver.PropertyResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Properties;
import java.util.concurrent.Executors;

/**
 * Specialisation of {@link PropertySourcesPlaceholderConfigurer} that can react to changes in the resources specified. The watching process does not start by
 * default, initiation is triggered by calling <code>ReadablePropertySourcesPlaceholderConfigurer.startWatching()</code>
 *
 * @author James Morgan
 */
public class ReadablePropertySourcesPlaceholderConfigurer extends PropertySourcesPlaceholderConfigurer implements EventPublisher, PropertyAccessorMXBean {

    private static final Logger log = LoggerFactory.getLogger(ReadablePropertySourcesPlaceholderConfigurer.class);
    private static final String MXBEAN_NAME = PropertyAccessor.class.getPackage().getName() + ":type="  + PropertyAccessor.class.getSimpleName();

	private final PropertyChangedEventNotifier eventNotifier;
	private final PropertyResolver propertyResolver;
    private final PropertyConversionService propertyConversionService;

	private Properties properties;
	private Resource[] locations;
    private long delay = 10000;

	@Autowired
	public ReadablePropertySourcesPlaceholderConfigurer(final PropertyChangedEventNotifier eventNotifier,
                                                        final PropertyResolver propertyResolver,
                                                        final PropertyConversionService propertyConversionService) {
		this.eventNotifier = eventNotifier;
		this.propertyResolver = propertyResolver;
        this.propertyConversionService = propertyConversionService;
    }

	@Override
	protected void loadProperties(final Properties props) throws IOException {
		super.loadProperties(props);
		this.properties = props;
	}

	@Override
	public void setLocations(final Resource[] locations) {
		super.setLocations(locations);
		this.locations = locations;
	}

	@Override
	public void onResourceChanged(final Resource resource) {
		try {
			final Properties reloadedProperties = PropertiesLoaderUtils.loadProperties(resource);
			for (final String property : this.properties.stringPropertyNames()) {

				final String oldValue = this.properties.getProperty(property);
				final String newValue = reloadedProperties.getProperty(property);

				if (propertyExistsAndNotNull(property, newValue) && propertyChange(oldValue, newValue)) {

                    // Process the property
                    processProperty(oldValue, newValue, property);
				}
			}
		}
		catch (final IOException e) {
			log.error("Failed to reload properties file once change", e);
		}
	}

    @Override
    public void setProperty(String property, String newValue) {

        if(propertyExistsAndNotNull(property, newValue)) {

            final String oldValue = this.properties.getProperty(property);
            if (propertyChange(oldValue, newValue)) {

                // Process the property
                processProperty(oldValue, newValue, property);
            }
        } else {
            log.warn("Failed setting property. Property {} not found.", property);
            throw new PropertyNotFoundException(property);
        }
    }

    @Override
    public String getProperty(String property) {

        if(this.properties.containsKey(property)) {
            return this.properties.getProperty(property);
        } else {
            log.warn("Failed getting property. Property {} not found.", property);
            throw new PropertyNotFoundException(property);
        }
    }

    @Override
    public String getParsedProperty(String property) {

        try {
            return (String) propertyConversionService.convertPropertyForField(String.class, getProperty(property));
        } catch (Throwable e) {

            // This should be impossible to happen
            log.error("Failed to convert property {}.", property, e);
            throw new PropertyParsingException(e, property);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();

        // When instance is destroyed un-register the MXBean
        final MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        final ObjectName objectName = new ObjectName(MXBEAN_NAME);
        mBeanServer.unregisterMBean(objectName);
    }

    //**********************************************************
    //**********************************************************
    // PROPERTIES START
    //**********************************************************
    //**********************************************************

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public Properties getProperties() {
		return this.properties;
	}

    //**********************************************************
    //**********************************************************
    // PROPERTIES END
    //**********************************************************
    //**********************************************************

	public void startWatching() {
		if (null == this.eventNotifier) {
			throw new BeanInitializationException("Event bus not setup, you should not be calling this method...!");
		}
		try {
			// Here we actually create and set a FileWatcher to monitor the given locations
			Executors.newSingleThreadExecutor().execute(new PropertiesWatcher(this.locations, this, delay));

            // Register this instance as an MBean
            registerMBean();
		}
		catch (final IOException e) {
			log.error("Unable to start properties file watcher", e);
		}
	}

    private void registerMBean() {

        try {
            final MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
            final ObjectName objectName = new ObjectName(MXBEAN_NAME);
            mBeanServer.registerMBean(this, objectName);
        } catch (JMException e) {
            log.error("MBean registration failed.", e);
        }
    }

    private void processProperty(String oldValue, String newValue, String property) {

        // Update locally stored copy of properties
        this.properties.setProperty(property, newValue);

        // Post change event to notify any potential listeners
        this.eventNotifier.post(new PropertyModifiedEvent(property, oldValue, newValue));
    }

	public Object resolveProperty(final Object property) {
		final Object resolvedPropertyValue = this.properties.get(this.propertyResolver.resolveProperty(property));
		if (this.propertyResolver.requiresFurtherResoltuion(resolvedPropertyValue)) {
			return resolveProperty(resolvedPropertyValue);
		}
		return resolvedPropertyValue;
	}

	private boolean propertyChange(final String oldValue, final String newValue) {
		return null == oldValue || !oldValue.equals(newValue);
	}

	private boolean propertyExistsAndNotNull(final String property, final String newValue) {
		return this.properties.containsKey(property) && null != newValue;
	}
}
