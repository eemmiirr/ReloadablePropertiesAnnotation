package com.morgan.design.properties.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import com.morgan.design.properties.testBeans.ReloadingAutowiredPropertyBean;

@ContextConfiguration(locations = { "classpath:/spring/spring-reloading-reloadablePropertyPostProcessorIntTest.xml" })
public class UpdatingReloadablePropertyPostProcessorIntTest extends AbstractJUnit4SpringContextTests {

	private static final String DIR = "target/test-classes/test-files/";
	private static final String PROPERTIES = "reloading.properties";

	@Autowired
	private ReloadingAutowiredPropertyBean bean;

	private Properties loadedProperties;

	@Before
	public void setUp() throws IOException {
		this.loadedProperties = PropertiesLoaderUtils.loadAllProperties(PROPERTIES);
		assertThat(this.bean.getStringProperty(), is("Injected String Value"));
        assertThat(this.bean.getIntegerELProperty(), is(2));
        assertThat(this.bean.getStringELProperty(), is("EL Injected String Value"));
	}

	@After
	public void cleanUp() throws Exception {
		this.loadedProperties.setProperty("dynamicProperty.stringValue", "Injected String Value");
        this.loadedProperties.setProperty("dynamicELProperty.integerValue", "#{ 1+1 }");
        this.loadedProperties.setProperty("dynamicELProperty.stringValue", "#{ new String('EL ').concat('Injected String Value') }");

		final OutputStream newOutputStream = new FileOutputStream(new File(DIR + PROPERTIES));
		this.loadedProperties.store(newOutputStream, null);

		Thread.sleep(2000); // this is a hack -> I need to find an alternative

		assertThat(this.bean.getStringProperty(), is("Injected String Value"));
        assertThat(this.bean.getIntegerELProperty(), is(2));
        assertThat(this.bean.getStringELProperty(), is("EL Injected String Value"));
	}

	@Test
	public void shouldReloadAlteredStringProperty() throws Exception {
		assertThat(this.bean.getStringProperty(), is("Injected String Value"));

		this.loadedProperties.setProperty("dynamicProperty.stringValue", "Altered Injected String Value");

		final File file = new File(DIR + PROPERTIES);
		final OutputStream newOutputStream = new FileOutputStream(file);
		this.loadedProperties.store(newOutputStream, null);
		newOutputStream.flush();
		newOutputStream.close();

		Thread.sleep(2000); // this is a hack -> I need to find an alternative

		assertThat(this.bean.getStringProperty(), is("Altered Injected String Value"));
	}

    @Test
    public void shouldReloadAlteredIntegerElProperty() throws Exception {
        assertThat(this.bean.getIntegerELProperty(), is(2));

        this.loadedProperties.setProperty("dynamicELProperty.integerValue", "#{ 2+2 }");

        final File file = new File(DIR + PROPERTIES);
        final OutputStream newOutputStream = new FileOutputStream(file);
        this.loadedProperties.store(newOutputStream, null);
        newOutputStream.flush();
        newOutputStream.close();

        Thread.sleep(2000); // this is a hack -> I need to find an alternative

        assertThat(this.bean.getIntegerELProperty(), is(4));
    }

    @Test
    public void shouldReloadAlteredStringELProperty() throws Exception {
        assertThat(this.bean.getStringELProperty(), is("EL Injected String Value"));

        this.loadedProperties.setProperty("dynamicELProperty.stringValue", "#{ new String('Altered EL ').concat('Injected String Value') }");

        final File file = new File(DIR + PROPERTIES);
        final OutputStream newOutputStream = new FileOutputStream(file);
        this.loadedProperties.store(newOutputStream, null);
        newOutputStream.flush();
        newOutputStream.close();

        Thread.sleep(2000); // this is a hack -> I need to find an alternative

        assertThat(this.bean.getStringELProperty(), is("Altered EL Injected String Value"));
    }
}
