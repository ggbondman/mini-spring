package com.zmq.utils;

import com.zmq.io.InputStreamCallback;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import static java.lang.System.out;

/**
 * @author zmq
 * <p>读取文件流相关的工具类
 */
public class ClassPathUtils {

    /**
     * 从指定路径读取文件流，并按照回调方法进行处理
     *
     * @param path                文件路径
     * @param inputStreamCallback 对获取到的文件流进行处理的回调方法
     * @param <T>                 回调方法返回的对象类型
     * @return 回调方法返回的对象
     */
    public static <T> T readInputStream(String path, InputStreamCallback inputStreamCallback) {
        if (path.startsWith("/")) path = path.substring(1);
        try (InputStream inputStream = getContextClassLoader().getResourceAsStream(path)) {
            if (inputStream == null) {
                out.println("The application.yml has not found");
                return null;
            }
            // 方法返回InputStream会导致异常传递，最好使用回调方法处理
            return inputStreamCallback.get(inputStream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * 从指定路径读取文件流,并直接转化为字符串
     *
     * @param path 文件路径
     * @return 转化后的字符串
     */
    public static String readString(String path) {
        return readInputStream(path, new InputStreamCallback() {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T get(InputStream stream) throws IOException {
                return (T) new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            }
        });
    }

    /**
     * 获取上下文的类加载器
     * <p>默认获取当前线程的上下文类加载器，如果获取不到就转为获取当前类的类加载器
     *
     * @return 类加载器
     */
    public static ClassLoader getContextClassLoader() {
        ClassLoader cl = null;
        cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = ClassPathUtils.class.getClassLoader();
        }
        return cl;
    }

}
