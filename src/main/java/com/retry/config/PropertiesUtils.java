package com.retry.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.ResourceUtils;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by zbyte on 17-7-21.
 *
 * 获取retry.conf配置信息
 */
public class PropertiesUtils {

    public static final String CONFIG = System.getProperty("conf", "classpath:retry.conf");

    private static final Map<String, String> PROPERTIES = new HashMap<>();

    private static final Log LOGGER = LogFactory.getLog(PropertiesUtils.class);

    /**
     * 初始化标志
     */
    private static boolean INITED = false;

    /**
     * 初始化
     */
    static {
        PropertiesUtils.init();
    }

    public static void init() {
        if (!PropertiesUtils.INITED) {
            PropertiesUtils.init(PropertiesUtils.PROPERTIES);
            PropertiesUtils.INITED = true;
        }
    }

    private static void init(Map<String, String> map) {
        Properties properties = new Properties();
        PropertiesUtils.LOGGER.info("Loading config file: " + PropertiesUtils.CONFIG);
        PropertiesUtils.loading(PropertiesUtils.CONFIG, properties);
        PropertiesUtils.convert(map, properties);
        // System Properties覆盖配置文件
        PropertiesUtils.convert(map, System.getProperties());
    }

    /**
     * HashTable to HashMap
     * @param map
     * @param properties
     */
    private static void convert(Map<String, String> map, Properties properties) {
        for (Object item : properties.keySet()) {
            String key = item.toString();
            String value = properties.getProperty(key);
            map.put(key.trim().toLowerCase(), value.trim());
            PropertiesUtils.LOGGER.debug("Loading property key = " + key + ", value = " + value);
        }
    }

    /**
     * 加载配置文件
     * @param file
     * @param properties
     */
    private static void loading(String file, Properties properties) {
        try (InputStream input = ResourceUtils.getURL(file).openStream()) {
            properties.load(input);
        } catch (Throwable throwable) {
            PropertiesUtils.LOGGER.warn(throwable.getMessage(), throwable);
        }
    }

    public static String get(String key) {
        return PropertiesUtils.get(key, null);
    }

    public static String get(String key, String def) {
        String value = PropertiesUtils.PROPERTIES.get(key);
        return value == null ? def : value;
    }

}
