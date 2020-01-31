package org.oscm.util;

import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.*;

public class PropertyUtilTest {

    @Test
    public void testProp() throws ClassNotFoundException {
        final Properties properties = PropertyUtil.getPropertiesFor("propertyUtilTest.properties");
        assertEquals("Text", properties.getProperty("random.something"));
    }

//    @Test
//    public void
}