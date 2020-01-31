package org.oscm.util;

import lombok.*;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class PropertyUtil {
    private static Map<PropertyIdWrapper, Properties> propertyMap = new ConcurrentHashMap<>();

    @Value
    @EqualsAndHashCode
    private static class PropertyIdWrapper {
        private String fileName;
        private String moduleName;
    }

    private static PropertyUtil propertyUtil;

    private PropertyUtil(){}

    public static Properties getPropertiesFor(String fileName) throws ClassNotFoundException {
        if(propertyUtil == null){
            propertyUtil = new PropertyUtil();
        }
        return propertyUtil.sdass(fileName);
      }


      private Properties sdass(String fileName) throws ClassNotFoundException {
          Class<?> aClass = Class.forName(Thread.currentThread().getStackTrace()[2].getClassName());
          final PropertyIdWrapper idWrapper = new PropertyIdWrapper(fileName, aClass.getPackage().toString());
          if(!propertyMap.containsKey(idWrapper)){
              loadProperties(fileName, aClass);
          }
          return propertyMap.get(idWrapper);
      }

    private static void loadProperties(String fileName, Class<?> tClass) {
        final Properties properties = new Properties();
        try {
            String pack = tClass.getPackage().toString();
            properties.load(tClass.getClassLoader().getResourceAsStream(fileName));
            propertyMap.put(new PropertyIdWrapper(fileName, pack), properties);
        } catch (IOException e) {
//            TODO
            e.printStackTrace();
        }
    }
}
