package com.zmq.property;

import com.zmq.utils.ClassUtils;
import com.zmq.utils.YamlUtils;
import jakarta.annotation.Nullable;

import java.util.*;
import java.util.function.Function;

/**
 * <p>针对 <code>@Value 对配置参数的注入，而定义的配置解析类
 * <p>PropertyResolver类提供3种查询方式：
 * <ul>
 *     <li>1. 按配置的key查询，例如：getProperty("app.title");</li>
 *     <li>2. 以${abc.xyz}形式的查询，例如，getProperty("${app.title}")，常用于@Value("${app.title}")注入；</li>
 *     <li>3. 带默认值的，以${abc.xyz:defaultValue}形式的查询，例如，getProperty("${app.title:Summer}")，常用于@Value("${app.title:Summer}")注入。</li>
 * </ul>
 *
 */
public class PropertyResolver {
    /**
     * 存储解析完成的参数
     */
    private final Map<String, Object> properties = new HashMap<>();

    /**
     * 用于将字符串转换为所需类型的转换器，默认支持：
     * <ul>
     *     <li>boolean、Boolean</li>
     *     <li>byte、Byte</li>
     *     <li>short、Short</li>
     *     <li>int、Integer</li>
     *     <li>long、Long</li>
     *     <li>float、Float</li>
     *     <li>double、Double</li>
     *     <li>LocalDate</li>
     *     <li>LocalTime</li>
     *     <li>LocalDateTime</li>
     *     <li>ZonedDateTime</li>
     *     <li>Duration</li>
     *     <li>ZoneId</li>
     * </ul>
     * 也可以自定义转换器 {@link PropertyResolver#registerConverter(Class, Function) registerConverter}
     */
    private final Map<Class<?>, Function<String, Object>> converters = new HashMap<>(ClassUtils.CLASS_CONVERTER);

    public PropertyResolver(Properties props) {
        // 存入环境变量
        this.properties.putAll(System.getenv());
        // 存入Properties
        Set<String> names = props.stringPropertyNames();
        Map<String, Object> stringObjectMap = YamlUtils.loadYamlAsPlainMap("/application.yml");
        this.properties.putAll(stringObjectMap);
        names.forEach(name -> this.properties.put(name, props.getProperty(name)));
    }

    /**
     * 注册自定义类型转换器
     * @param targetType 需要转换的类型
     * @param converter 转换方法
     */
    public void registerConverter(Class<?> targetType, Function<String, Object> converter) {
        this.converters.put(targetType, converter);
    }

    /**
     * 获取key对应的参数值，如果指定的key不存在，则返回指定的默认值
     * @param key 指定的key
     * @param defaultValue 指定的默认值
     * @return 获取到的参数值
     */
    public String getPropertyOrDefault(String key, String defaultValue) {
        String value = getProperty(key);
        return value != null ? value : parseValue(defaultValue);
    }

    /**
     * 获取key对应的参数值，如果指定的key不存在，则抛出空指针异常 NullPointerException
     * @param key 指定的key
     * @return 获取到的参数值
     */
    public String getRequiredProperty(String key) {
        String value = getProperty(key);
        return Objects.requireNonNull(value, STR."Property \"\{key}\" not found ");
    }

    public String parseValue(String value) {
        PropertyExpr propertyExpr = parsePropertyExpr(value);
        if (propertyExpr != null) {
            if (propertyExpr.defaultValue() != null) {
                return getPropertyOrDefault(propertyExpr.key(), propertyExpr.defaultValue());
            } else {
                return getRequiredProperty(propertyExpr.key());
            }
        }
        return value;
    }

    @Nullable
    public <T> T getProperty(String key, Class<T> targetType) {

        String value = getProperty(key);
        if (value != null) {
            return convert(value, targetType);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T> T convert(String value, Class<T> targetType) {
        Function<String, Object> converter = converters.get(targetType);
        if (converter == null) {
            throw new IllegalArgumentException(STR."Unsupported value type: \{targetType.getName()}");
        }
        return (T) converter.apply(value);
    }


    @Nullable
    public String getProperty(String key) {
        PropertyExpr keyExpr = parsePropertyExpr(key);
        if (keyExpr != null) {
            if (keyExpr.defaultValue() != null) {
                return getPropertyOrDefault(keyExpr.key(), keyExpr.defaultValue());
            } else {
                return getRequiredProperty(keyExpr.key());
            }
        }
        Object value = this.properties.get(key);
        if (value instanceof List){
            throw new IllegalArgumentException(STR."The key '\{key}' has duplicate values");
        }
        if (value != null) {
            return parseValue(value.toString());
        }
        return null;
    }


    // 解析 "${}"

    PropertyExpr parsePropertyExpr(String key) {
        if (key.startsWith("${") && key.endsWith("}")) {
            int n = key.indexOf(':');
            if (n == -1) {  // 没有默认值defaultValue: ${key}
                String k = key.substring(2, key.length() - 1);
                return new PropertyExpr(k, null);
            } else {
                String k = key.substring(2, n);
                return new PropertyExpr(k, key.substring(n + 1, key.length() - 1));
            }
        }
        return null;
    }


}

record PropertyExpr(String key, String defaultValue) {

}
