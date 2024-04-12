package com.zmq.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author zmq
 * 回调函数，用于从YAML文件流中读取数据并格式化
 */
@FunctionalInterface
public interface InputStreamCallback {

    <T> T get(InputStream stream) throws IOException;
}
