package com.zmq.utils;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.resolver.Resolver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author zmq
 * 读取YAML文件的工具类
 */
public class YamlUtils {

    /**
     * 从指定路径读取yaml文件，并解析其格式转化为yaml形式的Map对象
     *
     * @param path 需要读取的yaml文件路径
     * @return 转化后的Map对象
     */
    private static Map<String, Object> loadYaml(String path) {
        LoaderOptions loaderOptions = new LoaderOptions();
        DumperOptions dumperOptions = new DumperOptions();
        Representer representer = new Representer(dumperOptions);
        //禁用所有的隐式转换，并将所有值视为字符串。
        class NoImplicitResolver extends Resolver {
            public NoImplicitResolver() {
                super();
                super.yamlImplicitResolvers.clear();
            }
        }
        NoImplicitResolver resolver = new NoImplicitResolver();
        Yaml yaml = new Yaml(new Constructor(loaderOptions), representer, dumperOptions, loaderOptions, resolver);
        return ClassPathUtils.readInputStream(path, yaml::load);
    }

    /**
     * 从指定路径读取yaml文件，并解析其格式转化为properties形式的Map对象
     *
     * @param path 需要读取的yaml文件路径
     * @return 转化后的Map对象
     */
    public static Map<String, Object> loadYamlAsPlainMap(String path) {
        Map<String, Object> data = loadYaml(path);
        Map<String, Object> plain = new HashMap<>();
        convertTo(data, "", plain);
        return plain;
    }

    /**
     * 用递归方式将yaml格式的Map转化为properties格式的map,例如
     * <blockquote>
     * <pre>
     * {
     *      "app":{
     *                "title": "Spring Framework"
     *            }
     * }
     * </pre></blockquote>
     * 转化为:
     * <blockquote>
     * <pre>
     * {
     *      "app.title":"Spring Framework"
     * }
     * </pre></blockquote>
     * @param source yaml格式的Map
     * @param prefix 转化后的key的前缀
     * @param plain  转化后的Map
     */
    @SuppressWarnings("unchecked")
    private static void convertTo(Map<String, Object> source, String prefix, Map<String, Object> plain) {
        if (source==null){
            return;
        }
        for (String key : source.keySet()) {
            Object value = source.get(key);
            if (value instanceof Map map) {
                convertTo((Map<String, Object>) map, prefix +"."+ key+".", plain);
            } else if (value instanceof List) {
                plain.put(prefix + key, value);
            } else {
                plain.put(prefix + key, value.toString());
            }
        }
    }

}

