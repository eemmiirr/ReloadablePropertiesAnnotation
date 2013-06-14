package com.morgan.design.properties.testBeans;

import org.springframework.stereotype.Component;

import com.morgan.design.properties.ReloadableProperty;

@Component
public class ReloadingAutowiredPropertyBean {

	@ReloadableProperty("dynamicProperty.stringValue")
	private String stringProperty;

    @ReloadableProperty("dynamicELProperty.integerValue")
    private Integer integerELProperty;

    @ReloadableProperty("dynamicELProperty.stringValue")
    private String stringELProperty;

	public String getStringProperty() {
		return this.stringProperty;
	}

    public Integer getIntegerELProperty() {
        return integerELProperty;
    }

    public String getStringELProperty() {
        return stringELProperty;
    }
}
